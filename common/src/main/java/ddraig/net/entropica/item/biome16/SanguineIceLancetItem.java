package ddraig.net.entropica.item.biome16;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Consumer;

/**
 * Sanguine Ice-Lancet — swift Blood Glass and Sanguine Heart-Gel surgical ice blade (192 durability).
 * Carves all #minecraft:ice blocks cleanly intact at 12x speed without Silk Touch, and chills foes
 * with Slowness I + frostbite while siphoning 1.5 HP back to the wielder on hit.
 */
public class SanguineIceLancetItem extends Item {

    public SanguineIceLancetItem(Properties properties) {
        super(properties
                .stacksTo(1)
                .durability(192)
                .enchantable(14)
                .component(DataComponents.WEAPON, new Weapon(1))
                .attributes(createAttributes()));
    }

    public static ItemAttributeModifiers createAttributes() {
        return ItemAttributeModifiers.builder()
                .add(
                        Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 4.5D, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND
                )
                .add(
                        Attributes.ATTACK_SPEED,
                        new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -2.0D, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND
                )
                .build();
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (state.is(BlockTags.ICE)) {
            return 12.0F;
        }
        if (state.is(Blocks.COBWEB)
                || state.is(BlockTags.SWORD_INSTANTLY_MINES)
                || state.is(BlockTags.SWORD_EFFICIENT)) {
            return 4.0F;
        }
        return 1.0F;
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return state.is(BlockTags.ICE);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 0), attacker);
        int maxFreeze = target.getTicksRequiredToFreeze() + 80;
        target.setTicksFrozen(Math.min(maxFreeze, target.getTicksFrozen() + 60));

        if (attacker != null && attacker.isAlive()) {
            attacker.heal(1.5F);
        }

        if (target.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, target.blockPosition(), SoundEvents.GLASS_HIT, SoundSource.PLAYERS, 0.7F, 1.35F);
            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                    target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ(),
                    5, 0.2D, 0.25D, 0.2D, 0.03D);
            serverLevel.sendParticles(new DustParticleOptions(0xD42A48, 0.85F),
                    target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ(),
                    5, 0.2D, 0.25D, 0.2D, 0.02D);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("Ice Cutter: Rapidly carves any Ice or Sanguine Ice intact without Silk Touch.")
                .withStyle(ChatFormatting.GOLD));
        tooltipComponents.accept(Component.literal("On Hit: Inflicts Slowness I & frostbite (3s) and restores 1.5 HP to the wielder.")
                .withStyle(ChatFormatting.AQUA));
    }
}
