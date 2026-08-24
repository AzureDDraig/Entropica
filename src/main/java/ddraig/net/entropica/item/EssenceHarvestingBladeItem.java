package ddraig.net.entropica.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class EssenceHarvestingBladeItem extends Item {

    public EssenceHarvestingBladeItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // Apply standard weapon durability damage and vanilla logic first
        super.postHurtEnemy(stack, target, attacker);

        // Check if the target was killed by this specific strike on the server side
        if (target.isDeadOrDying() && target.level() instanceof ServerLevel serverLevel) {

            // Play a magical sound effect so the blade feels uniquely thematic on kills
            serverLevel.playSound(null, target.blockPosition(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.0F, 1.5F);
        }
    }
}