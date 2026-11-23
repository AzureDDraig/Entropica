package ddraig.net.entropica.client.renderer.mud_lurker_salamander;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.entity.mud_lurker_salamander.MudLurkerSalamanderEntity;
import ddraig.net.entropica.entity.mud_lurker_salamander.MudLurkerSalamanderVariant;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class MudLurkerSalamanderRenderer extends MobRenderer<MudLurkerSalamanderEntity, MudLurkerSalamanderRenderState, MudLurkerSalamanderModel> {

    private static final ResourceLocation TEXTURE_OVERWORLD = ResourceLocation.fromNamespaceAndPath(
            Entropica.MODID, "textures/entity/mud_lurker_salamander/mud_lurker_salamander.png"
    );
    private static final ResourceLocation TEXTURE_NETHER = ResourceLocation.fromNamespaceAndPath(
            Entropica.MODID, "textures/entity/mud_lurker_salamander/mud_lurker_salamander_nether.png"
    );

    public MudLurkerSalamanderRenderer(EntityRendererProvider.Context context) {
        super(context, new MudLurkerSalamanderModel(context.bakeLayer(MudLurkerSalamanderModel.LAYER_LOCATION)), 0.55F);
    }

    @Override
    public MudLurkerSalamanderRenderState createRenderState() {
        return new MudLurkerSalamanderRenderState();
    }

    @Override
    public void extractRenderState(MudLurkerSalamanderEntity entity, MudLurkerSalamanderRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.walkAnimationState.copyFrom(entity.walkAnimationState);
        state.attackAnimationState.copyFrom(entity.attackAnimationState);
        state.enterWaterAnimationState.copyFrom(entity.enterWaterAnimationState);
        state.swimIdleAnimationState.copyFrom(entity.swimIdleAnimationState);
        state.swimAnimationState.copyFrom(entity.swimAnimationState);
        state.swimAttackAnimationState.copyFrom(entity.swimAttackAnimationState);
        state.exitWaterAnimationState.copyFrom(entity.exitWaterAnimationState);
        state.variant = entity.getVariant();
        state.inFluid = entity.isInFluidMedium();
    }

    @Override
    protected int getBlockLightLevel(MudLurkerSalamanderEntity entity, BlockPos pos) {
        if (entity.isNetherVariant()) {
            return Math.max(10, super.getBlockLightLevel(entity, pos));
        }
        return super.getBlockLightLevel(entity, pos);
    }

    @Override
    public ResourceLocation getTextureLocation(MudLurkerSalamanderRenderState state) {
        return state.variant == MudLurkerSalamanderVariant.NETHER_MAGMA ? TEXTURE_NETHER : TEXTURE_OVERWORLD;
    }
}
