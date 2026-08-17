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

public class LookingGlassItem extends Item {

    public LookingGlassItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.playSound(SoundEvents.SPYGLASS_USE, 1.0F, 1.0F);
        player.awardStat(Stats.ITEM_USED.get(this));

        if (level.isClientSide()) {
            SkyLookingGlassScreen.open();
        }
        return InteractionResult.SUCCESS;
    }
}
