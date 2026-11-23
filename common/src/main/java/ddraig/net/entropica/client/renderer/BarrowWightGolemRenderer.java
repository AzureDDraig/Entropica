package ddraig.net.entropica.client.renderer;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.model.BarrowWightGolemModel;
import ddraig.net.entropica.entity.barrow_wight_golem.BarrowWightGolemEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class BarrowWightGolemRenderer extends MobRenderer<BarrowWightGolemEntity, BarrowWightGolemRenderState, BarrowWightGolemModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Entropica.MODID, "textures/entity/barrow_wight_golem/barrow_wight_golem.png"
    );

    public BarrowWightGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new BarrowWightGolemModel(context.bakeLayer(BarrowWightGolemModel.LAYER_LOCATION)), 0.85F);
    }

    @Override
    public BarrowWightGolemRenderState createRenderState() {
        return new BarrowWightGolemRenderState();
    }

    @Override
    public void extractRenderState(BarrowWightGolemEntity entity, BarrowWightGolemRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.walkAnimationState.copyFrom(entity.walkAnimationState);
        state.attackAnimationState.copyFrom(entity.attackAnimationState);
        state.soulfireBurstAnimationState.copyFrom(entity.soulfireBurstAnimationState);
        state.dormantEnterAnimationState.copyFrom(entity.dormantEnterAnimationState);
        state.dormantAnimationState.copyFrom(entity.dormantAnimationState);
        state.awakenAnimationState.copyFrom(entity.awakenAnimationState);
        state.hurtAnimationState.copyFrom(entity.hurtAnimationState);
        state.deathAnimationState.copyFrom(entity.deathAnimationState);
        state.sentinelState = entity.getSentinelState();
        state.isBurstingSoulfire = entity.isBurstingSoulfire();
    }

    @Override
    protected float getFlipDegrees() {
        return 0.0F;
    }

    @Override
    protected int getBlockLightLevel(BarrowWightGolemEntity entity, BlockPos pos) {
        int coreLight;
        if (entity.isDeadOrDying()) {
            coreLight = 2;
        } else if (entity.isBurstingSoulfire()) {
            coreLight = 15;
        } else if (entity.getSentinelState() == BarrowWightGolemEntity.STATE_DORMANT) {
            coreLight = 5;
        } else {
            coreLight = 11;
        }
        return Math.max(coreLight, super.getBlockLightLevel(entity, pos));
    }

    @Override
    public ResourceLocation getTextureLocation(BarrowWightGolemRenderState state) {
        return TEXTURE;
    }
}
