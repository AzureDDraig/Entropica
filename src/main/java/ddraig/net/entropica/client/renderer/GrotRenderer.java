package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.client.model.GrotModel;
import ddraig.net.entropica.client.model.ModModelLayers;
import ddraig.net.entropica.entity.grot.GrotEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class GrotRenderer extends MobRenderer<GrotEntity, GrotRenderState, GrotModel> {

    private static final ResourceLocation GROT_TEXTURE = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/entity/grot/grot.png");

    public GrotRenderer(EntityRendererProvider.Context context) {
        super(context, new GrotModel(context.bakeLayer(ModModelLayers.GROT)), 0.25F);
        this.addLayer(new GrotJellyLayer(this, context.getModelSet()));
    }

    @Override
    public GrotRenderState createRenderState() {
        return new GrotRenderState();
    }

    @Override
    public void extractRenderState(GrotEntity entity, GrotRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.essenceType = entity.getEssenceType();
        state.grotAgeInTicks = entity.tickCount + partialTick;
        state.size = entity.getSize();
        state.squish = Mth.lerp(partialTick, entity.oSquish, entity.squish);

        // We use our custom death time for the melting animation
        state.grotDeathTime = entity.deathTime > 0 ? entity.deathTime + partialTick : 0.0f;
        state.hurtTime = entity.hurtTime;
        state.healTime = entity.healTime;

        // FIX: By mathematically lying to the base LivingEntityRenderer and telling it the entity
        // is not dead, we completely disable the 90-degree death flip while preserving all
        // native, flawless rotation and looking logic!
        state.deathTime = 0.0f;
    }

    @Override
    public ResourceLocation getTextureLocation(GrotRenderState state) {
        return GROT_TEXTURE;
    }

    @Override
    protected float getShadowRadius(GrotRenderState state) {
        return 0.25F * state.size;
    }

    @Override
    protected void scale(GrotRenderState state, PoseStack poseStack) {
        // Multiplies the 0.5 block visual geometry perfectly by the Grot's size state!
        float f = state.size;
        float f1 = state.squish / (f * 0.5F + 1.0F);
        float f2 = 1.0F / (f1 + 1.0F);
        poseStack.scale(f2 * f, 1.0F / f2 * f, f2 * f);
    }
}