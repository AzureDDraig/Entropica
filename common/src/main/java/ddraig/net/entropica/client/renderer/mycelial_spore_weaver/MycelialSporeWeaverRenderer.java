package ddraig.net.entropica.client.renderer.mycelial_spore_weaver;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.entity.mycelial_spore_weaver.MycelialSporeWeaverEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class MycelialSporeWeaverRenderer extends MobRenderer<MycelialSporeWeaverEntity, MycelialSporeWeaverRenderState, MycelialSporeWeaverModel> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Entropica.MODID, "textures/entity/mycelial_spore_weaver/mycelial_spore_weaver.png"
    );

    public MycelialSporeWeaverRenderer(EntityRendererProvider.Context context) {
        super(context, new MycelialSporeWeaverModel(context.bakeLayer(MycelialSporeWeaverModel.LAYER_LOCATION)), 0.55F);
        this.addLayer(new MycelialSporeWeaverGlowLayer(this));
    }

    @Override
    public MycelialSporeWeaverRenderState createRenderState() {
        return new MycelialSporeWeaverRenderState();
    }

    @Override
    public void extractRenderState(MycelialSporeWeaverEntity entity, MycelialSporeWeaverRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.hasCap = entity.hasCap();
        state.puffTicks = entity.getPuffTicks();
    }

    @Override
    public ResourceLocation getTextureLocation(MycelialSporeWeaverRenderState state) {
        return TEXTURE;
    }
}
