package ddraig.net.entropica.client.renderer.vapor_bellows_crab;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.entity.vapor_bellows_crab.VaporBellowsCrabEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class VaporBellowsCrabRenderer extends MobRenderer<VaporBellowsCrabEntity, VaporBellowsCrabRenderState, VaporBellowsCrabModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Entropica.MODID, "textures/entity/vapor_bellows_crab/vapor_bellows_crab.png"
    );

    public VaporBellowsCrabRenderer(EntityRendererProvider.Context context) {
        super(context, new VaporBellowsCrabModel(context.bakeLayer(VaporBellowsCrabModel.LAYER_LOCATION)), 0.7F);
    }

    @Override
    public VaporBellowsCrabRenderState createRenderState() {
        return new VaporBellowsCrabRenderState();
    }

    @Override
    public void extractRenderState(VaporBellowsCrabEntity entity, VaporBellowsCrabRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.walkAnimationState.copyFrom(entity.walkAnimationState);
        state.attackAnimationState.copyFrom(entity.attackAnimationState);
        state.steamVentAnimationState.copyFrom(entity.steamVentAnimationState);
        state.braceDefenseAnimationState.copyFrom(entity.braceDefenseAnimationState);
        state.ventCharged = entity.isVentCharged();
    }

    @Override
    public ResourceLocation getTextureLocation(VaporBellowsCrabRenderState state) {
        return TEXTURE;
    }
}
