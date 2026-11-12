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
        if (this.minecraft.player != null) {
            if (AstrolabeItem.isScoping(this.minecraft.player)) {
                this.accumulatedDX *= 0.15;
                this.accumulatedDY *= 0.15;
            }

            float roll = ddraig.net.entropica.client.camera.GravityCameraHandler.getRoll(1.0f);
            if (Math.abs(roll) > 0.05f) {
                double rad = Math.toRadians(roll);
                double cos = Math.cos(rad);
                double sin = Math.sin(rad);
                double origDX = this.accumulatedDX;
                double origDY = this.accumulatedDY;

                // Rotate mouse input delta by camera roll angle so looking left/right and up/down matches the rotated screen view
                this.accumulatedDX = origDX * cos - origDY * sin;
                this.accumulatedDY = origDX * sin + origDY * cos;
            }
        }
    }
}
