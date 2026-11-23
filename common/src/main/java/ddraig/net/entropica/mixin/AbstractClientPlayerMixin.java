package ddraig.net.entropica.mixin;

import ddraig.net.entropica.client.renderer.DoubleVisionClientRenderer;
import ddraig.net.entropica.item.AstrolabeItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
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
        float fov = cir.getReturnValue();
        boolean modified = false;

        if (AstrolabeItem.isScoping(player)) {
            fov *= 0.1F;
            modified = true;
        }
        if (DoubleVisionClientRenderer.isDoubleVisionActive(player)) {
            float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks();
            float time = (player.level().getGameTime() % 72000L) + partialTick;
            fov *= (1.0F + 0.035F * Mth.sin(time * 0.22F));
            modified = true;
        }
        if (modified) {
            cir.setReturnValue(fov);
        }
    }
}
