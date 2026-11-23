package ddraig.net.entropica.client.renderer;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.model.PeatMudKelpieModel;
import ddraig.net.entropica.entity.peat_mud_kelpie.PeatMudKelpieEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class PeatMudKelpieRenderer extends MobRenderer<PeatMudKelpieEntity, PeatMudKelpieRenderState, PeatMudKelpieModel> {
    private static final ResourceLocation TEXTURE_PEAT_MUD = ResourceLocation.fromNamespaceAndPath(
            Entropica.MODID, "textures/entity/peat_mud_kelpie/peat_mud_kelpie.png"
    );
    private static final ResourceLocation TEXTURE_MANGROVE = ResourceLocation.fromNamespaceAndPath(
            Entropica.MODID, "textures/entity/peat_mud_kelpie/peat_mud_kelpie_mangrove.png"
    );

    public PeatMudKelpieRenderer(EntityRendererProvider.Context context) {
        super(context, new PeatMudKelpieModel(context.bakeLayer(PeatMudKelpieModel.LAYER_LOCATION)), 0.65F);
    }

    @Override
    public PeatMudKelpieRenderState createRenderState() {
        return new PeatMudKelpieRenderState();
    }

    @Override
    public void extractRenderState(PeatMudKelpieEntity entity, PeatMudKelpieRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.walkAnimationState.copyFrom(entity.walkAnimationState);
        state.swimAnimationState.copyFrom(entity.swimAnimationState);
        state.attackAnimationState.copyFrom(entity.attackAnimationState);
        state.rearAnimationState.copyFrom(entity.rearAnimationState);
        state.isMangroveVariant = entity.isMangroveVariant();
        state.isWaterWalking = entity.isWaterWalking();
        state.isRearing = entity.isRearing();
    }

    @Override
    protected int getBlockLightLevel(PeatMudKelpieEntity entity, BlockPos pos) {
        int spiritGlow = (entity.isRearing() || entity.isWaterWalking()) ? 9 : 6;
        return Math.max(spiritGlow, super.getBlockLightLevel(entity, pos));
    }

    @Override
    public ResourceLocation getTextureLocation(PeatMudKelpieRenderState state) {
        return state.isMangroveVariant ? TEXTURE_MANGROVE : TEXTURE_PEAT_MUD;
    }
}
