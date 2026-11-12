package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.entity.geyser_wiggle_worm.GeyserWiggleWormEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class GeyserWiggleWormRenderer extends MobRenderer<GeyserWiggleWormEntity, GeyserWiggleWormRenderState, GeyserWiggleWormModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/entity/geyser_wiggle_worm/geyser_wiggle_worm.png");

    public GeyserWiggleWormRenderer(EntityRendererProvider.Context context) {
        super(context, new GeyserWiggleWormModel(context.bakeLayer(GeyserWiggleWormModel.LAYER_LOCATION)), 1.0F);
    }

    @Override
    public GeyserWiggleWormRenderState createRenderState() {
        return new GeyserWiggleWormRenderState();
    }

    @Override
    public void extractRenderState(GeyserWiggleWormEntity entity, GeyserWiggleWormRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.slitherAnimationState.copyFrom(entity.slitherAnimationState);
        state.rearUpAnimationState.copyFrom(entity.rearUpAnimationState);
        state.attackAnimationState.copyFrom(entity.attackAnimationState);
        state.ventSteamAnimationState.copyFrom(entity.ventSteamAnimationState);
        state.isVentingSteam = entity.isVentingSteam();
        state.isRearingUp = entity.isRearingUp();
        state.isAttacking = entity.isAttacking();
    }

    @Override
    protected void scale(GeyserWiggleWormRenderState state, PoseStack poseStack) {
        super.scale(state, poseStack);
        poseStack.scale(1.75F, 1.75F, 1.75F);
    }

    @Override
    public ResourceLocation getTextureLocation(GeyserWiggleWormRenderState state) {
        return TEXTURE;
    }
}
