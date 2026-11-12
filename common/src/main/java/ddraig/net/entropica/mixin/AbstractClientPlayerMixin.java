package ddraig.net.entropica.mixin;

import ddraig.net.entropica.item.AstrolabeItem;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin {

    @Inject(method = "getFieldOfViewModifier", at = @At("RETURN"), cancellable = true, require = 0)
    private void entropica$modifyLookingGlassFov(CallbackInfoReturnable<Float> cir) {
        Player player = (Player) (Object) this;
        if (AstrolabeItem.isScoping(player)) {
            cir.setReturnValue(cir.getReturnValue() * 0.1F);
        }
    }
}
