package ddraig.net.entropica.spell.mutation.element;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.entity.SpellEidolonEntity;
import ddraig.net.entropica.entity.projectile.SpellProjectileEntity;
import ddraig.net.entropica.item.EssenceItem;
import ddraig.net.entropica.registry.ModItems;
import ddraig.net.entropica.spell.engine.SpellCombatReactionEngine;
import ddraig.net.entropica.spell.engine.SpellExecutionEngine;
import ddraig.net.entropica.spell.engine.SpellLingeringZoneManager;
import ddraig.net.entropica.spell.fuel.SpellFuelService;
import ddraig.net.entropica.spell.mutation.handler.MateriaPartMutationHandler;
import ddraig.net.entropica.spell.mutation.handler.SpellMutationHandlerRegistry;
import ddraig.net.entropica.util.EntityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.village.ReputationEventType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared runtime mechanics for Tier 4 Apex (#48–#50), Prismatic (#51),
 * Tier 5 Eschaton (#52–#54), and Tier 6 Singularity (#55–#57) Spell Mutations:
 * <ul>
 *     <li>5.0-second (100-tick) movement-validated continuous Transmute channeling</li>
 *     <li>Bottled standing-water Elixirs &amp; Omega Horizon alchemical essence</li>
 *     <li>Curse purging and permanent enchantment tier elevation (+1, +2, and +5)</li>
 *     <li>Non-protected block-to-Materia-fuel dissolution (Eschaton Event Horizon Vaporization)</li>
 *     <li>Shared friendly/smite/projectile/shield/aggro helpers</li>
 * </ul>
 */
public final class ApexEschatonSingularityHelper {

    public static final String TAG_TRANSMUTE_CHANNEL_BYPASS = "SpellApexTransmuteBypass";
    public static final String TAG_TRANSMUTE_CHANNEL_ACTIVE_UNTIL = "SpellApexTransmuteActiveUntil";
    public static final String TAG_ELIXIR_TYPE = "SpellApexElixirType";

    private ApexEschatonSingularityHelper() {
    }

    // =========================================================================
    // 1. 5.0-Second (100-Tick) Continuous Transmute Channeling
    // =========================================================================

    /**
     * Enforces the canonical 5.0-second (100-tick) continuous channeling requirement on
     * Tier 4 Apex, Prismatic, Tier 5 Eschaton, and Tier 6 Singularity {@code TRANSMUTE} spells.
     *
     * @return {@code true} if a 5.0s channel has been started (or is already active), meaning the caller
     *         should NOT immediately transmute; {@code false} if {@code caster == null} or channel bypass
     *         is active so the caller should execute its per-block transmutation immediately.
     */
    public static boolean beginOrExecuteChanneledTransmute(
            ServerLevel level,
            @Nullable LivingEntity caster,
            BlockPos origin,
            int radius,
            int potencyTier,
            EssenceType materia,
            String title,
            @Nullable Runnable onChannelCompletedExtra
    ) {
        if (caster == null) {
            return false;
        }
        CompoundTag casterData = EntityHelper.getPersistentData(caster);
        if (casterData.getBoolean(TAG_TRANSMUTE_CHANNEL_BYPASS).orElse(false)) {
            return false;
        }
        long now = level.getGameTime();
        if (casterData.getLong(TAG_TRANSMUTE_CHANNEL_ACTIVE_UNTIL).orElse(0L) > now) {
            return true;
        }

        casterData.putLong(TAG_TRANSMUTE_CHANNEL_ACTIVE_UNTIL, now + 104L);
        Vec3 anchor = caster.position();
        BlockPos targetOrigin = origin.immutable();

        if (caster instanceof ServerPlayer sp) {
            sp.displayClientMessage(Component.literal("§e✦ Channeling " + title + " (5.0s — remain steady)..."), true);
        }
        level.playSound(null, caster.blockPosition(), SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 0.85F, 1.25F);

        SpellLingeringZoneManager.registerZone(level, new SpellLingeringZoneManager.ActiveSpellZone(
                caster, Vec3.atCenterOf(targetOrigin), radius, 100, now, null, null, materia, 1.0F,
                (srv, zone, elapsed) -> {
                    LivingEntity owner = zone.resolveCaster(srv);
                    if (owner == null || !owner.isAlive()) {
                        return false;
                    }

                    double dx = owner.getX() - anchor.x;
                    double dz = owner.getZ() - anchor.z;
                    if ((dx * dx + dz * dz) > 1.5625D) {
                        EntityHelper.getPersistentData(owner).remove(TAG_TRANSMUTE_CHANNEL_ACTIVE_UNTIL);
                        if (owner instanceof ServerPlayer sp) {
                            sp.displayClientMessage(Component.literal("§c✖ " + title + " channel interrupted!"), true);
                        }
                        srv.playSound(null, owner.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.75F, 0.9F);
                        return false;
                    }

                    if (elapsed < 100) {
                        if (elapsed % 8 == 0) {
                            srv.sendParticles(ParticleTypes.ENCHANT, owner.getX(), owner.getY(1.1D), owner.getZ(),
                                    12, 0.45D, 0.55D, 0.45D, 0.15D);
                            srv.sendParticles(ParticleTypes.END_ROD, zone.center.x, zone.center.y + 0.6D, zone.center.z,
                                    10, Math.min(4.0D, radius * 0.35D), 0.4D, Math.min(4.0D, radius * 0.35D), 0.03D);
                        }
                        if (elapsed % 20 == 0 && owner instanceof ServerPlayer sp) {
                            float secsLeft = Math.max(0.0F, (100 - elapsed) / 20.0F);
                            sp.displayClientMessage(Component.literal(String.format("§e✦ Channeling %s (%.1fs)...", title, secsLeft)), true);
                        }
                        return true;
                    }

                    CompoundTag d = EntityHelper.getPersistentData(owner);
                    d.putBoolean(TAG_TRANSMUTE_CHANNEL_BYPASS, true);
                    try {
                        if (onChannelCompletedExtra != null) {
                            onChannelCompletedExtra.run();
                        }
                        MateriaPartMutationHandler handler = SpellMutationHandlerRegistry.get(materia);
                        if (handler != null) {
                            int rSq = radius * radius;
                            for (BlockPos p : BlockPos.betweenClosed(
                                    targetOrigin.offset(-radius, -3, -radius),
                                    targetOrigin.offset(radius, 3, radius))) {
                                if (targetOrigin.distSqr(p) > (double) rSq) continue;
                                BlockState bs = srv.getBlockState(p);
                                if (bs.isAir()) {
                                    if (p.equals(targetOrigin)) {
                                        handler.onTransmuteBlock(srv, owner, p.immutable(), bs, potencyTier);
                                    }
                                    continue;
                                }
                                if (!SpellExecutionEngine.isProtectedBlockOrContainer(srv, p, bs)) {
                                    handler.onTransmuteBlock(srv, owner, p.immutable(), bs, potencyTier);
                                }
                            }
                            for (ItemEntity item : srv.getEntitiesOfClass(ItemEntity.class, new AABB(targetOrigin).inflate(radius))) {
                                if (item.isAlive()) {
                                    handler.onTransmuteDroppedItem(srv, owner, item, potencyTier);
                                }
                            }
                        }
                    } finally {
                        d.remove(TAG_TRANSMUTE_CHANNEL_BYPASS);
                        d.remove(TAG_TRANSMUTE_CHANNEL_ACTIVE_UNTIL);
                    }

                    srv.playSound(null, targetOrigin, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.15F);
                    srv.sendParticles(ParticleTypes.END_ROD, zone.center.x, zone.center.y + 0.8D, zone.center.z,
                            36, Math.min(5.0D, radius * 0.4D), 0.8D, Math.min(5.0D, radius * 0.4D), 0.06D);
                    if (owner instanceof ServerPlayer sp) {
                        sp.displayClientMessage(Component.literal("§a✦ " + title + " completed!"), true);
                    }
                    return false;
                }
        ));
        return true;
    }

    // =========================================================================
    // 2. Curse Purging & Permanent Enchantment Tier Elevation
    // =========================================================================

    public static boolean purgeCursesAndElevateEquipment(
            LivingEntity target,
            int bonusLevels,
            float elevateChance,
            int maxEnchantsPerPiece
    ) {
        boolean changed = false;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack eq = target.getItemBySlot(slot);
            if (purgeCursesAndElevateStack(eq, target.getRandom(), bonusLevels, elevateChance, maxEnchantsPerPiece)) {
                changed = true;
            }
        }
        return changed;
    }

    public static boolean purgeCursesAndElevateStack(
            ItemStack stack,
            RandomSource rand,
            int bonusLevels,
            float elevateChance,
            int maxEnchantsToElevate
    ) {
        if (stack.isEmpty()) return false;
        boolean changed = PenumbraSpellMutationHandler.purgeCursesFromStack(stack);
        if (bonusLevels <= 0 || maxEnchantsToElevate <= 0 || rand.nextFloat() >= elevateChance) {
            return changed;
        }

        ItemEnchantments ench = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (ench.isEmpty()) return changed;

        ItemEnchantments.Mutable mut = new ItemEnchantments.Mutable(ench);
        List<Holder<Enchantment>> nonCurses = new ArrayList<>();
        for (Holder<Enchantment> holder : ench.keySet()) {
            if (!holder.is(EnchantmentTags.CURSE)) {
                nonCurses.add(holder);
            }
        }
        if (nonCurses.isEmpty()) return changed;

        int count = Math.min(maxEnchantsToElevate, nonCurses.size());
        // Shuffle slightly when elevating a subset (e.g. +1 on 1 random enchant or +2 on up to 2 enchants)
        if (count < nonCurses.size()) {
            for (int i = nonCurses.size() - 1; i > 0; i--) {
                int j = rand.nextInt(i + 1);
                Holder<Enchantment> tmp = nonCurses.get(i);
                nonCurses.set(i, nonCurses.get(j));
                nonCurses.set(j, tmp);
            }
        }

        for (int i = 0; i < count; i++) {
            Holder<Enchantment> h = nonCurses.get(i);
            int cur = ench.getLevel(h);
            if (cur > 0) {
                mut.set(h, Math.min(255, cur + bonusLevels));
                changed = true;
            }
        }
        if (changed) {
            stack.set(DataComponents.ENCHANTMENTS, mut.toImmutable());
        }
        return changed;
    }

    // =========================================================================
    // 3. Zombie Villager Instant Cure with Permanent Maximum Trade Discounts
    // =========================================================================

    public static boolean cureZombieVillagersWithMaxDiscounts(
            ServerLevel level,
            @Nullable LivingEntity caster,
            BlockPos center,
            double radius
    ) {
        boolean curedAny = false;
        for (ZombieVillager zv : level.getEntitiesOfClass(ZombieVillager.class, new AABB(center).inflate(radius), LivingEntity::isAlive)) {
            Villager villager = EntityType.VILLAGER.create(level, EntitySpawnReason.CONVERSION);
            if (villager != null) {
                villager.setPos(zv.getX(), zv.getY(), zv.getZ());
                villager.setYRot(zv.getYRot());
                villager.setXRot(zv.getXRot());
                villager.setVillagerData(zv.getVillagerData());
                if (zv.isBaby()) villager.setBaby(true);
                level.addFreshEntity(villager);
                if (caster instanceof Player player) {
                    for (int i = 0; i < 5; i++) {
                        villager.onReputationEventFrom(ReputationEventType.ZOMBIE_VILLAGER_CURED, player);
                    }
                }
                zv.discard();
                level.playSound(null, villager.blockPosition(), SoundEvents.ZOMBIE_VILLAGER_CURE, SoundSource.PLAYERS, 1.0F, 1.0F);
                curedAny = true;
            }
        }
        return curedAny;
    }

    // =========================================================================
    // 4. Bottled Standing-Water Elixirs & Elixir Right-Click Consumption
    // =========================================================================

    public static boolean tryBottleStandingWaterElixir(
            ServerLevel level,
            @Nullable LivingEntity caster,
            BlockPos origin,
            int searchRadius,
            EssenceType materia
    ) {
        BlockPos waterPos = null;
        for (BlockPos p : BlockPos.betweenClosed(
                origin.offset(-searchRadius, -2, -searchRadius),
                origin.offset(searchRadius, 2, searchRadius))) {
            BlockState bs = level.getBlockState(p);
            if (bs.is(Blocks.WATER) && bs.getFluidState().isSource()
                    && !SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) {
                waterPos = p.immutable();
                break;
            }
        }
        if (waterPos == null) {
            return false;
        }

        boolean consumedFromPlayer = false;
        ItemEntity bottleEntity = null;
        if (caster instanceof Player player) {
            Inventory inv = player.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack s = inv.getItem(i);
                if (s.is(Items.GLASS_BOTTLE)) {
                    if (!player.getAbilities().instabuild) {
                        s.shrink(1);
                    }
                    consumedFromPlayer = true;
                    break;
                }
            }
        }
        if (!consumedFromPlayer) {
            for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(origin).inflate(searchRadius + 1.5D))) {
                if (item.isAlive() && item.getItem().is(Items.GLASS_BOTTLE)) {
                    bottleEntity = item;
                    break;
                }
            }
            if (bottleEntity == null) {
                return false;
            }
            ItemStack st = bottleEntity.getItem();
            st.shrink(1);
            if (st.isEmpty()) {
                bottleEntity.discard();
            } else {
                bottleEntity.setItem(st);
            }
        }

        level.setBlockAndUpdate(waterPos, Blocks.AIR.defaultBlockState());
        ItemStack output = createTransmutedElixirStack(level.random, materia);
        if (output.isEmpty()) return false;

        if (consumedFromPlayer && caster instanceof Player player) {
            if (!player.getInventory().add(output)) {
                player.drop(output, false);
            }
        } else {
            ItemEntity drop = new ItemEntity(level, waterPos.getX() + 0.5D, waterPos.getY() + 0.5D, waterPos.getZ() + 0.5D, output);
            drop.setDefaultPickUpDelay();
            level.addFreshEntity(drop);
        }
        level.playSound(null, waterPos, SoundEvents.BOTTLE_FILL_DRAGONBREATH, SoundSource.PLAYERS, 1.0F, 1.15F);
        return true;
    }

    public static ItemStack createTransmutedElixirStack(RandomSource rand, EssenceType materia) {
        return switch (materia) {
            case EMPYREAN -> makeNamedElixir(
                    "§6Dew of the World-Tree",
                    "EMPYREAN_DEW"
            );
            case TERMINUS -> makeNamedElixir(
                    "§bGlacial Frost Elixir",
                    "TERMINUS_FROST"
            );
            case APOTHEOSIS -> makeNamedElixir(
                    "§eNectar of the Alpha Genesis",
                    "APOTHEOSIS_NECTAR"
            );
            case ESCHATON -> {
                if (rand.nextBoolean()) {
                    yield makeNamedElixir(
                            "§5Gravitational Stasis Elixir",
                            "ESCHATON_STASIS"
                    );
                }
                ItemStack omegaEssence = new ItemStack(ModItems.STRONG_ESSENCE.get(), 1);
                EssenceItem.setEssenceType(omegaEssence, EssenceType.ESCHATON);
                omegaEssence.set(DataComponents.CUSTOM_NAME, Component.literal("§5Essence of the Omega Horizon"));
                CompoundTag tag = omegaEssence.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
                tag.putString("SpellApexBrewingIngredient", "OMEGA_HORIZON");
                omegaEssence.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                yield omegaEssence;
            }
            default -> ItemStack.EMPTY;
        };
    }

    private static ItemStack makeNamedElixir(String displayName, String elixirId) {
        ItemStack potion = new ItemStack(Items.POTION, 1);
        potion.set(DataComponents.CUSTOM_NAME, Component.literal(displayName));
        CompoundTag tag = new CompoundTag();
        tag.putString(TAG_ELIXIR_TYPE, elixirId);
        potion.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return potion;
    }

    public static void consumeTransmutedElixir(
            ServerLevel level,
            Player player,
            ItemStack stack,
            InteractionHand hand,
            String elixirType
    ) {
        long now = level.getGameTime();
        switch (elixirType) {
            case "EMPYREAN_DEW" -> {
                // Dew of the World-Tree: Regeneration III, Saturation, and +10 Absorption hearts (20.0 HP)
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 2));
                player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 200, 0));
                player.setAbsorptionAmount(Math.max(player.getAbsorptionAmount(), 20.0F));
            }
            case "TERMINUS_FROST" -> {
                // Glacial Frost Elixir: +6 Armor Toughness, Freeze & Fire immunity, +30% attack speed on ice (60s)
                EntityHelper.getPersistentData(player)
                        .putLong(SpellLingeringZoneManager.TAG_TERMINUS_ELIXIR_UNTIL, now + 1200L);
                player.clearFire();
                player.setTicksFrozen(0);
            }
            case "ESCHATON_STASIS" -> {
                // Gravitational Stasis Elixir: +10 Armor Toughness, complete damage reduction, zero-g flight for 60s
                EntityHelper.getPersistentData(player)
                        .putLong(SpellLingeringZoneManager.TAG_ESCHATON_STASIS_ELIXIR_UNTIL, now + 1200L);
                SpellLingeringZoneManager.grantArcane3DFlight(player, 1200);
            }
            case "APOTHEOSIS_NECTAR" -> {
                // Nectar of the Alpha Genesis: Regeneration V, Saturation, +14 Absorption hearts (28.0 HP), 3D flight for 90s
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 4));
                player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 400, 1));
                player.setAbsorptionAmount(Math.max(player.getAbsorptionAmount(), 28.0F));
                SpellLingeringZoneManager.grantArcane3DFlight(player, 1800);
            }
            default -> {
                return;
            }
        }

        level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_DRINK.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
        level.playSound(null, player.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.7F, 1.35F);
        level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY(1.0D), player.getZ(), 18, 0.35D, 0.45D, 0.35D, 0.05D);

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
            ItemStack emptyBottle = new ItemStack(Items.GLASS_BOTTLE);
            if (stack.isEmpty()) {
                player.setItemInHand(hand, emptyBottle);
            } else if (!player.getInventory().add(emptyBottle)) {
                player.drop(emptyBottle, false);
            }
        }
    }

    // =========================================================================
    // 5. Dimensional Event Horizon Vaporization (Block -> Materia Fuel)
    // =========================================================================

    public static int dissolveNonProtectedBlocksIntoFuel(
            ServerLevel level,
            @Nullable LivingEntity caster,
            BlockPos center,
            int radius,
            EssenceType fuelType,
            long mfumPerBlock
    ) {
        int r = Math.clamp(radius, 1, 16);
        int rSq = r * r;
        int dissolved = 0;
        int maxPerBurst = 72;

        for (BlockPos p : BlockPos.betweenClosed(center.offset(-r, -2, -r), center.offset(r, 3, r))) {
            if (dissolved >= maxPerBurst) break;
            if (center.distSqr(p) > (double) rSq) continue;
            BlockState bs = level.getBlockState(p);
            if (bs.isAir() || !bs.getFluidState().isEmpty()) continue;
            if (bs.getDestroySpeed(level, p) < 0.0F) continue;
            if (SpellExecutionEngine.isProtectedBlockOrContainer(level, p, bs)) continue;
            if (bs.is(Blocks.CRYING_OBSIDIAN) || bs.is(Blocks.OBSIDIAN) || bs.is(Blocks.BLACKSTONE)) continue;

            level.removeBlock(p, false);
            dissolved++;
        }

        if (dissolved > 0 && caster instanceof Player player) {
            SpellFuelService.restorePlayerMateriaFuel(player, fuelType, dissolved * Math.max(1L, mfumPerBlock));
        }
        return dissolved;
    }

    // =========================================================================
    // 6. Ore Smelting & Metamorphosis Helpers (Prismatic #51 & Cataclysm #53)
    // =========================================================================

    @Nullable
    public static Item resolveSmeltedOreOutput(Item input) {
        if (input == Items.RAW_COPPER || input == Items.COPPER_ORE || input == Items.DEEPSLATE_COPPER_ORE) return Items.COPPER_INGOT;
        if (input == Items.RAW_IRON || input == Items.IRON_ORE || input == Items.DEEPSLATE_IRON_ORE) return Items.IRON_INGOT;
        if (input == Items.RAW_GOLD || input == Items.GOLD_ORE || input == Items.DEEPSLATE_GOLD_ORE || input == Items.NETHER_GOLD_ORE) return Items.GOLD_INGOT;
        if (input == Items.REDSTONE_ORE || input == Items.DEEPSLATE_REDSTONE_ORE) return Items.REDSTONE;
        if (input == Items.LAPIS_ORE || input == Items.DEEPSLATE_LAPIS_ORE) return Items.LAPIS_LAZULI;
        if (input == Items.NETHER_QUARTZ_ORE) return Items.QUARTZ;
        if (input == Items.COAL_ORE || input == Items.DEEPSLATE_COAL_ORE) return Items.COAL;
        if (input == Items.EMERALD_ORE || input == Items.DEEPSLATE_EMERALD_ORE) return Items.EMERALD;
        if (input == Items.DIAMOND_ORE || input == Items.DEEPSLATE_DIAMOND_ORE) return Items.DIAMOND;
        if (input == Items.ANCIENT_DEBRIS) return Items.NETHERITE_SCRAP;
        return null;
    }

    @Nullable
    public static Item resolveSmeltedBlockOreOutput(Block block) {
        if (block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE) return Items.COPPER_INGOT;
        if (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE) return Items.IRON_INGOT;
        if (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE || block == Blocks.NETHER_GOLD_ORE) return Items.GOLD_INGOT;
        if (block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE) return Items.REDSTONE;
        if (block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE) return Items.LAPIS_LAZULI;
        if (block == Blocks.NETHER_QUARTZ_ORE) return Items.QUARTZ;
        if (block == Blocks.COAL_ORE || block == Blocks.DEEPSLATE_COAL_ORE) return Items.COAL;
        if (block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE) return Items.EMERALD;
        if (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE) return Items.DIAMOND;
        if (block == Blocks.ANCIENT_DEBRIS) return Items.NETHERITE_SCRAP;
        return null;
    }

    public static Item rollPrismaticMetamorphosisSmeltOutput(RandomSource rand, Item fallbackOutput) {
        if (rand.nextFloat() >= 0.10F) {
            return fallbackOutput;
        }
        Item[] upToDiamond = new Item[]{
                Items.COPPER_INGOT,
                Items.IRON_INGOT,
                Items.GOLD_INGOT,
                Items.LAPIS_LAZULI,
                Items.REDSTONE,
                Items.QUARTZ,
                Items.EMERALD,
                Items.DIAMOND
        };
        return upToDiamond[rand.nextInt(upToDiamond.length)];
    }

    @Nullable
    public static Item resolveDamagedMetalScrapIngot(ItemStack stack) {
        if (stack.isEmpty()) return null;
        Item it = stack.getItem();
        if (it == Items.IRON_SWORD || it == Items.IRON_PICKAXE || it == Items.IRON_AXE || it == Items.IRON_SHOVEL || it == Items.IRON_HOE
                || it == Items.IRON_HELMET || it == Items.IRON_CHESTPLATE || it == Items.IRON_LEGGINGS || it == Items.IRON_BOOTS
                || it == Items.CHAINMAIL_HELMET || it == Items.CHAINMAIL_CHESTPLATE || it == Items.CHAINMAIL_LEGGINGS || it == Items.CHAINMAIL_BOOTS
                || it == Items.SHEARS || it == Items.SHIELD || it == Items.IRON_HORSE_ARMOR) {
            return Items.IRON_INGOT;
        }
        if (it == Items.GOLDEN_SWORD || it == Items.GOLDEN_PICKAXE || it == Items.GOLDEN_AXE || it == Items.GOLDEN_SHOVEL || it == Items.GOLDEN_HOE
                || it == Items.GOLDEN_HELMET || it == Items.GOLDEN_CHESTPLATE || it == Items.GOLDEN_LEGGINGS || it == Items.GOLDEN_BOOTS
                || it == Items.GOLDEN_HORSE_ARMOR) {
            return Items.GOLD_INGOT;
        }
        if (it == Items.NETHERITE_SWORD || it == Items.NETHERITE_PICKAXE || it == Items.NETHERITE_AXE || it == Items.NETHERITE_SHOVEL || it == Items.NETHERITE_HOE
                || it == Items.NETHERITE_HELMET || it == Items.NETHERITE_CHESTPLATE || it == Items.NETHERITE_LEGGINGS || it == Items.NETHERITE_BOOTS) {
            return Items.NETHERITE_SCRAP;
        }
        return null;
    }

    // =========================================================================
    // 7. Shared Combat & Entity Helpers
    // =========================================================================

    public static boolean isFriendly(LivingEntity entity, @Nullable LivingEntity caster) {
        if (caster == null) return false;
        if (entity == caster || entity.isAlliedTo(caster)) return true;
        if (caster instanceof Player && entity instanceof Player && !(entity instanceof Monster)) return true;
        return entity instanceof SpellEidolonEntity eid && (caster.getUUID().equals(eid.getOwnerUuid()) || eid.isFriendlyTo(caster));
    }

    public static boolean isDarkUndeadOrAbyssal(LivingEntity entity) {
        return entity.getType().is(EntityTypeTags.UNDEAD)
                || entity instanceof WitherSkeleton
                || entity instanceof Blaze
                || entity instanceof Ghast
                || entity instanceof MagmaCube
                || entity instanceof AbstractPiglin
                || entity instanceof Hoglin
                || entity instanceof Zoglin
                || entity instanceof EnderMan
                || entity instanceof Endermite
                || entity instanceof Shulker
                || entity instanceof Warden
                || entity instanceof Phantom
                || entity instanceof Vex;
    }

    public static int vaporizeHostileProjectiles(
            ServerLevel level,
            @Nullable LivingEntity caster,
            AABB box,
            @Nullable EssenceType fuelRestoreType,
            long mfumPerProj
    ) {
        int count = 0;
        for (Projectile proj : level.getEntitiesOfClass(Projectile.class, box)) {
            if (caster != null && proj.getOwner() == caster) continue;
            if (proj instanceof SpellProjectileEntity sp && caster != null && sp.getOwner() == caster) continue;
            level.sendParticles(ParticleTypes.ASH, proj.getX(), proj.getY(), proj.getZ(), 5, 0.08D, 0.08D, 0.08D, 0.02D);
            proj.discard();
            count++;
        }
        if (count > 0 && fuelRestoreType != null && mfumPerProj > 0L && caster instanceof Player player) {
            SpellFuelService.restorePlayerMateriaFuel(player, fuelRestoreType, count * mfumPerProj);
        }
        return count;
    }

    public static void deflectHostileProjectiles(ServerLevel level, @Nullable LivingEntity caster, AABB box) {
        for (Projectile proj : level.getEntitiesOfClass(Projectile.class, box)) {
            if (caster != null && proj.getOwner() == caster) continue;
            if (proj instanceof SpellProjectileEntity sp && caster != null && sp.getOwner() == caster) continue;
            Vec3 v = proj.getDeltaMovement();
            proj.setDeltaMovement(v.scale(-1.2D));
            if (caster != null) {
                proj.setOwner(caster);
            }
            proj.hasImpulse = true;
            level.sendParticles(ParticleTypes.END_ROD, proj.getX(), proj.getY(), proj.getZ(), 4, 0.08D, 0.08D, 0.08D, 0.03D);
        }
    }

    public static void pullProjectilesToward(ServerLevel level, @Nullable LivingEntity caster, AABB box, Vec3 targetPos) {
        for (Projectile proj : level.getEntitiesOfClass(Projectile.class, box)) {
            if (caster != null && proj.getOwner() == caster) continue;
            Vec3 toCenter = targetPos.subtract(proj.position());
            if (toCenter.lengthSqr() > 1.0E-4D) {
                proj.setDeltaMovement(toCenter.normalize().scale(Math.max(0.85D, proj.getDeltaMovement().length())));
                proj.hasImpulse = true;
            }
        }
    }

    public static void purgeBeneficialBuffs(LivingEntity target) {
        for (MobEffectInstance inst : new ArrayList<>(target.getActiveEffects())) {
            if (inst.getEffect().value().isBeneficial()) {
                target.removeEffect(inst.getEffect());
            }
        }
    }

    public static void disableShield(LivingEntity target, int cooldownTicks) {
        if (target.isBlocking()) {
            if (target instanceof Player p && !p.getUseItem().isEmpty()) {
                p.getCooldowns().addCooldown(p.getUseItem(), cooldownTicks);
            }
            target.stopUsingItem();
        }
    }

    public static void shedMobAggro(ServerLevel level, LivingEntity caster, Vec3 center, double radius) {
        AABB box = new AABB(center.x - radius, center.y - 8.0D, center.z - radius,
                center.x + radius, center.y + 8.0D, center.z + radius);
        for (Mob mob : level.getEntitiesOfClass(Mob.class, box)) {
            if (mob.getTarget() == caster) {
                mob.setTarget(null);
            }
            if (mob.getLastHurtByMob() == caster) {
                mob.setLastHurtByMob(null);
            }
        }
    }
}
