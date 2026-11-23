package ddraig.net.entropica.client.renderer.starlight_nautilus;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.entity.starlight_nautilus.StarlightNautilusEntity;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class StarlightNautilusRenderer extends AgeableMobRenderer<
        StarlightNautilusEntity,
        StarlightNautilusRenderState,
        StarlightNautilusModel
> {

    private static final ResourceLocation ADULT_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Entropica.MODID, "textures/entity/starlight_nautilus/starlight_nautilus.png"
    );
    private static final ResourceLocation BABY_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Entropica.MODID, "textures/entity/starlight_nautilus/starlight_nautilus_baby.png"
    );

    public StarlightNautilusRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new StarlightNautilusModel(context.bakeLayer(StarlightNautilusModel.LAYER_LOCATION)),
                new StarlightNautilusModel(context.bakeLayer(StarlightNautilusModel.BABY_LAYER_LOCATION)),
                0.55F
        );
        this.addLayer(new StarlightNautilusSaddleLayer(this, context.getModelSet()));
        this.addLayer(new StarlightNautilusArmorLayer(this, context.getModelSet()));
    }

    @Override
    public StarlightNautilusRenderState createRenderState() {
        return new StarlightNautilusRenderState();
    }

    @Override
    public void extractRenderState(StarlightNautilusEntity entity, StarlightNautilusRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        // Dedicated 8x6x9 baby mesh is already authored at true baby texel scale; avoid double-shrinking by 0.5x
        if (state.isBaby) {
            state.ageScale = 1.0F;
        }
        state.isSaddled = entity.isSaddled();
        state.armorType = entity.getArmorType();
        int dashTicks = entity.getDashTicks();
        state.dashProgress = dashTicks > 0
                ? Mth.clamp((dashTicks - partialTick) / (float) StarlightNautilusEntity.DASH_DURATION_TICKS, 0.0F, 1.0F)
                : 0.0F;
        state.inWater = entity.isInWater();
        state.isSitting = entity.isOrderedToSit();
        state.isRidden = entity.isVehicle();
        state.flightBuffKind = entity.getFlightBuffKind();
    }

    @Override
    public ResourceLocation getTextureLocation(StarlightNautilusRenderState state) {
        return state.isBaby ? BABY_TEXTURE : ADULT_TEXTURE;
    }

    @Override
    protected int getBlockLightLevel(StarlightNautilusEntity entity, BlockPos pos) {
        int glow = entity.hasActiveBuff(StarlightNautilusEntity.BUFF_AURA) ? 14 : 10;
        return Math.max(glow, super.getBlockLightLevel(entity, pos));
    }
}
