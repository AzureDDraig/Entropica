package ddraig.net.entropica.event;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;

@EventBusSubscriber(modid = Entropica.MODID)
public class MobDropHandler {

    @SubscribeEvent
    public static void onMobDeath(LivingDeathEvent event) {
        Entity entity = event.getEntity();
        if (entity.level().isClientSide()) return;

        // Pass all 3 tiers into the helper method
        if (entity instanceof Zombie || entity instanceof Skeleton || entity instanceof WitherSkeleton || entity instanceof Phantom) {
            tryDropTieredEssence(entity, ModItems.WEAK_UNDEAD_ESSENCE.get(), ModItems.AVERAGE_UNDEAD_ESSENCE.get(), ModItems.STRONG_UNDEAD_ESSENCE.get());
        }
        if (entity instanceof Drowned || entity instanceof Guardian || entity instanceof ElderGuardian) {
            tryDropTieredEssence(entity, ModItems.WEAK_WATER_ESSENCE.get(), ModItems.AVERAGE_WATER_ESSENCE.get(), ModItems.STRONG_WATER_ESSENCE.get());
        }
        if (entity instanceof ZombifiedPiglin || entity instanceof Piglin || entity instanceof Blaze || entity instanceof MagmaCube || entity instanceof Ghast) {
            tryDropTieredEssence(entity, ModItems.WEAK_NETHER_ESSENCE.get(), ModItems.AVERAGE_NETHER_ESSENCE.get(), ModItems.STRONG_NETHER_ESSENCE.get());
        }
        if (entity instanceof Creeper || entity instanceof Spider || entity instanceof CaveSpider) {
            tryDropTieredEssence(entity, ModItems.WEAK_CHIMERA_ESSENCE.get(), ModItems.AVERAGE_CHIMERA_ESSENCE.get(), ModItems.STRONG_CHIMERA_ESSENCE.get());
        }
        if (entity instanceof Husk) {
            tryDropTieredEssence(entity, ModItems.WEAK_ARID_ESSENCE.get(), ModItems.AVERAGE_ARID_ESSENCE.get(), ModItems.STRONG_ARID_ESSENCE.get());
        }
        if (entity instanceof Stray) {
            tryDropTieredEssence(entity, ModItems.WEAK_FROZEN_ESSENCE.get(), ModItems.AVERAGE_FROZEN_ESSENCE.get(), ModItems.STRONG_FROZEN_ESSENCE.get());
        }
        if (entity instanceof EnderMan || entity instanceof Endermite || entity instanceof Shulker || entity instanceof EnderDragon) {
            tryDropTieredEssence(entity, ModItems.WEAK_VOID_ESSENCE.get(), ModItems.AVERAGE_VOID_ESSENCE.get(), ModItems.STRONG_VOID_ESSENCE.get());
        }
        if (entity instanceof Breeze) {
            tryDropTieredEssence(entity, ModItems.WEAK_AIR_ESSENCE.get(), ModItems.AVERAGE_AIR_ESSENCE.get(), ModItems.STRONG_AIR_ESSENCE.get());
        }
        if (entity instanceof Slime || entity instanceof Silverfish) {
            tryDropTieredEssence(entity, ModItems.WEAK_EARTH_ESSENCE.get(), ModItems.AVERAGE_EARTH_ESSENCE.get(), ModItems.STRONG_EARTH_ESSENCE.get());
        }
    }

    @SubscribeEvent
    public static void onExperienceDrop(LivingExperienceDropEvent event) {
        if (event.getEntity().getPersistentData().getBoolean("EntropicaEssenceDropped").orElse(false)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getEntity() instanceof ItemEntity itemEntity) {
            String itemName = itemEntity.getItem().getItem().toString();
            // Checking if the item registry name contains "_essence" handles all 27 elements beautifully
            if (itemName.contains("_essence")) {
                itemEntity.setNoGravity(true);
                itemEntity.setInvulnerable(true);
                itemEntity.setGlowingTag(true);
            }
        }
    }

    private static void tryDropTieredEssence(Entity entity, Item weak, Item average, Item strong) {
        float dropChance = 0.10f;

        if (entity.level().random.nextFloat() <= dropChance) {
            // Pick the tier: 85% Weak, 10% Average, 5% Strong
            float tierRoll = entity.level().random.nextFloat();
            Item dropItem = weak; // Default to weak

            if (tierRoll > 0.85f && tierRoll <= 0.95f) dropItem = average;
            else if (tierRoll > 0.95f) dropItem = strong;

            ItemEntity drop = new ItemEntity(
                    entity.level(), entity.getX(), entity.getY() + 0.5D, entity.getZ(),
                    new ItemStack(dropItem)
            );

            drop.setNoGravity(true);
            drop.setDeltaMovement(0, 0, 0);
            drop.setInvulnerable(true);
            drop.setGlowingTag(true);

            entity.level().addFreshEntity(drop);
            entity.getPersistentData().putBoolean("EntropicaEssenceDropped", true);
        }
    }
}