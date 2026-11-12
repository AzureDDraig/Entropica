package ddraig.net.entropica.client.renderer.amber_weeping_stag;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.entity.amber_weeping_stag.AmberStagVariant;
import ddraig.net.entropica.entity.amber_weeping_stag.AmberWeepingStagEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class AmberWeepingStagRenderer extends MobRenderer<AmberWeepingStagEntity, AmberWeepingStagRenderState, AmberWeepingStagModel> {

    private static final ResourceLocation TEXTURE_AMBER = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/entity/amber_weeping_stag/amber_weeping_stag.png");
    private static final ResourceLocation TEXTURE_FROST = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/entity/amber_weeping_stag/amber_weeping_stag_frost.png");
    private static final ResourceLocation TEXTURE_SOLAR = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/entity/amber_weeping_stag/amber_weeping_stag_solar.png");

    public AmberWeepingStagRenderer(EntityRendererProvider.Context context) {
        super(context, new AmberWeepingStagModel(context.bakeLayer(AmberWeepingStagModel.LAYER_LOCATION)), 0.7F);
    }

    @Override
    public AmberWeepingStagRenderState createRenderState() {
        return new AmberWeepingStagRenderState();
    }

    @Override
    public void extractRenderState(AmberWeepingStagEntity entity, AmberWeepingStagRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.walkAnimationState.copyFrom(entity.walkAnimationState);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.attackAnimationState.copyFrom(entity.attackAnimationState);
        state.variant = entity.getVariant();
    }

    @Override
    public ResourceLocation getTextureLocation(AmberWeepingStagRenderState state) {
        if (state.variant == AmberStagVariant.FROST) {
            return TEXTURE_FROST;
        } else if (state.variant == AmberStagVariant.SOLAR) {
            return TEXTURE_SOLAR;
        }
        return TEXTURE_AMBER;
    }
}
