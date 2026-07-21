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
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // 1. Materia Lens Billboard over ItemEntity in the world
        if (state instanceof net.minecraft.client.renderer.entity.state.ItemEntityRenderState itemState) {
            if (!ddraig.net.entropica.client.LensOverlayRenderer.isMateriaVisionActive(mc.player)) return;

            net.minecraft.world.phys.Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
            double worldX = cameraPos.x + itemState.x;
            double worldY = cameraPos.y + itemState.y;
            double worldZ = cameraPos.z + itemState.z;

            net.minecraft.world.phys.AABB area = new net.minecraft.world.phys.AABB(worldX - 0.5, worldY - 0.5, worldZ - 0.5, worldX + 0.5, worldY + 0.5, worldZ + 0.5);
            java.util.List<net.minecraft.world.entity.item.ItemEntity> entities = mc.level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, area);
            if (entities.isEmpty()) return;

            net.minecraft.world.entity.item.ItemEntity itemEntity = entities.get(0);
            net.minecraft.world.item.ItemStack stack = itemEntity.getItem();
            if (stack.isEmpty()) return;

            java.util.List<ddraig.net.entropica.api.ItemEssenceMap.EssenceValue> values = ddraig.net.entropica.api.ItemEssenceMap.getEssenceFor(stack);
            if (values != null && !values.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (int idx = 0; idx < values.size(); idx++) {
                    ddraig.net.entropica.api.ItemEssenceMap.EssenceValue val = values.get(idx);
                    if (idx > 0) sb.append(" | ");
                    sb.append(String.format("%.1f %s", val.amount(), val.type().getDisplayName()));
                }
                String text = sb.toString();

                Font font = mc.font;
                poseStack.pushPose();
                poseStack.translate(0.0d, itemState.boundingBoxHeight + 0.3d, 0.0d);
                poseStack.mulPose(mc.gameRenderer.getMainCamera().rotation());
                poseStack.scale(-0.015f, -0.015f, 0.015f);

                float xOffset = -font.width(text) / 2f;
                int bg = 0x50000000;
                font.drawInBatch(text, xOffset, 0.0F, 0xFFFFFF, false, poseStack.last().pose(), multiBufferSource, Font.DisplayMode.SEE_THROUGH, bg, i);
                font.drawInBatch(text, xOffset, 0.0F, 0xFFFFFF, false, poseStack.last().pose(), multiBufferSource, Font.DisplayMode.NORMAL, 0, i);

                poseStack.popPose();
            }
            return;
        }

        // 2. Vitae Lens Healthbar Billboard over LivingEntity in the world
        if (state instanceof LivingEntityRenderState livingState) {
            if (!ddraig.net.entropica.client.LensOverlayRenderer.isVitaeVisionActive(mc.player)) return;

            net.minecraft.world.phys.Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
            double worldX = cameraPos.x + livingState.x;
            double worldY = cameraPos.y + livingState.y;
            double worldZ = cameraPos.z + livingState.z;

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
            int bg = 0x50000088;
            font.drawInBatch(text, xOffset, 0.0F, 0xFFFFFF, false, poseStack.last().pose(), multiBufferSource, Font.DisplayMode.SEE_THROUGH, bg, i);
            font.drawInBatch(text, xOffset, 0.0F, 0xFFFFFF, false, poseStack.last().pose(), multiBufferSource, Font.DisplayMode.NORMAL, 0, i);

            poseStack.popPose();
        }
    }
}
