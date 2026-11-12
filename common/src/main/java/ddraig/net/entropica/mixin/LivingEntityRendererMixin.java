package ddraig.net.entropica.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import ddraig.net.entropica.client.gravity.GravitonSolesClientHandler;
import ddraig.net.entropica.gravity.GravityApi;
import ddraig.net.entropica.registry.ModEffects;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.WeakHashMap;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> {

    private static final WeakHashMap<LivingEntityRenderState, Direction> ENTROPICA$WALL_ROTATION_MAP = new WeakHashMap<>();

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    private void entropica$applyInvertedGravityVisual(T entity, S state, float partialTick, CallbackInfo ci) {
        if (GravityApi.isInverted(entity)) {
            state.isUpsideDown = true;
            ENTROPICA$WALL_ROTATION_MAP.remove(state);
        } else {
            MobEffectInstance effect = entity.getEffect(ModEffects.WEIGHTLESSNESS);
            if (effect != null && effect.getAmplifier() >= 2) {
                state.isUpsideDown = true;
                ENTROPICA$WALL_ROTATION_MAP.remove(state);
            } else if (GravityApi.hasGravitonSoles(entity)) {
                Direction wallDir = GravitonSolesClientHandler.getWallDirection(entity.level(), entity);
                if (wallDir != null) {
                    ENTROPICA$WALL_ROTATION_MAP.put(state, wallDir);
                } else {
                    ENTROPICA$WALL_ROTATION_MAP.remove(state);
                }
            } else {
                ENTROPICA$WALL_ROTATION_MAP.remove(state);
            }
        }
    }

    @Inject(method = "setupRotations", at = @At("TAIL"))
    private void entropica$applyWallRotation(S state, PoseStack poseStack, float bodyRot, float scale, CallbackInfo ci) {
        Direction wallDir = ENTROPICA$WALL_ROTATION_MAP.get(state);
        if (wallDir != null) {
            poseStack.translate(0.0f, state.boundingBoxHeight * 0.5f, 0.0f);
            if (wallDir == Direction.NORTH) {
                poseStack.mulPose(Axis.XP.rotationDegrees(-90.0f));
            } else if (wallDir == Direction.SOUTH) {
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));
            } else if (wallDir == Direction.WEST) {
                poseStack.mulPose(Axis.ZP.rotationDegrees(90.0f));
            } else if (wallDir == Direction.EAST) {
                poseStack.mulPose(Axis.ZP.rotationDegrees(-90.0f));
            }
            poseStack.translate(0.0f, -state.boundingBoxHeight * 0.5f, 0.0f);
        }
    }
}
