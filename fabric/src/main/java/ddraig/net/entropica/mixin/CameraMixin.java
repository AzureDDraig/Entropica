package ddraig.net.entropica.mixin;

import ddraig.net.entropica.client.camera.GravityCameraHandler;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow
    @Final
    private Quaternionf rotation;

    @Inject(method = "setup", at = @At("TAIL"))
    private void entropica$applyGravityCameraAngles(BlockGetter level, Entity entity, boolean detached, boolean thirdPersonReverse, float partialTick, CallbackInfo ci) {
        float yawSlant = GravityCameraHandler.getYawSlant(partialTick);
        float pitchSlant = GravityCameraHandler.getPitchSlant(partialTick);
        float roll = GravityCameraHandler.getRoll(partialTick);

        if (Math.abs(yawSlant) > 0.001f) {
            this.rotation.rotateY((float) Math.toRadians(yawSlant));
        }
        if (Math.abs(pitchSlant) > 0.001f) {
            this.rotation.rotateX((float) Math.toRadians(pitchSlant));
        }
        if (Math.abs(roll) > 0.001f) {
            this.rotation.rotateZ((float) Math.toRadians(roll));
        }
    }
}
