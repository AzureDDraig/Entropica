package ddraig.net.entropica.mixin;

import ddraig.net.entropica.client.renderer.ArcaneWingsRenderLayer;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Attaches {@link ArcaneWingsRenderLayer} to every {@link AvatarRenderer} (wide & slim player skins)
 * across both NeoForge and Fabric.
 */
@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin<T extends Avatar & ClientAvatarEntity> extends LivingEntityRenderer<T, AvatarRenderState, PlayerModel> {

    protected AvatarRendererMixin(EntityRendererProvider.Context context, PlayerModel model, float shadowRadius) {
        super(context, model, shadowRadius);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void entropica$attachArcaneWingsLayer(EntityRendererProvider.Context context, boolean slim, CallbackInfo ci) {
        this.addLayer(new ArcaneWingsRenderLayer(this));
    }
}
