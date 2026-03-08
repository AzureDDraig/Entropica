package ddraig.net.entropica.event;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.registry.ModEnchantments;
import ddraig.net.entropica.registry.ModEntityTags;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.animal.Panda;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.GlowSquid;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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

        boolean holdingBlade = false;
        int reapLevel = 0;

        Entity killer = event.getSource().getEntity();
        if (killer instanceof LivingEntity livingKiller) {
            ItemStack weapon = livingKiller.getMainHandItem();

            if (weapon.is(ModItems.ESSENCE_HARVESTING_BLADE.get())) {
                holdingBlade = true;
            }

            var registryAccess = livingKiller.registryAccess();
            var enchantmentLookup = registryAccess.lookupOrThrow(Registries.ENCHANTMENT);
            var essenceReapHolder = enchantmentLookup.get(ModEnchantments.ESSENCE_REAP);

            if (essenceReapHolder.isPresent()) {
                reapLevel = weapon.getEnchantmentLevel(essenceReapHolder.get());
            }

            if (reapLevel > 0 && entity.level() instanceof ServerLevel serverLevel) {
                serverLevel.playSound(null, entity.blockPosition(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.0F, 1.5F);
            }
        }

        boolean isPassive = entity instanceof Animal || entity instanceof WaterAnimal || entity instanceof AmbientCreature || entity instanceof Squid;
        EntityType<?> type = entity.getType();

        tryEnvironmentalDrop(entity, holdingBlade, reapLevel, isPassive);

        // ==========================================
        // INDEPENDENT ESSENCE CHECKS
        // ==========================================

        if (type.is(ModEntityTags.DROPS_UNDEAD_ESSENCE) || entity instanceof Zombie || entity instanceof Skeleton || entity instanceof WitherSkeleton || entity instanceof Phantom) {
            tryDropTieredEssence(entity, ddraig.net.entropica.api.EssenceType.UNDEAD, holdingBlade, reapLevel, isPassive);
        }

        if (type.is(ModEntityTags.DROPS_WATER_ESSENCE) || entity instanceof Drowned || entity instanceof Guardian || entity instanceof ElderGuardian || entity instanceof WaterAnimal || entity instanceof Squid) {
            tryDropTieredEssence(entity, ddraig.net.entropica.api.EssenceType.WATER, holdingBlade, reapLevel, isPassive);
        }

        if (type.is(ModEntityTags.DROPS_NETHER_ESSENCE) || entity instanceof ZombifiedPiglin || entity instanceof Piglin || entity instanceof Blaze || entity instanceof MagmaCube || entity instanceof Ghast) {
            tryDropTieredEssence(entity, ddraig.net.entropica.api.EssenceType.NETHER, holdingBlade, reapLevel, isPassive);
        }

        if (type.is(ModEntityTags.DROPS_CHIMERA_ESSENCE) || entity instanceof Creeper || entity instanceof Spider || entity instanceof CaveSpider || entity instanceof Axolotl) {
            tryDropTieredEssence(entity, ddraig.net.entropica.api.EssenceType.CHIMERA, holdingBlade, reapLevel, isPassive);
        }

        if (type.is(ModEntityTags.DROPS_ARID_ESSENCE) || entity instanceof Husk) {
            tryDropTieredEssence(entity, ddraig.net.entropica.api.EssenceType.ARID, holdingBlade, reapLevel, isPassive);
        }

        if (type.is(ModEntityTags.DROPS_FROZEN_ESSENCE) || entity instanceof Stray || entity instanceof PolarBear) {
            tryDropTieredEssence(entity, ddraig.net.entropica.api.EssenceType.FROZEN, holdingBlade, reapLevel, isPassive);
        }

        if (type.is(ModEntityTags.DROPS_VOID_ESSENCE) || entity instanceof EnderMan || entity instanceof Endermite || entity instanceof Shulker || entity instanceof EnderDragon) {
            tryDropTieredEssence(entity, ddraig.net.entropica.api.EssenceType.VOID, holdingBlade, reapLevel, isPassive);
        }

        if (type.is(ModEntityTags.DROPS_AIR_ESSENCE) || entity instanceof Breeze || entity instanceof AmbientCreature) {
            tryDropTieredEssence(entity, ddraig.net.entropica.api.EssenceType.AIR, holdingBlade, reapLevel, isPassive);
        }

        if (type.is(ModEntityTags.DROPS_EARTH_ESSENCE) || entity instanceof Slime || entity instanceof Silverfish || entity instanceof Cow || entity instanceof Pig) {
            tryDropTieredEssence(entity, ddraig.net.entropica.api.EssenceType.EARTH, holdingBlade, reapLevel, isPassive);
        }

        if (type.is(ModEntityTags.DROPS_RADIANT_ESSENCE) || entity instanceof GlowSquid) {
            tryDropTieredEssence(entity, ddraig.net.entropica.api.EssenceType.RADIANT, holdingBlade, reapLevel, isPassive);
        }

        if (type.is(ModEntityTags.DROPS_NATURE_ESSENCE) || entity instanceof Ocelot || entity instanceof Parrot || entity instanceof Panda) {
            tryDropTieredEssence(entity, ddraig.net.entropica.api.EssenceType.NATURE, holdingBlade, reapLevel, isPassive);
        }

        // Vanilla hardcodes with variants
        if (entity instanceof Turtle && !type.is(ModEntityTags.DROPS_WATER_ESSENCE) && !type.is(ModEntityTags.DROPS_EARTH_ESSENCE)) {
            ddraig.net.entropica.api.EssenceType dropType = entity.level().random.nextBoolean() ? ddraig.net.entropica.api.EssenceType.WATER : ddraig.net.entropica.api.EssenceType.EARTH;
            tryDropTieredEssence(entity, dropType, holdingBlade, reapLevel, isPassive);
        }
        if (entity instanceof Frog frog && !type.is(ModEntityTags.DROPS_ARID_ESSENCE) && !type.is(ModEntityTags.DROPS_FROZEN_ESSENCE) && !type.is(ModEntityTags.DROPS_NATURE_ESSENCE)) {
            String frogVariant = frog.getVariant().unwrapKey().map(key -> key.location().getPath()).orElse("temperate");
            if (frogVariant.equals("warm")) tryDropTieredEssence(entity, ddraig.net.entropica.api.EssenceType.ARID, holdingBlade, reapLevel, isPassive);
            else if (frogVariant.equals("cold")) tryDropTieredEssence(entity, ddraig.net.entropica.api.EssenceType.FROZEN, holdingBlade, reapLevel, isPassive);
            else tryDropTieredEssence(entity, ddraig.net.entropica.api.EssenceType.NATURE, holdingBlade, reapLevel, isPassive);
        }
        if (entity instanceof Fox fox && !type.is(ModEntityTags.DROPS_FROZEN_ESSENCE) && !type.is(ModEntityTags.DROPS_EARTH_ESSENCE)) {
            if (fox.getVariant() == Fox.Variant.SNOW) tryDropTieredEssence(entity, ddraig.net.entropica.api.EssenceType.FROZEN, holdingBlade, reapLevel, isPassive);
            else tryDropTieredEssence(entity, ddraig.net.entropica.api.EssenceType.EARTH, holdingBlade, reapLevel, isPassive);
        }
        if (entity instanceof Rabbit rabbit && !type.is(ModEntityTags.DROPS_ARID_ESSENCE) && !type.is(ModEntityTags.DROPS_FROZEN_ESSENCE) && !type.is(ModEntityTags.DROPS_EARTH_ESSENCE)) {
            if (rabbit.getVariant() == Rabbit.Variant.GOLD) tryDropTieredEssence(entity, ddraig.net.entropica.api.EssenceType.ARID, holdingBlade, reapLevel, isPassive);
            else if (rabbit.getVariant() == Rabbit.Variant.WHITE || rabbit.getVariant() == Rabbit.Variant.WHITE_SPLOTCHED) tryDropTieredEssence(entity, ddraig.net.entropica.api.EssenceType.FROZEN, holdingBlade, reapLevel, isPassive);
            else tryDropTieredEssence(entity, ddraig.net.entropica.api.EssenceType.EARTH, holdingBlade, reapLevel, isPassive);
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

        // Only apply floating physics to the physical Orbs!
        if (event.getEntity() instanceof ItemEntity itemEntity) {
            if (itemEntity.getItem().getItem() instanceof ddraig.net.entropica.item.EssenceItem) {
                itemEntity.setNoGravity(true);
                itemEntity.setInvulnerable(true);
                itemEntity.setGlowingTag(true);
            }
        }
    }

    private static void tryDropTieredEssence(Entity entity, ddraig.net.entropica.api.EssenceType type, boolean holdingBlade, int reapLevel, boolean isPassive) {
        float dropChance = 0.001f;

        if (reapLevel > 0) {
            dropChance = 0.20f + ((reapLevel - 1) * 0.15f);
        } else if (holdingBlade) {
            dropChance = 0.20f;
        }

        if (isPassive) dropChance /= 2.0f;

        if (entity.level().random.nextFloat() <= dropChance) {
            float tierRoll = entity.level().random.nextFloat();
            Item dropItem = ModItems.WEAK_ESSENCE.get();

            if (isPassive) {
                if (reapLevel == 5) {
                    if (entity instanceof PolarBear) dropItem = ModItems.STRONG_ESSENCE.get();
                    else dropItem = (tierRoll <= 0.10f) ? ModItems.STRONG_ESSENCE.get() : ModItems.AVERAGE_ESSENCE.get();
                } else {
                    float averageChance = (reapLevel > 0) ? (reapLevel - 1) * 0.25f : 0.0f;
                    dropItem = (tierRoll <= averageChance) ? ModItems.AVERAGE_ESSENCE.get() : ModItems.WEAK_ESSENCE.get();

                    if (entity instanceof PolarBear && dropItem == ModItems.WEAK_ESSENCE.get()) dropItem = ModItems.AVERAGE_ESSENCE.get();
                }
            } else {
                if (reapLevel == 5) {
                    if (tierRoll <= 0.30f) dropItem = ModItems.STRONG_ESSENCE.get();
                    else if (tierRoll <= 0.99f) dropItem = ModItems.AVERAGE_ESSENCE.get();
                } else if (reapLevel >= 3) {
                    if (tierRoll <= 0.15f) dropItem = ModItems.STRONG_ESSENCE.get();
                    else if (tierRoll <= 0.60f) dropItem = ModItems.AVERAGE_ESSENCE.get();
                } else if (holdingBlade || reapLevel > 0) {
                    if (tierRoll <= 0.05f) dropItem = ModItems.STRONG_ESSENCE.get();
                    else if (tierRoll <= 0.20f) dropItem = ModItems.AVERAGE_ESSENCE.get();
                } else {
                    if (tierRoll <= 0.10f) dropItem = ModItems.AVERAGE_ESSENCE.get();
                }
            }

            ItemStack dropStack = new ItemStack(dropItem);
            ddraig.net.entropica.item.EssenceItem.setEssenceType(dropStack, type);
            spawnEssenceDrop(entity, dropStack);
        }
    }

    private static boolean tryEnvironmentalDrop(Entity entity, boolean holdingBlade, int reapLevel, boolean isPassive) {
        float envChance = 0.0005f;

        if (reapLevel > 0) {
            envChance = (0.20f + ((reapLevel - 1) * 0.15f)) / 2.0f;
        } else if (holdingBlade) {
            envChance = 0.10f;
        }

        if (isPassive) envChance /= 2.0f;

        if (entity.level().random.nextFloat() <= envChance) {
            Level level = entity.level();

            ddraig.net.entropica.api.EssenceType envType = null;

            if (level.dimension() == Level.NETHER) {
                envType = ddraig.net.entropica.api.EssenceType.NETHER;
            } else if (level.dimension() == Level.END) {
                envType = ddraig.net.entropica.api.EssenceType.VOID;
            } else if (level.isWaterAt(entity.blockPosition())) {
                envType = ddraig.net.entropica.api.EssenceType.WATER;
            } else if (level.getBiome(entity.blockPosition()).value().getBaseTemperature() < 0.15f) {
                envType = ddraig.net.entropica.api.EssenceType.FROZEN;
            } else if (level.getBiome(entity.blockPosition()).value().getBaseTemperature() > 1.5f) {
                envType = ddraig.net.entropica.api.EssenceType.ARID;
            }

            if (envType != null) {
                Item dropItem = ModItems.WEAK_ESSENCE.get();

                if (isPassive) {
                    if (reapLevel == 5) {
                        if (entity instanceof PolarBear) dropItem = ModItems.STRONG_ESSENCE.get();
                        else dropItem = (level.random.nextFloat() <= 0.10f) ? ModItems.STRONG_ESSENCE.get() : ModItems.AVERAGE_ESSENCE.get();
                    } else {
                        float averageChance = (reapLevel > 0) ? (reapLevel - 1) * 0.25f : 0.0f;
                        dropItem = (level.random.nextFloat() <= averageChance) ? ModItems.AVERAGE_ESSENCE.get() : ModItems.WEAK_ESSENCE.get();
                        if (entity instanceof PolarBear && dropItem == ModItems.WEAK_ESSENCE.get()) dropItem = ModItems.AVERAGE_ESSENCE.get();
                    }
                } else {
                    float tierRoll = level.random.nextFloat();
                    if (reapLevel == 5) {
                        if (tierRoll <= 0.30f) dropItem = ModItems.STRONG_ESSENCE.get();
                        else if (tierRoll <= 0.99f) dropItem = ModItems.AVERAGE_ESSENCE.get();
                    } else if (reapLevel >= 3) {
                        if (tierRoll <= 0.15f) dropItem = ModItems.STRONG_ESSENCE.get();
                        else if (tierRoll <= 0.60f) dropItem = ModItems.AVERAGE_ESSENCE.get();
                    } else if (holdingBlade || reapLevel > 0) {
                        if (tierRoll <= 0.05f) dropItem = ModItems.STRONG_ESSENCE.get();
                        else if (tierRoll <= 0.20f) dropItem = ModItems.AVERAGE_ESSENCE.get();
                    } else {
                        if (tierRoll <= 0.10f) dropItem = ModItems.AVERAGE_ESSENCE.get();
                    }
                }

                ItemStack dropStack = new ItemStack(dropItem);
                ddraig.net.entropica.item.EssenceItem.setEssenceType(dropStack, envType);
                spawnEssenceDrop(entity, dropStack);
                return true;
            }
        }
        return false;
    }

    private static void spawnEssenceDrop(Entity entity, ItemStack dropStack) {
        ItemEntity drop = new ItemEntity(
                entity.level(), entity.getX(), entity.getY() + 0.5D, entity.getZ(),
                dropStack
        );

        drop.setNoGravity(true);
        drop.setDeltaMovement(0, 0, 0);
        drop.setInvulnerable(true);
        drop.setGlowingTag(true);

        entity.level().addFreshEntity(drop);
        entity.getPersistentData().putBoolean("EntropicaEssenceDropped", true);
    }
}