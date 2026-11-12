package ddraig.net.entropica.mixin;

import ddraig.net.entropica.util.EntityHelper;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    
    @Inject(method = "getExperienceReward", at = @At("HEAD"), cancellable = true)
    private void cancelExperienceReward(CallbackInfoReturnable<Integer> info) {
        if (EntityHelper.hasEssenceDropped((LivingEntity) (Object) this)) {
            info.setReturnValue(0);
        }
    }
}
