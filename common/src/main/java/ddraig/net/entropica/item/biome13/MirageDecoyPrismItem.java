package ddraig.net.entropica.item.biome13;

import ddraig.net.entropica.entity.projectile.MirageDecoyPrismEntity;
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
 * Mirage Decoy Prism — throwable Mirage Materia crystal prism that projects a distracting illusion on impact.
 */
public class MirageDecoyPrismItem extends Item implements ProjectileItem {

    public MirageDecoyPrismItem(Properties properties) {
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
                0.55F,
                0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
        );
        player.getCooldowns().addCooldown(stack, 20);

        if (!level.isClientSide()) {
            MirageDecoyPrismEntity prism = new MirageDecoyPrismEntity(level, player, stack.copyWithCount(1));
            prism.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.35F, 1.0F);
            level.addFreshEntity(prism);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        if (!player.isCreative()) {
            stack.shrink(1);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        return new MirageDecoyPrismEntity(level, pos.x(), pos.y(), pos.z(), stack.copyWithCount(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("Thrown: Shatters into a dazzling Mirage Materia decoy burst that draws nearby hostile attention.")
                .withStyle(ChatFormatting.GOLD));
        tooltipComponents.accept(Component.literal("Impact: Dazes nearby foes with Double Vision and prismatic light.")
                .withStyle(ChatFormatting.AQUA));
    }
}
