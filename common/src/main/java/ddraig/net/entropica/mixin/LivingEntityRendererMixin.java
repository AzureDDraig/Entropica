package ddraig.net.entropica.mixin;

import ddraig.net.entropica.gravity.GravityApi;
import ddraig.net.entropica.registry.ModEffects;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> {

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    private void entropica$applyInvertedGravityVisual(T entity, S state, float partialTick, CallbackInfo ci) {
        if (GravityApi.isInverted(entity)) {
            state.isUpsideDown = true;
        } else {
            MobEffectInstance effect = entity.getEffect(ModEffects.WEIGHTLESSNESS);
            if (effect != null && effect.getAmplifier() >= 2) {
                state.isUpsideDown = true;
            }
        }
    }
}
