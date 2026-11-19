package ddraig.net.entropica.mixin;

import ddraig.net.entropica.client.worldgen.BiomeAtmosphereClientManager;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.DimensionOrBossFogEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DimensionOrBossFogEnvironment.class)
public abstract class DimensionOrBossFogEnvironmentMixin {

    @Inject(method = "setupFog", at = @At("RETURN"))
    private void entropica$applyCustomNetherBiomeFog(
            FogData fogData,
            Entity entity,
            BlockPos pos,
            ClientLevel level,
            float renderDistanceBlocks,
            DeltaTracker deltaTracker,
            CallbackInfo ci
    ) {
        BiomeAtmosphereClientManager.applyCustomFogDistances(
                fogData,
                entity,
                pos,
                level,
                renderDistanceBlocks,
                deltaTracker.getGameTimeDeltaTicks()
        );
    }
}
