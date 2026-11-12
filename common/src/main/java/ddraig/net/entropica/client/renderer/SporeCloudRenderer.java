package ddraig.net.entropica.client.renderer;

import ddraig.net.entropica.entity.SporeCloudEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public class SporeCloudRenderer extends EntityRenderer<SporeCloudEntity, EntityRenderState> {

    public SporeCloudRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }
}
