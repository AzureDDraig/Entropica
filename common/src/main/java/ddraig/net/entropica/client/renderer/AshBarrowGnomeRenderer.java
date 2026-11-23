package ddraig.net.entropica.client.renderer;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.model.AshBarrowGnomeModel;
import ddraig.net.entropica.entity.ash_barrow_gnome.AshBarrowGnomeEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class AshBarrowGnomeRenderer extends MobRenderer<AshBarrowGnomeEntity, AshBarrowGnomeRenderState, AshBarrowGnomeModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Entropica.MODID, "textures/entity/ash_barrow_gnome/ash_barrow_gnome.png"
    );

    public AshBarrowGnomeRenderer(EntityRendererProvider.Context context) {
        super(context, new AshBarrowGnomeModel(context.bakeLayer(AshBarrowGnomeModel.LAYER_LOCATION)), 0.50F);
    }

    @Override
    public AshBarrowGnomeRenderState createRenderState() {
        return new AshBarrowGnomeRenderState();
    }

    @Override
    public void extractRenderState(AshBarrowGnomeEntity entity, AshBarrowGnomeRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.walkAnimationState.copyFrom(entity.walkAnimationState);
        state.attackAnimationState.copyFrom(entity.attackAnimationState);
        state.digAnimationState.copyFrom(entity.digAnimationState);
        state.hideAnimationState.copyFrom(entity.hideAnimationState);
        state.unhideAnimationState.copyFrom(entity.unhideAnimationState);
        state.hurtAnimationState.copyFrom(entity.hurtAnimationState);
        state.deathAnimationState.copyFrom(entity.deathAnimationState);
        state.cairnState = entity.getCairnState();
        state.isDiggingGravel = entity.isDiggingGravel();
    }

    @Override
    protected float getFlipDegrees() {
        return 0.0F;
    }

    @Override
    protected int getBlockLightLevel(AshBarrowGnomeEntity entity, BlockPos pos) {
        int runeLight = entity.isAlive() ? 5 : 2;
        return Math.max(runeLight, super.getBlockLightLevel(entity, pos));
    }

    @Override
    public ResourceLocation getTextureLocation(AshBarrowGnomeRenderState state) {
        return TEXTURE;
    }
}
