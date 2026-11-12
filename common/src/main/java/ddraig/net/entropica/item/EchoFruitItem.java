package ddraig.net.entropica.item;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.registry.ModItems;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EchoFruitItem extends Item {
    private static final Map<UUID, Long> LAST_EAT_TIME = new HashMap<>();
    private static final Map<UUID, EssenceType> LAST_EAT_TYPE = new HashMap<>();

    public EchoFruitItem(Properties properties) {
        super(properties.food(new FoodProperties.Builder().nutrition(5).saturationModifier(0.6f).alwaysEdible().build()));
    }

    public static ItemStack createFruitForAspect(EssenceType aspect) {
        ItemStack stack = new ItemStack(ModItems.ECHO_FRUIT.get());
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(aspect.getColorCode() + aspect.getDisplayName() + " Echo Fruit"));
        return stack;
    }

    public static EssenceType getAspectFromStack(ItemStack stack) {
        if (stack.has(DataComponents.CUSTOM_NAME)) {
            String name = stack.get(DataComponents.CUSTOM_NAME).getString();
            for (EssenceType type : EssenceType.values()) {
                if (!type.isFragment() && name.contains(type.getDisplayName())) {
                    return type;
                }
            }
        }
        return EssenceType.REGULAR;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide() && entity instanceof Player player) {
            EssenceType aspect = getAspectFromStack(stack);
            long currentTime = level.getGameTime();
            UUID uuid = player.getUUID();

            Long lastTime = LAST_EAT_TIME.get(uuid);
            if (lastTime != null && (currentTime - lastTime) < 1200) {
                applyMateriaToxicity(player, aspect);
            } else {
                applyAspectBuffs(player, aspect);
            }

            LAST_EAT_TIME.put(uuid, currentTime);
            LAST_EAT_TYPE.put(uuid, aspect);
        }

        return result;
    }

    private void applyAspectBuffs(Player player, EssenceType aspect) {
        switch (aspect) {
            case FROZEN -> {
                player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 600, 1));
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0));
            }
            case NETHER -> {
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 1200, 1));
                player.addEffect(new MobEffectInstance(MobEffects.HASTE, 600, 1));
            }
            case NATURE -> {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 400, 1));
                player.removeEffect(MobEffects.POISON);
                player.removeEffect(MobEffects.WITHER);
            }
            case VOID -> {
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 600, 1));
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 1200, 0));
            }
            case LIGHTNING -> {
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, 600, 1));
                player.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 600, 1));
            }
            case WATER -> {
                player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 1200, 0));
                player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 600, 0));
            }
            case UNDEAD -> {
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 600, 0));
                player.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, 600, 1));
            }
            case RADIANT -> {
                player.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, 1200, 1));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 1));
            }
            default -> {
                player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 600, 0));
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, 600, 0));
            }
        }
    }

    private void applyMateriaToxicity(Player player, EssenceType aspect) {
        player.displayClientMessage(Component.literal("§c⚠ Warning: Materia Toxicity Overload! (" + aspect.getDisplayName() + ")"), false);
        switch (aspect) {
            case FROZEN -> {
                player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 400, 2));
                player.hurt(player.damageSources().freeze(), 6.0f);
            }
            case NETHER -> {
                player.igniteForSeconds(8);
                player.hurt(player.damageSources().inFire(), 6.0f);
            }
            case NATURE -> {
                player.addEffect(new MobEffectInstance(MobEffects.POISON, 300, 2));
                player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 300, 1));
            }
            case VOID -> {
                player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 160, 2));
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0));
            }
            case LIGHTNING -> {
                player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 200, 4));
                player.hurt(player.damageSources().lightningBolt(), 8.0f);
            }
            case WATER -> {
                player.setAirSupply(0);
                player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 300, 0));
            }
            case UNDEAD -> {
                player.addEffect(new MobEffectInstance(MobEffects.WITHER, 300, 2));
                player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 400, 2));
            }
            case RADIANT -> {
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 300, 1));
                player.hurt(player.damageSources().magic(), 6.0f);
            }
            default -> {
                player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 300, 1));
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 400, 1));
            }
        }
    }

    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        EssenceType aspect = getAspectFromStack(stack);
        tooltip.add(Component.literal(aspect.getColorCode() + "Aspect: " + aspect.getDisplayName()));
        tooltip.add(Component.literal("§8⚠ Eating multiple within 60s causes Materia Toxicity!"));
    }
}
