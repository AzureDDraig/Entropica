package ddraig.net.entropica.client.renderer;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.model.SandstoneGolemModel;
import ddraig.net.entropica.entity.sandstone_golem.SandstoneGolemEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class SandstoneGolemRenderer extends MobRenderer<SandstoneGolemEntity, SandstoneGolemRenderState, SandstoneGolemModel> {
    private static final ResourceLocation TEXTURE_PALE = ResourceLocation.fromNamespaceAndPath(
            Entropica.MODID, "textures/entity/sandstone_golem/sandstone_golem.png"
    );
    private static final ResourceLocation TEXTURE_RED = ResourceLocation.fromNamespaceAndPath(
            Entropica.MODID, "textures/entity/sandstone_golem/sandstone_golem_red.png"
    );

    public SandstoneGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new SandstoneGolemModel(context.bakeLayer(SandstoneGolemModel.LAYER_LOCATION)), 0.70F);
    }

    @Override
    public SandstoneGolemRenderState createRenderState() {
        return new SandstoneGolemRenderState();
    }

    @Override
    public void extractRenderState(SandstoneGolemEntity entity, SandstoneGolemRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.walkAnimationState.copyFrom(entity.walkAnimationState);
        state.attackAnimationState.copyFrom(entity.attackAnimationState);
        state.repairAnimationState.copyFrom(entity.repairAnimationState);
        state.isRedVariant = entity.isRedVariant();
        state.isRepairing = entity.isRepairing();
        state.isProvoked = entity.isProvoked();
    }

    @Override
    protected int getBlockLightLevel(SandstoneGolemEntity entity, BlockPos pos) {
        int coreGlow = entity.isRepairing() ? 11 : 7;
        return Math.max(coreGlow, super.getBlockLightLevel(entity, pos));
    }

    @Override
    public ResourceLocation getTextureLocation(SandstoneGolemRenderState state) {
        return state.isRedVariant ? TEXTURE_RED : TEXTURE_PALE;
    }
}
