package ddraig.net.entropica.mixin;

import ddraig.net.entropica.client.input.FirmamentWeaverClientHandler;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class FabricMouseScrollMixin {

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void entropica$onMouseScroll(long window, double xoffset, double yoffset, CallbackInfo ci) {
        if (yoffset != 0 && FirmamentWeaverClientHandler.handleMouseScroll(yoffset)) {
            ci.cancel();
        }
    }
}
