package ddraig.net.entropica.item.biome12;

import ddraig.net.entropica.entity.projectile.GlazedSporeBombEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

/**
 * Glazed Spore Bomb — throwable frost-encased Spore and Glacial Materia bursting pod.
 */
public class GlazedSporeBombItem extends Item implements ProjectileItem {

    public GlazedSporeBombItem(Properties properties) {
        super(properties.stacksTo(16));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.SNOWBALL_THROW,
                SoundSource.NEUTRAL,
                0.5F,
                0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
        );
        player.getCooldowns().addCooldown(stack, 20);

        if (!level.isClientSide()) {
            GlazedSporeBombEntity bomb = new GlazedSporeBombEntity(level, player, stack.copyWithCount(1));
            bomb.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.35F, 1.0F);
            level.addFreshEntity(bomb);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        if (!player.isCreative()) {
            stack.shrink(1);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        return new GlazedSporeBombEntity(level, pos.x(), pos.y(), pos.z(), stack.copyWithCount(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("Thrown: Bursts into a chilled golden spore cloud on impact.")
                .withStyle(ChatFormatting.YELLOW));
        tooltipComponents.accept(Component.literal("Impact: Inflicts Slowness, Poison, and frost buildup on caught foes.")
                .withStyle(ChatFormatting.AQUA));
    }
}
