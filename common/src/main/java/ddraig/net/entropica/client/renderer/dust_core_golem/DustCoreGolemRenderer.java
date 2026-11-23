package ddraig.net.entropica.client.renderer.dust_core_golem;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.entity.dust_core_golem.DustCoreGolemEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class DustCoreGolemRenderer extends MobRenderer<DustCoreGolemEntity, DustCoreGolemRenderState, DustCoreGolemModel> {

    private static final ResourceLocation TEXTURE_POLAR = ResourceLocation.fromNamespaceAndPath(
            Entropica.MODID, "textures/entity/dust_core_golem/dust_core_golem.png"
    );
    private static final ResourceLocation TEXTURE_CANYON = ResourceLocation.fromNamespaceAndPath(
            Entropica.MODID, "textures/entity/dust_core_golem/dust_core_golem_canyon.png"
    );

    public DustCoreGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new DustCoreGolemModel(context.bakeLayer(DustCoreGolemModel.LAYER_LOCATION)), 0.45F);
    }

    @Override
    public DustCoreGolemRenderState createRenderState() {
        return new DustCoreGolemRenderState();
    }

    @Override
    public void extractRenderState(DustCoreGolemEntity entity, DustCoreGolemRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.variant = entity.getVariant();
        state.actionState = entity.getActionState();
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.walkAnimationState.copyFrom(entity.walkAnimationState);
        state.attackAnimationState.copyFrom(entity.attackAnimationState);
        state.repairAnimationState.copyFrom(entity.repairAnimationState);
    }

    @Override
    protected int getBlockLightLevel(DustCoreGolemEntity entity, BlockPos pos) {
        return Math.max(8, super.getBlockLightLevel(entity, pos));
    }

    @Override
    public ResourceLocation getTextureLocation(DustCoreGolemRenderState state) {
        return state.variant == DustCoreGolemEntity.VARIANT_RED_CANYON_SILT ? TEXTURE_CANYON : TEXTURE_POLAR;
    }
}
