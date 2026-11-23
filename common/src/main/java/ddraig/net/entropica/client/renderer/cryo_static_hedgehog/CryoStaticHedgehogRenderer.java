package ddraig.net.entropica.client.renderer.cryo_static_hedgehog;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.entity.cryo_static_hedgehog.CryoStaticHedgehogEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class CryoStaticHedgehogRenderer extends MobRenderer<CryoStaticHedgehogEntity, CryoStaticHedgehogRenderState, CryoStaticHedgehogModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Entropica.MODID, "textures/entity/cryo_static_hedgehog/cryo_static_hedgehog.png"
    );

    public CryoStaticHedgehogRenderer(EntityRendererProvider.Context context) {
        super(context, new CryoStaticHedgehogModel(context.bakeLayer(CryoStaticHedgehogModel.LAYER_LOCATION)), 0.35F);
    }

    @Override
    public CryoStaticHedgehogRenderState createRenderState() {
        return new CryoStaticHedgehogRenderState();
    }

    @Override
    public void extractRenderState(CryoStaticHedgehogEntity entity, CryoStaticHedgehogRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.walkAnimationState.copyFrom(entity.walkAnimationState);
        state.curlAnimationState.copyFrom(entity.curlAnimationState);
        state.sneezeAnimationState.copyFrom(entity.sneezeAnimationState);
        state.digInAnimationState.copyFrom(entity.digInAnimationState);
        state.digHoldAnimationState.copyFrom(entity.digHoldAnimationState);
        state.digOutAnimationState.copyFrom(entity.digOutAnimationState);
        state.isCurled = entity.isCurled();
        state.isStormCharged = entity.isStormCharged();
    }

    @Override
    public ResourceLocation getTextureLocation(CryoStaticHedgehogRenderState state) {
        return TEXTURE;
    }
}
