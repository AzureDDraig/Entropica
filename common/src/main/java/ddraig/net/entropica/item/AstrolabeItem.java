package ddraig.net.entropica.item;

import ddraig.net.entropica.client.gui.SkyLookingGlassScreen;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class AstrolabeItem extends Item {

    public AstrolabeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.playSound(SoundEvents.SPYGLASS_USE, 1.0F, 1.2F);
        player.awardStat(Stats.ITEM_USED.get(this));

        if (level.isClientSide()) {
            SkyLookingGlassScreen.open();
        }
        return InteractionResult.SUCCESS;
    }

    public static boolean isScoping(Player player) {
        if (player == null) return false;
        return player.isUsingItem() && (player.getUseItem().getItem() instanceof AstrolabeItem || player.getUseItem().getItem() instanceof LookingGlassItem);
    }
}
