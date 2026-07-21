package ddraig.net.entropica.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void entropica$onRender(EntityRenderState state, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
        if (!(state instanceof LivingEntityRenderState livingState)) return;
        
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (!ddraig.net.entropica.client.LensOverlayRenderer.isVitaeVisionActive(mc.player)) return;

        double worldX = livingState.x;
        double worldY = livingState.y;
        double worldZ = livingState.z;

        net.minecraft.world.phys.AABB area = new net.minecraft.world.phys.AABB(worldX - 0.5, worldY - 0.5, worldZ - 0.5, worldX + 0.5, worldY + 0.5, worldZ + 0.5);
        java.util.List<LivingEntity> entities = mc.level.getEntitiesOfClass(LivingEntity.class, area);
        if (entities.isEmpty()) return;

        LivingEntity entity = entities.get(0);
        if (entity == mc.player) return;
        if (mc.player.distanceToSqr(entity) > 32 * 32) return;

        float health = entity.getHealth();
        float maxHealth = entity.getMaxHealth();
        
        int totalSegments = 10;
        int filledSegments = Math.max(0, Math.min(totalSegments, Math.round((health / maxHealth) * totalSegments)));
        
        StringBuilder bar = new StringBuilder("§8[§c");
        for (int idx = 0; idx < totalSegments; idx++) {
            if (idx == filledSegments) {
                bar.append("§8");
            }
            bar.append("■");
        }
        bar.append("§8]");
        
        String text = String.format("HP: %.1f/%.1f %s", health, maxHealth, bar.toString());
        
        Font font = mc.font;
        
        poseStack.pushPose();
        poseStack.translate(0.0d, livingState.boundingBoxHeight + 0.4d, 0.0d);
        poseStack.mulPose(mc.gameRenderer.getMainCamera().rotation());
        poseStack.scale(-0.02f, -0.02f, 0.02f);
        
        float xOffset = -font.width(text) / 2f;
        int light = i;
        
        int bg = 0x50000088;
        font.drawInBatch(text, xOffset, 0.0F, 0xFFFFFF, false, poseStack.last().pose(), multiBufferSource, Font.DisplayMode.SEE_THROUGH, bg, light);
        font.drawInBatch(text, xOffset, 0.0F, 0xFFFFFF, false, poseStack.last().pose(), multiBufferSource, Font.DisplayMode.NORMAL, 0, light);
        
        poseStack.popPose();
    }
}
