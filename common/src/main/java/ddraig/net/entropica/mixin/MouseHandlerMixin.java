package ddraig.net.entropica.mixin;

import ddraig.net.entropica.item.AstrolabeItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    private double accumulatedDX;

    @Shadow
    private double accumulatedDY;

    @Inject(method = "turnPlayer", at = @At("HEAD"))
    private void entropica$dampenMouseDelta(double d, CallbackInfo ci) {
        if (this.minecraft.player != null && AstrolabeItem.isScoping(this.minecraft.player)) {
            this.accumulatedDX *= 0.15;
            this.accumulatedDY *= 0.15;
        }
    }
}
