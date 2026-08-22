package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ddraig.net.entropica.compat.jei.AstralMultiblockRecipe;
import ddraig.net.entropica.compat.jei.AstralMultiblockRecipes;
import ddraig.net.entropica.item.AstrolabeItem;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.List;
import java.util.Map;

public class AstrolabeGhostRenderer {

    public static void renderGhost(PoseStack poseStack, Matrix4f modelViewMatrix, Camera camera, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        Player player = mc.player;
        ItemStack held = ItemStack.EMPTY;
        if (player.getMainHandItem().getItem() instanceof AstrolabeItem) {
            held = player.getMainHandItem();
        } else if (player.getOffhandItem().getItem() instanceof AstrolabeItem) {
            held = player.getOffhandItem();
        }

        // If player is not actively holding an Astrolabe, check if they have an anchored Astrolabe in inventory
        if (held.isEmpty()) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack invStack = player.getInventory().getItem(i);
                if (invStack.getItem() instanceof AstrolabeItem) {
                    CustomData cd = invStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                    CompoundTag t = cd.copyTag();
                    if (t.getBoolean(AstrolabeItem.TAG_HAS_ANCHOR).orElse(false)) {
                        held = invStack;
                        break;
                    }
                }
            }
        }

        if (held.isEmpty()) return;

        CustomData customData = held.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();

        int bpIdx = tag.getInt(AstrolabeItem.TAG_BLUEPRINT_INDEX).orElse(0);
        if (bpIdx <= 0) return;

        boolean hasAnchor = tag.getBoolean(AstrolabeItem.TAG_HAS_ANCHOR).orElse(false);
        BlockPos anchorPos;

        if (hasAnchor) {
            anchorPos = new BlockPos(
                    tag.getInt(AstrolabeItem.TAG_ANCHOR_X).orElse(0),
                    tag.getInt(AstrolabeItem.TAG_ANCHOR_Y).orElse(0),
                    tag.getInt(AstrolabeItem.TAG_ANCHOR_Z).orElse(0)
            );
        } else {
            // Only follow cursor if holding Astrolabe in hand
            boolean isHolding = player.getMainHandItem().getItem() instanceof AstrolabeItem || player.getOffhandItem().getItem() instanceof AstrolabeItem;
            if (!isHolding) return;

            Vec3 eyePos = player.getEyePosition(partialTick);
            Vec3 lookVec = player.getViewVector(partialTick);
            Vec3 endVec = eyePos.add(lookVec.scale(24.0));

            BlockHitResult hit = mc.level.clip(new ClipContext(
                    eyePos, endVec,
                    ClipContext.Block.OUTLINE,
                    ClipContext.Fluid.NONE,
                    player
            ));

            if (hit.getType() == HitResult.Type.BLOCK) {
                anchorPos = hit.getBlockPos().above();
            } else {
                return;
            }
        }

        List<AstralMultiblockRecipe> recipes = AstralMultiblockRecipes.createRecipes();
        if (bpIdx > recipes.size()) return;

        AstralMultiblockRecipe recipe = recipes.get(bpIdx - 1);
        List<List<String>> layers = recipe.layerBlueprints();
        Map<Character, ItemStack> legend = recipe.symbolLegend();

        int layerCount = layers.size();
        if (layerCount == 0) return;

        int depth = layers.get(0).size();
        int width = layers.get(0).get(0).length();

        int halfW = width / 2;
        int halfD = depth / 2;

        Vec3 camPos = camera.getPosition();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer lineConsumer = bufferSource.getBuffer(RenderType.lines());
        BlockRenderDispatcher blockRenderer = mc.getBlockRenderer();

        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);

        float timeSec = (System.currentTimeMillis() % 10000000L) * 0.001f;

        int activeLayer = tag.getInt(AstrolabeItem.TAG_ACTIVE_LAYER).orElse(0);

        for (int l = 0; l < layerCount; l++) {
            if (activeLayer > 0 && l != activeLayer - 1) continue;

            List<String> layer = layers.get(l);
            for (int z = 0; z < layer.size(); z++) {
                String row = layer.get(z);
                for (int x = 0; x < row.length(); x++) {
                    char c = row.charAt(x);
                    if (c == '.') continue;

                    ItemStack expectedStack = legend.get(c);
                    if (expectedStack == null || expectedStack.isEmpty()) continue;

                    BlockPos worldP = anchorPos.offset(x - halfW, l - 1, z - halfD);
                    BlockState existing = mc.level.getBlockState(worldP);
                    BlockState expectedState = (expectedStack.getItem() instanceof BlockItem bi) ? bi.getBlock().defaultBlockState() : null;

                    boolean isCorrect = (expectedState != null && existing.is(expectedState.getBlock()));
                    boolean isAir = existing.isAir();

                    float pulse = 0.85f + 0.15f * (float) Math.sin(timeSec * 3.0f + (x + z + l) * 0.5f);

                    if (isCorrect) {
                        // Already placed correctly - subtle emerald outline
                        float r = 0.15f * pulse, g = 1.0f * pulse, b = 0.35f * pulse, a = hasAnchor ? 0.35f : 0.20f;
                        renderGhostBox(poseStack, lineConsumer, worldP, r, g, b, a);
                    } else if (isAir && expectedState != null) {
                        // Missing block - render the authentic 3D block model in-world!
                        poseStack.pushPose();
                        poseStack.translate(worldP.getX(), worldP.getY(), worldP.getZ());
                        blockRenderer.renderSingleBlock(expectedState, poseStack, bufferSource, 15728880, OverlayTexture.NO_OVERLAY);
                        poseStack.popPose();

                        // Add glowing cyan starlight outline to frame the block template
                        float r = 0.2f * pulse, g = 0.85f * pulse, b = 1.0f * pulse, a = hasAnchor ? 0.55f : 0.35f;
                        renderGhostBox(poseStack, lineConsumer, worldP, r, g, b, a);
                    } else if (!isAir) {
                        // Wrong block placed here - pulsing red warning outline
                        float r = 1.0f * pulse, g = 0.2f * pulse, b = 0.2f * pulse, a = hasAnchor ? 0.85f : 0.55f;
                        renderGhostBox(poseStack, lineConsumer, worldP, r, g, b, a);
                    }
                }
            }
        }

        if (hasAnchor) {
            float pulse = 0.7f + 0.3f * (float) Math.sin(timeSec * 2.5f);
            float bx = anchorPos.getX() + 0.5f;
            float bz = anchorPos.getZ() + 0.5f;
            float by0 = anchorPos.getY();
            float by1 = by0 + 32.0f;
            Matrix4f lineMat = poseStack.last().pose();
            addLine(lineConsumer, lineMat, bx, by0, bz, bx, by1, bz, 0.2f * pulse, 0.9f * pulse, 1.0f * pulse, 0.65f * pulse);
            addLine(lineConsumer, lineMat, bx - 0.25f, by0, bz - 0.25f, bx - 0.25f, by1, bz - 0.25f, 0.1f * pulse, 0.7f * pulse, 0.9f * pulse, 0.35f * pulse);
            addLine(lineConsumer, lineMat, bx + 0.25f, by0, bz + 0.25f, bx + 0.25f, by1, bz + 0.25f, 0.1f * pulse, 0.7f * pulse, 0.9f * pulse, 0.35f * pulse);
        }

        bufferSource.endBatch(RenderType.lines());
        poseStack.popPose();
    }

    private static void renderGhostBox(PoseStack poseStack, VertexConsumer consumer, BlockPos pos, float r, float g, float b, float a) {
        poseStack.pushPose();
        poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
        Matrix4f mat = poseStack.last().pose();

        float x0 = 0.005f, y0 = 0.005f, z0 = 0.005f;
        float x1 = 0.995f, y1 = 0.995f, z1 = 0.995f;

        // Bottom 4 edges
        addLine(consumer, mat, x0, y0, z0, x1, y0, z0, r, g, b, a);
        addLine(consumer, mat, x1, y0, z0, x1, y0, z1, r, g, b, a);
        addLine(consumer, mat, x1, y0, z1, x0, y0, z1, r, g, b, a);
        addLine(consumer, mat, x0, y0, z1, x0, y0, z0, r, g, b, a);

        // Top 4 edges
        addLine(consumer, mat, x0, y1, z0, x1, y1, z0, r, g, b, a);
        addLine(consumer, mat, x1, y1, z0, x1, y1, z1, r, g, b, a);
        addLine(consumer, mat, x1, y1, z1, x0, y1, z1, r, g, b, a);
        addLine(consumer, mat, x0, y1, z1, x0, y1, z0, r, g, b, a);

        // Vertical 4 edges
        addLine(consumer, mat, x0, y0, z0, x0, y1, z0, r, g, b, a);
        addLine(consumer, mat, x1, y0, z0, x1, y1, z0, r, g, b, a);
        addLine(consumer, mat, x1, y0, z1, x1, y1, z1, r, g, b, a);
        addLine(consumer, mat, x0, y0, z1, x0, y1, z1, r, g, b, a);

        poseStack.popPose();
    }

    private static void addLine(VertexConsumer consumer, Matrix4f mat, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b, float a) {
        float nx = x2 - x1;
        float ny = y2 - y1;
        float nz = z2 - z1;
        float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (len > 0) {
            nx /= len;
            ny /= len;
            nz /= len;
        }
        consumer.addVertex(mat, x1, y1, z1).setColor(r, g, b, a).setNormal(nx, ny, nz);
        consumer.addVertex(mat, x2, y2, z2).setColor(r, g, b, a).setNormal(nx, ny, nz);
    }
}
