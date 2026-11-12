package ddraig.net.entropica.event;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.entity.SpellEidolonEntity;
import ddraig.net.entropica.registry.ModItems;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.LootEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

/**
 * Injects the 13 Scholar's Journals (Volumes I - XIII) into structure chest loot tables
 * and thematic mob/boss drops across Overworld, Nether, End, and Entropica encounters.
 */
public final class CelestialJournalLootHandler {

    private static final int[] ARCHIVE_VOLUMES = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13};
    private static final int[] OVERWORLD_EXPEDITION_VOLUMES = {1, 2, 3, 4, 5, 7, 9, 11};
    private static final int[] END_AND_ABYSS_VOLUMES = {6, 7, 8, 9, 10, 12, 13};
    private static final int[] CASTER_MOB_VOLUMES = {1, 2, 3, 4, 5, 6, 11};
    private static final int[] COSMIC_MOB_VOLUMES = {6, 7, 8, 9, 12};
    private static final int[] APEX_BOSS_VOLUMES = {8, 10, 12, 13};

    private CelestialJournalLootHandler() {
    }

    public static void register() {
        LootEvent.MODIFY_LOOT_TABLE.register((key, context, builtin) -> {
            if (!builtin || key == null) return;
            ResourceLocation id = key.location();
            String path = id.getPath();

            if (!path.startsWith("chests/") && !path.contains("chest") && !path.contains("library") && !path.contains("vault")) {
                return;
            }

            // 1. Libraries, Mansions, Ancient Cities & Modded Archives/Observatories
            if (path.contains("stronghold_library")) {
                LootPool.Builder pool = LootPool.lootPool()
                        .setRolls(UniformGenerator.between(1.0F, 2.0F))
                        .when(LootItemRandomChanceCondition.randomChance(0.75F));
                addJournalEntries(pool, ARCHIVE_VOLUMES);
                context.addPool(pool);
            } else if (path.contains("ancient_city") || path.contains("woodland_mansion")
                    || (Entropica.MODID.equals(id.getNamespace()) && (path.contains("library") || path.contains("observatory") || path.contains("spire") || path.contains("sanctum")))) {
                LootPool.Builder pool = LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .when(LootItemRandomChanceCondition.randomChance(0.55F));
                addJournalEntries(pool, ARCHIVE_VOLUMES);
                context.addPool(pool);
            }
            // 2. End Cities, Bastions, Nether Fortresses & Trial Chambers
            else if (path.contains("end_city_treasure") || path.contains("bastion_treasure")
                    || path.contains("nether_bridge") || path.contains("trial_chambers/reward_unique")
                    || path.contains("trial_chambers/reward_ominous")) {
                LootPool.Builder pool = LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .when(LootItemRandomChanceCondition.randomChance(0.40F));
                addJournalEntries(pool, END_AND_ABYSS_VOLUMES);
                context.addPool(pool);
            }
            // 3. Overworld Temples, Dungeons, Mineshafts, Shipwrecks, Cartographers & Ruins
            else if (path.contains("desert_pyramid") || path.contains("jungle_temple")
                    || path.contains("simple_dungeon") || path.contains("abandoned_mineshaft")
                    || path.contains("shipwreck_map") || path.contains("igloo_chest")
                    || path.contains("pillager_outpost") || path.contains("ruined_portal")
                    || path.contains("buried_treasure") || path.contains("village_cartographer")
                    || path.contains("village_temple") || path.contains("trail_ruins")) {
                LootPool.Builder pool = LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .when(LootItemRandomChanceCondition.randomChance(0.30F));
                addJournalEntries(pool, OVERWORLD_EXPEDITION_VOLUMES);
                context.addPool(pool);
            }
        });

        EntityEvent.LIVING_DEATH.register((entity, source) -> {
            if (!entity.level().isClientSide() && entity.level() instanceof ServerLevel serverLevel) {
                tryDropJournalFromMob(serverLevel, entity, source);
            }
            return EventResult.pass();
        });
    }

    private static void addJournalEntries(LootPool.Builder pool, int[] volumes) {
        for (int vol : volumes) {
            Item item = ModItems.getCelestialJournal(vol).get();
            int weight = (vol >= 10) ? 6 : 10;
            pool.add(LootItem.lootTableItem(item).setWeight(weight));
        }
    }

    private static void tryDropJournalFromMob(ServerLevel level, LivingEntity mob, DamageSource source) {
        if (mob instanceof Player || mob instanceof SpellEidolonEntity) {
            return;
        }

        Player killerPlayer = resolveKillerPlayer(level, mob, source);
        if (killerPlayer == null) {
            return;
        }

        int looting = getLootingLevel(level, killerPlayer);
        RandomSource rng = level.getRandom();

        // 1. Major Bosses: Guaranteed Journal Drop
        if (mob instanceof EnderDragon || mob instanceof WitherBoss || mob instanceof Warden) {
            int vol = APEX_BOSS_VOLUMES[rng.nextInt(APEX_BOSS_VOLUMES.length)];
            spawnJournalDrop(level, mob, vol);
            return;
        }
        if (mob instanceof ElderGuardian) {
            int[] seaVols = {4, 5, 7, 12};
            spawnJournalDrop(level, mob, seaVols[rng.nextInt(seaVols.length)]);
            return;
        }

        // 2. Entropica Custom Creatures (e.g. Gemini Goat Mage & Astral/Void Denizens)
        ResourceLocation typeId = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
        if (typeId != null && Entropica.MODID.equals(typeId.getNamespace())) {
            float chance = typeId.getPath().contains("mage") || typeId.getPath().contains("boss")
                    ? 0.45F + 0.08F * looting
                    : 0.12F + 0.03F * looting;
            if (rng.nextFloat() < chance) {
                int vol = ARCHIVE_VOLUMES[rng.nextInt(ARCHIVE_VOLUMES.length)];
                spawnJournalDrop(level, mob, vol);
            }
            return;
        }

        // 3. Scholarly & Arcane Casters (Evoker, Witch, Illusioner)
        if (mob instanceof Evoker || mob instanceof Witch || mob instanceof Illusioner) {
            float chance = 0.10F + 0.03F * looting;
            if (rng.nextFloat() < chance) {
                int vol = CASTER_MOB_VOLUMES[rng.nextInt(CASTER_MOB_VOLUMES.length)];
                spawnJournalDrop(level, mob, vol);
            }
            return;
        }

        // 4. Cosmic, Aerial & Abyssal Denizens (Enderman, Phantom, Breeze, Blaze, Piglin Brute)
        if (mob instanceof EnderMan || mob instanceof Phantom || mob instanceof Breeze
                || mob instanceof Blaze || mob instanceof PiglinBrute) {
            float chance = 0.05F + 0.02F * looting;
            if (rng.nextFloat() < chance) {
                int vol = COSMIC_MOB_VOLUMES[rng.nextInt(COSMIC_MOB_VOLUMES.length)];
                spawnJournalDrop(level, mob, vol);
            }
        }
    }

    private static Player resolveKillerPlayer(ServerLevel level, LivingEntity mob, DamageSource source) {
        if (source != null && source.getEntity() instanceof Player p) {
            return p;
        }
        if (source != null && source.getEntity() instanceof SpellEidolonEntity eidolon) {
            if (eidolon.resolveSummoner(level) instanceof Player owner) {
                return owner;
            }
        }
        return mob.getKillCredit() instanceof Player credit ? credit : null;
    }

    private static int getLootingLevel(ServerLevel level, Player player) {
        try {
            var lookup = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            var holderOpt = lookup.get(Enchantments.LOOTING);
            if (holderOpt.isPresent()) {
                return EnchantmentHelper.getItemEnchantmentLevel(holderOpt.get(), player.getMainHandItem());
            }
        } catch (Throwable ignored) {
        }
        return 0;
    }

    private static void spawnJournalDrop(ServerLevel level, LivingEntity mob, int volume) {
        ItemStack stack = new ItemStack(ModItems.getCelestialJournal(volume).get());
        ItemEntity drop = new ItemEntity(level, mob.getX(), mob.getY() + 0.35D, mob.getZ(), stack);
        drop.setDefaultPickUpDelay();
        level.addFreshEntity(drop);
    }
}
