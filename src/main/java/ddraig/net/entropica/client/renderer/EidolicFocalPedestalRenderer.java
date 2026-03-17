package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.block.VisIchorInputPortBlock;
import ddraig.net.entropica.block.entity.EidolicLatheBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class EidolicFocalPedestalRenderer implements BlockEntityRenderer<EidolicLatheBlockEntity, EidolicFocalPedestalRenderer.LatheRenderState> {

    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    private static final ResourceLocation SMOKE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/particle/effect_0.png");
    private static final ResourceLocation DRIP_TEXTURE = ResourceLocation.withDefaultNamespace("textures/particle/drip_fall.png");

    private final Font font;
    private final ItemModelResolver itemModelResolver;

    public EidolicFocalPedestalRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.font();
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public boolean shouldRender(EidolicLatheBlockEntity blockEntity, Vec3 cameraPos) {
        return true;
    }

    @Override
    public LatheRenderState createRenderState() {
        return new LatheRenderState();
    }

    @Override
    public void extractRenderState(EidolicLatheBlockEntity be, LatheRenderState state, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);

        state.isFormed = be.isFormed();
        Level level = be.getLevel();

        state.time = (level != null ? (level.getGameTime() % 360000L) : 0) + partialTick;
        state.pedestalOffsets.clear();
        state.fuelPedestalOffsets.clear();

        state.isCrafting = be.isCrafting;
        state.waitForClick = be.waitForClick;
        state.craftingProgress = be.craftingProgress;
        state.maxCraftingProgress = Math.max(1, be.maxCraftingProgress);
        state.craftingEssenceType = be.craftingEssenceType;

        if (state.isCrafting) {
            if (state.waitForClick) {
                // If waiting for the player to click, pause interpolation completely
                state.smoothProg = (float) be.craftingProgress / state.maxCraftingProgress;
            } else {
                state.smoothProg = Math.min(1.0f, (be.craftingProgress + partialTick) / state.maxCraftingProgress);
            }
        } else {
            state.smoothProg = 0.0f;
        }

        BlockPos center = be.getBlockPos();

        if (be.activePortPos != null && level != null) {
            state.portOffset = new Vec3(be.activePortPos.getX() - center.getX(), be.activePortPos.getY() - center.getY(), be.activePortPos.getZ() - center.getZ());
            Block portBlock = level.getBlockState(be.activePortPos).getBlock();
            state.isIchorPort = (portBlock instanceof VisIchorInputPortBlock);
        } else {
            state.portOffset = null;
            state.isIchorPort = false;
        }

        if (state.isFormed) {
            for (BlockPos p : be.connectedPedestals) {
                state.pedestalOffsets.add(new Vec3(p.getX() - center.getX(), p.getY() - center.getY(), p.getZ() - center.getZ()));
            }

            // Extract the newly added Ampoule Fuel Pedestals
            for (BlockPos p : be.fuelPedestals) {
                state.fuelPedestalOffsets.add(new Vec3(p.getX() - center.getX(), p.getY() - center.getY(), p.getZ() - center.getZ()));
            }

            int seed = (int) be.getBlockPos().asLong();
            for (int i = 0; i < 4; i++) {
                ItemStack stack = be.inventory.getItem(i);
                state.hasItem[i] = !stack.isEmpty();
                if (!stack.isEmpty() && level != null) {
                    this.itemModelResolver.updateForTopItem(state.items[i], stack, ItemDisplayContext.FIXED, level, null, seed + i);
                } else {
                    state.items[i].clear();
                }
            }

            // Extract the Output Slot (Slot 4)
            if (be.inventory.getContainerSize() > 4) {
                ItemStack outStack = be.inventory.getItem(4);
                state.hasOutputItem = !outStack.isEmpty();
                if (state.hasOutputItem && level != null) {
                    this.itemModelResolver.updateForTopItem(state.outputItem, outStack, ItemDisplayContext.FIXED, level, null, seed + 4);
                } else {
                    state.outputItem.clear();
                }
            } else {
                state.hasOutputItem = false;
                state.outputItem.clear();
            }
        }
    }

    @Override
    public void submit(LatheRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        if (!state.isFormed) return;

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        int light = 15728880;
        float time = state.time;

        float r = 0.2f, g = 0.8f, b = 1.0f;
        float a = 0.6f + (float) Math.sin(time * 0.05f) * 0.2f;

        float cr = 1.0f, cg = 1.0f, cb = 1.0f;
        float prog = state.smoothProg;

        if (state.isCrafting && state.craftingEssenceType != null) {
            int colorInt = state.craftingEssenceType.getColorInt();
            cr = ((colorInt >> 16) & 0xFF) / 255.0f;
            cg = ((colorInt >> 8) & 0xFF) / 255.0f;
            cb = (colorInt & 0xFF) / 255.0f;
        }

        final float finalCr = cr;
        final float finalCg = cg;
        final float finalCb = cb;
        final float finalProg = prog;

        poseStack.pushPose();
        poseStack.translate(0.5, 0, 0.5);

        // ==========================================
        // TIMING CONSTANTS FOR THE RITUAL PHASES
        // ==========================================
        // Mapped to 500 Ticks:
        final float P_FEED_HOLD = 0.30f;   // 150 Ticks (Pause 1)
        final float P_FEED_FADE = 0.40f;   // 200 Ticks
        final float P_SPLIT_HOLD = 0.60f;  // 300 Ticks (Pause 2)
        final float P_SPLIT_FADE = 0.70f;  // 350 Ticks
        final float P_CHANNEL_HOLD = 0.85f;// 425 Ticks (Pause 3)

        Vec3 apex = new Vec3(0, 4.5, 0);
        Vec3 distPoint = new Vec3(0, 2.5, 0);
        Vec3 focalTop = new Vec3(0, 1.2, 0);

        // Calculate Alpha States for Smooth Fades
        float feedAlpha = 0f;
        float centerMassScale = 0f;
        float splitAlpha = 0f;
        float light1Alpha = 0f;
        float light2Alpha = 0f;

        if (state.isCrafting) {
            if (finalProg <= P_FEED_HOLD) {
                feedAlpha = 1.0f;
                centerMassScale = finalProg / P_FEED_HOLD;
                light1Alpha = 1.0f;
            } else if (finalProg <= P_FEED_FADE) {
                feedAlpha = 1.0f - ((finalProg - P_FEED_HOLD) / (P_FEED_FADE - P_FEED_HOLD));
                centerMassScale = 1.0f;
                light1Alpha = 1.0f;
                splitAlpha = 1.0f - feedAlpha; // Split starts as Feed ends
            } else if (finalProg <= P_SPLIT_HOLD) {
                splitAlpha = 1.0f;
                centerMassScale = 1.0f - ((finalProg - P_FEED_FADE) / (P_SPLIT_HOLD - P_FEED_FADE));
                light1Alpha = 1.0f;
            } else if (finalProg <= P_SPLIT_FADE) {
                splitAlpha = 1.0f - ((finalProg - P_SPLIT_HOLD) / (P_SPLIT_FADE - P_SPLIT_HOLD));
                light1Alpha = splitAlpha;
                light2Alpha = 1.0f - splitAlpha; // Apex Lightning starts as Split ends
            } else if (finalProg <= P_CHANNEL_HOLD) {
                light2Alpha = 1.0f;
            } else {
                light2Alpha = 1.0f - ((finalProg - P_CHANNEL_HOLD) / (1.0f - P_CHANNEL_HOLD)); // Fades out during blast
            }
        }

        // ==========================================
        // 1. DYNAMIC CRAFTING RITUAL PARTICLES
        // ==========================================
        if (state.isCrafting) {
            ResourceLocation particleTex = state.isIchorPort ? DRIP_TEXTURE : SMOKE_TEXTURE;

            // Phase 1: Feeding (Port OR Ampoules -> Center)
            if (feedAlpha > 0.01f) {

                // Draw Network Port Particles
                if (state.portOffset != null) {
                    Vec3 portLocalCenter = new Vec3(state.portOffset.x, state.portOffset.y + 0.5, state.portOffset.z);
                    Vec3 dirToCenter = new Vec3(-portLocalCenter.x, 0, -portLocalCenter.z);
                    if (dirToCenter.lengthSqr() > 0.001) {
                        dirToCenter = dirToCenter.normalize();
                    }
                    Vec3 portLocalFace = portLocalCenter.add(dirToCenter.scale(0.5));

                    for(int i = 0; i < 40; i++) {
                        float s = ((time * 0.03f) + (i / 40.0f)) % 1.0f;
                        Vec3 p = getSpiralPos(s, portLocalFace, distPoint);

                        poseStack.pushPose();
                        poseStack.translate(p.x, p.y, p.z);
                        poseStack.mulPose(cameraRenderState.orientation);
                        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180.0F));

                        final float fA = feedAlpha;
                        collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(particleTex), (pose, cons) -> {
                            drawQuad(cons, pose.pose(), 0.65f, finalCr, finalCg, finalCb, 0.9f * fA, light);
                        });
                        poseStack.popPose();
                    }
                }

                // Draw Pedestal Ampoule Particles
                if (!state.fuelPedestalOffsets.isEmpty()) {
                    for (Vec3 offset : state.fuelPedestalOffsets) {
                        Vec3 pedTop = new Vec3(offset.x, offset.y + 1.2, offset.z); // Rise from the top of the pedestal

                        for(int i = 0; i < 20; i++) {
                            float s = ((time * 0.03f) + (i / 20.0f)) % 1.0f;
                            Vec3 p = getPedestalSpiralPos(s, pedTop, distPoint);

                            poseStack.pushPose();
                            poseStack.translate(p.x, p.y, p.z);
                            poseStack.mulPose(cameraRenderState.orientation);
                            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180.0F));

                            final float fA = feedAlpha;
                            collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(SMOKE_TEXTURE), (pose, cons) -> {
                                drawQuad(cons, pose.pose(), 0.5f, finalCr, finalCg, finalCb, 0.9f * fA, light);
                            });
                            poseStack.popPose();
                        }
                    }
                }
            }

            // Central Fume Accumulation Mass
            if (centerMassScale > 0.01f) {
                poseStack.pushPose();
                poseStack.translate(distPoint.x, distPoint.y, distPoint.z);
                float massScale = centerMassScale * 0.35f + (float) Math.sin(time * 0.1f) * 0.02f;
                poseStack.scale(massScale, massScale, massScale);
                collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, cons) -> {
                    drawSphere(cons, pose.pose(), 1.0f, finalCr, finalCg, finalCb, 0.9f, light, Vec3.ZERO);
                });
                poseStack.popPose();
            }

            // Phase 2: Splitting (Center -> Nodes)
            if (splitAlpha > 0.01f) {
                for(Vec3 offset : state.pedestalOffsets) {
                    Vec3 nodePos = new Vec3(offset.x / 2.0, (offset.y + 1.0 + apex.y) / 2.0, offset.z / 2.0);

                    for(int i = 0; i < 15; i++) {
                        float s = ((time * 0.04f) + (i / 15.0f)) % 1.0f;
                        Vec3 p = distPoint.lerp(nodePos, s);

                        poseStack.pushPose();
                        poseStack.translate(p.x, p.y, p.z);
                        poseStack.mulPose(cameraRenderState.orientation);
                        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180.0F));

                        final float sA = splitAlpha;
                        collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(particleTex), (pose, cons) -> {
                            drawQuad(cons, pose.pose(), 0.45f, finalCr, finalCg, finalCb, 0.8f * sA, light);
                        });
                        poseStack.popPose();
                    }
                }
            }
        }

        // ==========================================
        // 2. THE GRAND MAGIC CIRCLE
        // ==========================================
        poseStack.pushPose();
        poseStack.translate(0, 2.5 + Math.sin(time * 0.03) * 0.1, 0);
        poseStack.pushPose();
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(time * 2.0f));

        collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, cons) -> {
            Matrix4f mat = pose.pose();

            float y0 = 0.00f;
            float y1 = 0.01f;
            float y2 = 0.02f;

            // Outer Rings
            drawFlatRing(cons, mat, 0.8f, 0.015f, 64, r, g, b, a, light, y0);
            drawFlatRing(cons, mat, 0.76f, 0.005f, 64, r, g, b, a, light, y0);

            // Inner Rings
            drawFlatRing(cons, mat, 0.56f, 0.015f, 64, r, g, b, a, light, y1);
            drawFlatRing(cons, mat, 0.54f, 0.005f, 64, r, g, b, a, light, y1);

            // Hexagram
            drawStarPolygon(cons, mat, 0.54f, 0.01f, 3, 1, 0.0f, r, g, b, a, light, y1);
            drawStarPolygon(cons, mat, 0.54f, 0.01f, 3, 1, (float)Math.PI, r, g, b, a, light, y1);

            // Mid Rings
            drawFlatRing(cons, mat, 0.32f, 0.01f, 48, r, g, b, a, light, y2);
            drawFlatRing(cons, mat, 0.30f, 0.005f, 48, r, g, b, a, light, y2);

            // Inner Pentagram
            drawStarPolygon(cons, mat, 0.30f, 0.008f, 5, 2, 0.0f, r, g, b, a, light, y2);

            // Center Ring
            drawFlatRing(cons, mat, 0.08f, 0.01f, 16, r, g, b, a, light, y2);

            // 5 Small Outer Boundary Circles
            for (int i = 0; i < 5; i++) {
                float angle = (float) (i * 2 * Math.PI / 5);
                float cx = (float) Math.cos(angle) * 0.66f;
                float cz = (float) Math.sin(angle) * 0.66f;
                drawFlatRingOffset(cons, mat, cx, cz, 0.12f, 0.01f, 32, r, g, b, a, light, y0);
            }
        });

        // --- THE TEXT / RUNE LAYERS (Double-sided!) ---
        drawFlatRunes(poseStack, bufferSource, this.font, 0.66f, 0.015f, light, time, r, g, b, a, 0.0f);

        poseStack.pushPose();
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-time * 4.0f));
        drawFlatRunes(poseStack, bufferSource, this.font, 0.43f, 0.01f, light, time, r, g, b, a, 0.01f);
        poseStack.popPose();

        for (int i = 0; i < 5; i++) {
            poseStack.pushPose();
            float angle = (float) (i * 2 * Math.PI / 5);
            poseStack.translate(Math.cos(angle) * 0.66f, 0.0f, Math.sin(angle) * 0.66f);

            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(time * 3.0f));
            drawSingleRune(poseStack, bufferSource, this.font, i, 0.02f, light, r, g, b, a);
            poseStack.popPose();
        }

        poseStack.popPose(); // End Spin
        poseStack.popPose(); // End Translation

        // ==========================================
        // 3. FLOATING INVENTORY ITEMS
        // ==========================================
        float itemScale = 0.4f;
        float hoverY = 1.6f + (float) Math.sin(time * 0.05f) * 0.1f;
        float itemRadius = 0.45f;

        for (int i = 0; i < 4; i++) {
            if (state.hasItem[i]) {
                poseStack.pushPose();
                float angle = (i * 90.0f) + (time * 3.0f);
                float rads = (float) Math.toRadians(angle);
                float xOffset = (float) Math.cos(rads) * itemRadius;
                float zOffset = (float) Math.sin(rads) * itemRadius;

                poseStack.translate(xOffset, hoverY, zOffset);
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(time * 5.0f));
                poseStack.scale(itemScale, itemScale, itemScale);
                state.items[i].submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
                poseStack.popPose();
            }
        }

        // ==========================================
        // 4. ALIGNMENT RINGS & LIGHTNING BEAMS
        // ==========================================
        int pedIndex = 0;
        for (Vec3 offset : state.pedestalOffsets) {
            poseStack.pushPose();

            Vec3 pedestalBase = new Vec3(offset.x, offset.y + 1.0, offset.z);
            Vec3 nodePos = new Vec3(offset.x / 2.0, (offset.y + 1.0 + apex.y) / 2.0, offset.z / 2.0);

            double dx = -offset.x;
            double dy = apex.y - (offset.y + 1.0);
            double dz = -offset.z;
            double distXZ = Math.sqrt(dx * dx + dz * dz);

            float yaw = (float) Math.toDegrees(Math.atan2(dx, dz));
            float pitch = (float) Math.toDegrees(Math.atan2(dy, distXZ));

            poseStack.translate(nodePos.x, nodePos.y, nodePos.z);

            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(yaw));
            poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90 - pitch));

            // Small 2D Flat Rings acting as Alignment Conduits
            poseStack.pushPose();
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(time * 6.0f));
            collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, cons) -> {
                Matrix4f mat = pose.pose();
                drawFlatRing(cons, mat, 0.175f, 0.015f, 24, r, g, b, a, light, 0.0f);
                drawFlatRing(cons, mat, 0.15f, 0.005f, 24, r, g, b, a, light, 0.0f);
            });
            poseStack.popPose();

            poseStack.pushPose();
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-time * 6.0f));
            drawFlatRunes(poseStack, bufferSource, this.font, 0.1625f, 0.006f, light, time, r, g, b, a, 0.0f);
            poseStack.popPose();

            poseStack.popPose();

            // Handle Render state for the Channeled Lightning and Nodes
            if (state.isCrafting) {
                Vec3 renderNodePos = nodePos;

                // Shake violently during Splitting and Channeling
                if (finalProg >= P_SPLIT_HOLD && finalProg <= P_CHANNEL_HOLD) {
                    float shakeStr = 0.06f;
                    float rx = (float) Math.sin(time * 30.0 + pedIndex * 13) * shakeStr;
                    float ry = (float) Math.cos(time * 27.0 + pedIndex * 7) * shakeStr;
                    float rz = (float) Math.sin(time * 33.0 + pedIndex * 11) * shakeStr;
                    renderNodePos = renderNodePos.add(rx, ry, rz);
                }

                final Vec3 finalRenderNodePos = renderNodePos;
                final long jitterSeed = (long)(time * 0.8f) + pedIndex * 1337L;
                final float l1A = light1Alpha;
                final float l2A = light2Alpha;

                collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, cons) -> {
                    Matrix4f matrix = pose.pose();

                    // PHASE 1: Lightning streams FROM the Pedestals up to the Nodes (Instantaneous)
                    if (l1A > 0.01f) {
                        drawLightning(cons, matrix, pedestalBase, finalRenderNodePos, 4, 0.02f, finalCr, finalCg, finalCb, 0.8f * l1A, light, jitterSeed);
                    }
                    // PHASE 3: Lightning streams FROM the shaking Nodes up to the Apex (Instantaneous)
                    if (l2A > 0.01f) {
                        drawLightning(cons, matrix, finalRenderNodePos, apex, 6, 0.03f, finalCr, finalCg, finalCb, 0.8f * l2A, light, jitterSeed);
                    }
                });

                // Draw the actual Orb inside the node
                float nodeOrbScale = 0f;
                if (finalProg <= P_FEED_HOLD) {
                    nodeOrbScale = (finalProg / P_FEED_HOLD) * 0.15f; // Scales up slowly as fumes feed
                } else if (finalProg <= P_SPLIT_FADE) {
                    nodeOrbScale = 0.15f;
                } else if (finalProg <= P_CHANNEL_HOLD) {
                    nodeOrbScale = 0.15f - (((finalProg - P_SPLIT_FADE)/(P_CHANNEL_HOLD - P_SPLIT_FADE)) * 0.15f); // Transfers out
                }

                if (nodeOrbScale > 0.001f) {
                    poseStack.pushPose();
                    poseStack.translate(finalRenderNodePos.x, finalRenderNodePos.y, finalRenderNodePos.z);
                    poseStack.scale(nodeOrbScale, nodeOrbScale, nodeOrbScale);
                    collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, cons) -> {
                        drawSphere(cons, pose.pose(), 1.0f, finalCr, finalCg, finalCb, 0.9f, light, Vec3.ZERO);
                    });
                    poseStack.popPose();
                }
            }
            pedIndex++;
        }

        // ==========================================
        // 5. APEX SPHERE & FINAL CONE BLAST
        // ==========================================
        if (state.isCrafting && finalProg >= P_SPLIT_FADE) {

            // Draw the swelling Apex Orb (Starts growing in Phase 3!)
            poseStack.pushPose();
            poseStack.translate(apex.x, apex.y, apex.z);

            float apexScale = 0;
            float apexAlpha = 0;

            if (finalProg <= P_CHANNEL_HOLD) {
                float phase3Prog = (finalProg - P_SPLIT_FADE) / (P_CHANNEL_HOLD - P_SPLIT_FADE);
                apexScale = 0.1f + phase3Prog * 0.2f;
                apexAlpha = phase3Prog * 0.8f;
            } else {
                float phase4Prog = (finalProg - P_CHANNEL_HOLD) / (1.0f - P_CHANNEL_HOLD);
                float shrink = Math.max(0.0f, (phase4Prog - 0.85f) / 0.15f);
                float orbFade = 1.0f - shrink;
                apexScale = (0.3f + phase4Prog * 0.4f + (float) Math.sin(time * 2.0f) * 0.05f) * orbFade;
                apexAlpha = (0.8f + (0.2f * phase4Prog)) * orbFade;
            }

            if (apexScale > 0.001f) {
                final float finalApexScale = apexScale;
                final float finalApexAlpha = apexAlpha;
                poseStack.scale(finalApexScale, finalApexScale, finalApexScale);
                collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, cons) -> {
                    drawSphere(cons, pose.pose(), 1.0f, finalCr, finalCg, finalCb, finalApexAlpha, light, Vec3.ZERO);
                });
            }
            poseStack.popPose();

            // Draw the massive, 16-sided geometric Cone Blast downwards (Phase 4 only)
            if (finalProg > P_CHANNEL_HOLD) {
                float phase4Prog = (finalProg - P_CHANNEL_HOLD) / (1.0f - P_CHANNEL_HOLD);
                float grow = Math.min(1.0f, phase4Prog / 0.15f);
                float shrink = Math.max(0.0f, (phase4Prog - 0.85f) / 0.15f);

                float clipTopY = net.minecraft.util.Mth.lerp(shrink, 4.5f, 1.2f);
                float clipBottomY = net.minecraft.util.Mth.lerp(grow, 4.5f, 1.2f);

                collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, cons) -> {
                    Matrix4f matrix = pose.pose();
                    drawClampedVerticalCone(cons, matrix, 4.5f, 2.5f, 0.0f, 0.8f, clipTopY, clipBottomY, finalCr, finalCg, finalCb, 0.9f, light);
                    drawClampedVerticalCone(cons, matrix, 2.5f, 1.2f, 0.8f, 0.05f, clipTopY, clipBottomY, finalCr, finalCg, finalCb, 0.9f, light);
                });
            }
        }

        // ==========================================
        // 6. COMPLETED WEAPON / OUTPUT ITEM
        // ==========================================
        if (state.hasOutputItem) {
            poseStack.pushPose();
            // Hover safely above the focal pedestal
            float outHoverY = 1.8f + (float) Math.sin(time * 0.05f) * 0.1f;
            poseStack.translate(0, outHoverY, 0);

            float spinRotation = (time % 4000L) / 4000.0f * 360.0f;
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(spinRotation));

            poseStack.scale(0.6f, 0.6f, 0.6f);
            state.outputItem.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }

        poseStack.popPose();
        bufferSource.endBatch();
    }

    private Vec3 getSpiralPos(float s, Vec3 portLocal, Vec3 distPoint) {
        Vec3 dirToCenter = new Vec3(-portLocal.x, 0, -portLocal.z);
        if (dirToCenter.lengthSqr() < 0.001) {
            return portLocal.lerp(distPoint, s);
        }
        dirToCenter = dirToCenter.normalize();

        float straightSegment = 0.20f;
        Vec3 straightEnd = portLocal.add(dirToCenter.scale(1.5));

        if (s < straightSegment) {
            float t = s / straightSegment;
            return portLocal.lerp(straightEnd, t);
        } else {
            float t = (s - straightSegment) / (1.0f - straightSegment);
            float radius = (float) Math.sqrt(straightEnd.x * straightEnd.x + straightEnd.z * straightEnd.z) * (1.0f - t);
            float startAngle = (float) Math.atan2(straightEnd.z, straightEnd.x);

            float rotDir = (portLocal.x > 0 || portLocal.z > 0) ? 1.0f : -1.0f;
            float angle = startAngle + t * (float)Math.PI * 4.0f * rotDir;

            float y = (float) (straightEnd.y + (distPoint.y - straightEnd.y) * t);
            return new Vec3(Math.cos(angle) * radius, y, Math.sin(angle) * radius);
        }
    }

    // Helper for Pedestal Ampoules to spiral upwards towards the center
    private Vec3 getPedestalSpiralPos(float s, Vec3 start, Vec3 distPoint) {
        float straightSegment = 0.20f;
        Vec3 straightEnd = start.add(0, 1.0, 0); // Burst straight up from the glass bottle 1 block

        if (s < straightSegment) {
            float t = s / straightSegment;
            return start.lerp(straightEnd, t);
        } else {
            float t = (s - straightSegment) / (1.0f - straightSegment);
            float radius = (float) Math.sqrt(straightEnd.x * straightEnd.x + straightEnd.z * straightEnd.z) * (1.0f - t);
            float startAngle = (float) Math.atan2(straightEnd.z, straightEnd.x);
            float angle = startAngle + t * (float)Math.PI * 4.0f;

            float y = (float) (straightEnd.y + (distPoint.y - straightEnd.y) * t);
            return new Vec3(Math.cos(angle) * radius, y, Math.sin(angle) * radius);
        }
    }

    // ==========================================
    // 2D PLANAR GEOMETRY HELPERS
    // ==========================================

    private void drawFlatLine(VertexConsumer consumer, Matrix4f matrix, float x1, float z1, float x2, float z2, float width, float r, float g, float b, float a, int light, float y) {
        float dx = x2 - x1;
        float dz = z2 - z1;
        float len = (float) Math.sqrt(dx * dx + dz * dz);
        if (len < 0.0001f) return;

        float nx = -dz / len * (width / 2.0f);
        float nz = dx / len * (width / 2.0f);

        consumer.addVertex(matrix, x1 + nx, y, z1 + nz).setColor(r, g, b, a).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(matrix, x1 - nx, y, z1 - nz).setColor(r, g, b, a).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(matrix, x2 - nx, y, z2 - nz).setColor(r, g, b, a).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(matrix, x2 + nx, y, z2 + nz).setColor(r, g, b, a).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);
    }

    private void drawFlatRing(VertexConsumer consumer, Matrix4f matrix, float radius, float width, int segments, float r, float g, float b, float a, int light, float y) {
        drawFlatRingOffset(consumer, matrix, 0, 0, radius, width, segments, r, g, b, a, light, y);
    }

    private void drawFlatRingOffset(VertexConsumer consumer, Matrix4f matrix, float cx, float cz, float radius, float width, int segments, float r, float g, float b, float a, int light, float y) {
        float halfWidth = width / 2.0f;
        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (i * 2 * Math.PI / segments);
            float angle2 = (float) ((i + 1) * 2 * Math.PI / segments);

            float c1 = (float) Math.cos(angle1);
            float s1 = (float) Math.sin(angle1);
            float c2 = (float) Math.cos(angle2);
            float s2 = (float) Math.sin(angle2);

            float x1_in = cx + c1 * (radius - halfWidth);
            float z1_in = cz + s1 * (radius - halfWidth);
            float x1_out = cx + c1 * (radius + halfWidth);
            float z1_out = cz + s1 * (radius + halfWidth);

            float x2_in = cx + c2 * (radius - halfWidth);
            float z2_in = cz + s2 * (radius - halfWidth);
            float x2_out = cx + c2 * (radius + halfWidth);
            float z2_out = cz + s2 * (radius + halfWidth);

            consumer.addVertex(matrix, x1_out, y, z1_out).setColor(r, g, b, a).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);
            consumer.addVertex(matrix, x1_in, y, z1_in).setColor(r, g, b, a).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);
            consumer.addVertex(matrix, x2_in, y, z2_in).setColor(r, g, b, a).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);
            consumer.addVertex(matrix, x2_out, y, z2_out).setColor(r, g, b, a).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);
        }
    }

    private void drawStarPolygon(VertexConsumer consumer, Matrix4f matrix, float radius, float width, int points, int step, float angleOffset, float r, float g, float b, float a, int light, float y) {
        for (int i = 0; i < points; i++) {
            float a1 = angleOffset + (float) (i * 2 * Math.PI / points);
            float a2 = angleOffset + (float) (((i + step) % points) * 2 * Math.PI / points);

            float x1 = (float) Math.cos(a1) * radius;
            float z1 = (float) Math.sin(a1) * radius;
            float x2 = (float) Math.cos(a2) * radius;
            float z2 = (float) Math.sin(a2) * radius;

            drawFlatLine(consumer, matrix, x1, z1, x2, z2, width, r, g, b, a, light, y);
        }
    }

    // ==========================================
    // FLAT TEXT RENDERERS
    // ==========================================

    private void drawFlatRunes(PoseStack poseStack, MultiBufferSource bufferSource, Font font, float radius, float textScale, int light, float time, float r, float g, float b, float a, float yOffset) {
        String RUNES = "ᚠᚢᚦᚨᚱᚲᚷᚹᚺᚾᛁᛃᛇᛈᛉᛊᛏᛒᛖᛗᛚᛜᛞᛟ";
        int len = RUNES.length();

        for (int i = 0; i < len; i++) {
            poseStack.pushPose();

            float angle = (i * 360.0f / len);
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-angle));
            poseStack.translate(radius, yOffset, 0);

            // Orient perfectly flat on the XZ plane
            poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90));
            poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(90));

            poseStack.scale(-textScale, -textScale, textScale);

            float alphaPulse = a * (0.5f + (float) Math.sin(time * 0.1f + i) * 0.5f);
            int alpha = (int) (alphaPulse * 255);
            int color = (alpha << 24) | (((int)(r*255)) << 16) | (((int)(g*255)) << 8) | ((int)(b*255));

            String charStr = String.valueOf(RUNES.charAt(i));
            float width = font.width(charStr);

            drawDoubleSidedText(poseStack, bufferSource, font, charStr, width, color, light);

            poseStack.popPose();
        }
    }

    private void drawSingleRune(PoseStack poseStack, MultiBufferSource bufferSource, Font font, int index, float textScale, int light, float r, float g, float b, float a) {
        String RUNES = "ᚠᚢᚦᚨᚱ";
        float alphaPulse = a * (0.8f + (float) Math.sin(System.currentTimeMillis() / 200.0) * 0.2f);
        int alpha = (int) (alphaPulse * 255);
        int color = (alpha << 24) | (((int)(r*255)) << 16) | (((int)(g*255)) << 8) | ((int)(b*255));

        poseStack.pushPose();
        poseStack.translate(0, 0.01f, 0);
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90));
        poseStack.scale(-textScale, -textScale, textScale);

        String charStr = String.valueOf(RUNES.charAt(index % RUNES.length()));
        float width = font.width(charStr);

        drawDoubleSidedText(poseStack, bufferSource, font, charStr, width, color, light);

        poseStack.popPose();
    }

    private void drawDoubleSidedText(PoseStack poseStack, MultiBufferSource bufferSource, Font font, String text, float width, int color, int light) {
        font.drawInBatch(text, -width / 2.0f, -font.lineHeight / 2.0f, color, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, light);

        poseStack.pushPose();
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(180));
        font.drawInBatch(text, -width / 2.0f, -font.lineHeight / 2.0f, color, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, light);
        poseStack.popPose();
    }

    // ==========================================
    // 3D GEOMETRY HELPERS
    // ==========================================

    private void drawQuad(VertexConsumer consumer, Matrix4f matrix, float size, float r, float g, float b, float a, int light) {
        float half = size / 2.0f;
        consumer.addVertex(matrix, -half, -half, 0.0F).setColor(r, g, b, a).setUv(0.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0.0F, 1.0F, 0.0F);
        consumer.addVertex(matrix, half, -half, 0.0F).setColor(r, g, b, a).setUv(1.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0.0F, 1.0F, 0.0F);
        consumer.addVertex(matrix, half, half, 0.0F).setColor(r, g, b, a).setUv(1.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0.0F, 1.0F, 0.0F);
        consumer.addVertex(matrix, -half, half, 0.0F).setColor(r, g, b, a).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0.0F, 1.0F, 0.0F);
    }

    private void drawLightning(VertexConsumer consumer, Matrix4f matrix, Vec3 start, Vec3 end, int segments, float width, float r, float g, float b, float a, int light, long seed) {
        java.util.Random rand = new java.util.Random(seed);
        Vec3 dir = end.subtract(start);
        float len = (float) dir.length();
        if (len < 0.001) return;
        Vec3 norm = dir.normalize();

        Vec3 up = new Vec3(0, 1, 0);
        if (Math.abs(norm.y) > 0.9) up = new Vec3(1, 0, 0);
        Vec3 right = norm.cross(up).normalize();
        Vec3 upOrth = right.cross(norm).normalize();

        Vec3 current = start;
        float step = len / segments;

        for (int i = 0; i < segments; i++) {
            Vec3 nextBase = start.add(norm.scale(step * (i + 1)));
            Vec3 next;
            if (i == segments - 1) {
                next = end; // Guarantee we hit the exact target destination
            } else {
                // Generates up to a 0.5 block visual offset jitter
                float rx = (rand.nextFloat() - 0.5f) * 0.5f;
                float ry = (rand.nextFloat() - 0.5f) * 0.5f;
                next = nextBase.add(right.scale(rx)).add(upOrth.scale(ry));
            }
            drawBeam(consumer, matrix, current, next, width, width, r, g, b, a, light);
            current = next;
        }
    }

    private void drawClampedVerticalCone(VertexConsumer consumer, Matrix4f matrix, float yTop, float yBottom, float rTop, float rBottom, float clipTop, float clipBottom, float r, float g, float b, float a, int light) {
        float actualTop = Math.min(yTop, clipTop);
        float actualBottom = Math.max(yBottom, clipBottom);

        if (actualTop <= actualBottom) return; // Completely clipped out

        // Interpolate the radius based on where the beam currently is in this specific segment
        float tTop = (actualTop - yBottom) / (yTop - yBottom);
        float actualRTop = rBottom + (rTop - rBottom) * tTop;

        float tBottom = (actualBottom - yBottom) / (yTop - yBottom);
        float actualRBottom = rBottom + (rTop - rBottom) * tBottom;

        drawCone(consumer, matrix, actualTop, actualBottom, actualRTop, actualRBottom, r, g, b, a, light);
    }

    private void drawCone(VertexConsumer consumer, Matrix4f matrix, float yTop, float yBottom, float rTop, float rBottom, float r, float g, float b, float a, int light) {
        int segments = 16; // 16-sided perfectly round cone
        for (int i = 0; i < segments; i++) {
            float theta1 = (float) (i * 2 * Math.PI / segments);
            float theta2 = (float) ((i + 1) * 2 * Math.PI / segments);

            float x1Top = (float) Math.cos(theta1) * rTop;
            float z1Top = (float) Math.sin(theta1) * rTop;
            float x2Top = (float) Math.cos(theta2) * rTop;
            float z2Top = (float) Math.sin(theta2) * rTop;

            float x1Bot = (float) Math.cos(theta1) * rBottom;
            float z1Bot = (float) Math.sin(theta1) * rBottom;
            float x2Bot = (float) Math.cos(theta2) * rBottom;
            float z2Bot = (float) Math.sin(theta2) * rBottom;

            // Draw a quad mapping the curved segment
            consumer.addVertex(matrix, x1Bot, yBottom, z1Bot).setColor(r, g, b, a).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);
            consumer.addVertex(matrix, x2Bot, yBottom, z2Bot).setColor(r, g, b, a).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);
            consumer.addVertex(matrix, x2Top, yTop, z2Top).setColor(r, g, b, a).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);
            consumer.addVertex(matrix, x1Top, yTop, z1Top).setColor(r, g, b, a).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);
        }
    }

    private void drawBeam(VertexConsumer consumer, Matrix4f matrix, Vec3 start, Vec3 end, float startWidth, float endWidth, float r, float g, float b, float a, int light) {
        Vec3 dir = end.subtract(start);
        if (dir.lengthSqr() < 0.0001) return;
        dir = dir.normalize();

        Vec3 up = new Vec3(0, 1, 0);
        if (Math.abs(dir.y) > 0.9) up = new Vec3(1, 0, 0);

        Vec3 right = dir.cross(up).normalize();
        Vec3 upOrth = right.cross(dir).normalize();

        Vec3[] startOffsets = {
                right.scale(startWidth),
                upOrth.scale(startWidth),
                right.scale(-startWidth),
                upOrth.scale(-startWidth)
        };
        Vec3[] endOffsets = {
                right.scale(endWidth),
                upOrth.scale(endWidth),
                right.scale(-endWidth),
                upOrth.scale(-endWidth)
        };

        for (int i = 0; i < 4; i++) {
            Vec3 so1 = startOffsets[i];
            Vec3 so2 = startOffsets[(i + 1) % 4];
            Vec3 eo1 = endOffsets[i];
            Vec3 eo2 = endOffsets[(i + 1) % 4];

            consumer.addVertex(matrix, (float)(start.x + so1.x), (float)(start.y + so1.y), (float)(start.z + so1.z)).setColor(r, g, b, a).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0,1,0);
            consumer.addVertex(matrix, (float)(end.x + eo1.x), (float)(end.y + eo1.y), (float)(end.z + eo1.z)).setColor(r, g, b, a).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0,1,0);
            consumer.addVertex(matrix, (float)(end.x + eo2.x), (float)(end.y + eo2.y), (float)(end.z + eo2.z)).setColor(r, g, b, a).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0,1,0);
            consumer.addVertex(matrix, (float)(start.x + so2.x), (float)(start.y + so2.y), (float)(start.z + so2.z)).setColor(r, g, b, a).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0,1,0);
        }
    }

    private void drawSphere(VertexConsumer consumer, Matrix4f matrix, float radius, float r, float g, float b, float a, int light, Vec3 offset) {
        int lats = 12;
        int longs = 12;
        int overlay = OverlayTexture.NO_OVERLAY;
        for (int i = 0; i < lats; i++) {
            float lat0 = (float) (Math.PI * (-0.5 + (double) i / lats));
            float z0 = radius * (float) Math.sin(lat0);
            float zr0 = radius * (float) Math.cos(lat0);

            float lat1 = (float) (Math.PI * (-0.5 + (double) (i + 1) / lats));
            float z1 = radius * (float) Math.sin(lat1);
            float zr1 = radius * (float) Math.cos(lat1);

            for (int j = 0; j < longs; j++) {
                float lng = (float) (2 * Math.PI * (double) j / longs);
                float x = (float) Math.cos(lng);
                float y = (float) Math.sin(lng);

                float lng1 = (float) (2 * Math.PI * (double) (j + 1) / longs);
                float x1 = (float) Math.cos(lng1);
                float y1 = (float) Math.sin(lng1);

                consumer.addVertex(matrix, x * zr0 + (float)offset.x, z0 + (float)offset.y, y * zr0 + (float)offset.z).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(x, z0/radius, y);
                consumer.addVertex(matrix, x1 * zr0 + (float)offset.x, z0 + (float)offset.y, y1 * zr0 + (float)offset.z).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(x1, z0/radius, y1);
                consumer.addVertex(matrix, x1 * zr1 + (float)offset.x, z1 + (float)offset.y, y1 * zr1 + (float)offset.z).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(x1, z1/radius, y1);
                consumer.addVertex(matrix, x * zr1 + (float)offset.x, z1 + (float)offset.y, y * zr1 + (float)offset.z).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(x, z1/radius, y);
            }
        }
    }

    public static class LatheRenderState extends BlockEntityRenderState {
        public boolean isFormed;
        public float time;

        public final List<Vec3> pedestalOffsets = new ArrayList<>();
        public final List<Vec3> fuelPedestalOffsets = new ArrayList<>();

        public final ItemStackRenderState[] items = new ItemStackRenderState[4];
        public final boolean[] hasItem = new boolean[4];

        public boolean isCrafting;
        public boolean waitForClick;
        public int craftingProgress;
        public int maxCraftingProgress;
        public EssenceType craftingEssenceType;
        public Vec3 portOffset;
        public boolean isIchorPort;

        public float smoothProg;

        public final ItemStackRenderState outputItem = new ItemStackRenderState();
        public boolean hasOutputItem = false;

        public LatheRenderState() {
            for (int i = 0; i < 4; i++) {
                items[i] = new ItemStackRenderState();
            }
        }
    }
}