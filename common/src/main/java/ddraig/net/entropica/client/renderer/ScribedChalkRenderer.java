package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.block.ScribedChalkBlock;
import ddraig.net.entropica.block.entity.ScribedChalkBlockEntity;
import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class ScribedChalkRenderer implements BlockEntityRenderer<ScribedChalkBlockEntity, ScribedChalkRenderer.ChalkRenderState> {
    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    private final ItemModelResolver itemModelResolver;

    public ScribedChalkRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public ChalkRenderState createRenderState() {
        return new ChalkRenderState();
    }

    @Override
    public void extractRenderState(ScribedChalkBlockEntity be, ChalkRenderState renderState, float partialTick, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, renderState, crumblingOverlay);
        
        Level level = be.getLevel();
        BlockState state = level != null ? level.getBlockState(be.getBlockPos()) : be.getBlockState();
        if (state.is(ddraig.net.entropica.registry.ModBlocks.SCRIBED_CHALK.get())) {
            renderState.nodeType = state.getValue(ScribedChalkBlock.NODE_TYPE);
            renderState.isCircuit = state.getValue(ScribedChalkBlock.CIRCUIT);
        } else {
            renderState.nodeType = ScribedChalkBlock.NodeType.DEFAULT;
            renderState.isCircuit = false;
        }
        renderState.facing = be.getFacing();
        renderState.dyeTicks = be.getDyeTicks() + partialTick;
        renderState.hasActiveDye = be.getActiveAffinity() != EssenceType.REGULAR;
        renderState.time = (level != null ? (level.getGameTime() % 360000L) : 0) + partialTick;

        // Set connections
        if (level != null) {
            BlockPos pos = be.getBlockPos();
            boolean selfInCircle = be.isInActiveCircle();
            
            boolean northChalk = shouldConnectSmart8(level, pos, ScribedChalkBlockEntity.Direction8.NORTH, renderState.isCircuit);
            if (northChalk && selfInCircle) {
                BlockEntity nBE = level.getBlockEntity(pos.north());
                if (nBE instanceof ScribedChalkBlockEntity neighborChalk && neighborChalk.isInActiveCircle()) {
                    northChalk = false;
                }
            }
            renderState.connectNorth = northChalk;

            boolean southChalk = shouldConnectSmart8(level, pos, ScribedChalkBlockEntity.Direction8.SOUTH, renderState.isCircuit);
            if (southChalk && selfInCircle) {
                BlockEntity sBE = level.getBlockEntity(pos.south());
                if (sBE instanceof ScribedChalkBlockEntity neighborChalk && neighborChalk.isInActiveCircle()) {
                    southChalk = false;
                }
            }
            renderState.connectSouth = southChalk;

            boolean eastChalk = shouldConnectSmart8(level, pos, ScribedChalkBlockEntity.Direction8.EAST, renderState.isCircuit);
            if (eastChalk && selfInCircle) {
                BlockEntity eBE = level.getBlockEntity(pos.east());
                if (eBE instanceof ScribedChalkBlockEntity neighborChalk && neighborChalk.isInActiveCircle()) {
                    eastChalk = false;
                }
            }
            renderState.connectEast = eastChalk;

            boolean westChalk = shouldConnectSmart8(level, pos, ScribedChalkBlockEntity.Direction8.WEST, renderState.isCircuit);
            if (westChalk && selfInCircle) {
                BlockEntity wBE = level.getBlockEntity(pos.west());
                if (wBE instanceof ScribedChalkBlockEntity neighborChalk && neighborChalk.isInActiveCircle()) {
                    westChalk = false;
                }
            }
            renderState.connectWest = westChalk;

            boolean neChalk = shouldConnectSmart8(level, pos, ScribedChalkBlockEntity.Direction8.NORTH_EAST, renderState.isCircuit);
            if (neChalk && selfInCircle) {
                BlockEntity neBE = level.getBlockEntity(pos.offset(1, 0, -1));
                if (neBE instanceof ScribedChalkBlockEntity neighborChalk && neighborChalk.isInActiveCircle()) {
                    neChalk = false;
                }
            }
            renderState.connectNorthEast = neChalk;

            boolean nwChalk = shouldConnectSmart8(level, pos, ScribedChalkBlockEntity.Direction8.NORTH_WEST, renderState.isCircuit);
            if (nwChalk && selfInCircle) {
                BlockEntity nwBE = level.getBlockEntity(pos.offset(-1, 0, -1));
                if (nwBE instanceof ScribedChalkBlockEntity neighborChalk && neighborChalk.isInActiveCircle()) {
                    nwChalk = false;
                }
            }
            renderState.connectNorthWest = nwChalk;

            boolean seChalk = shouldConnectSmart8(level, pos, ScribedChalkBlockEntity.Direction8.SOUTH_EAST, renderState.isCircuit);
            if (seChalk && selfInCircle) {
                BlockEntity seBE = level.getBlockEntity(pos.offset(1, 0, 1));
                if (seBE instanceof ScribedChalkBlockEntity neighborChalk && neighborChalk.isInActiveCircle()) {
                    seChalk = false;
                }
            }
            renderState.connectSouthEast = seChalk;

            boolean swChalk = shouldConnectSmart8(level, pos, ScribedChalkBlockEntity.Direction8.SOUTH_WEST, renderState.isCircuit);
            if (swChalk && selfInCircle) {
                BlockEntity swBE = level.getBlockEntity(pos.offset(-1, 0, 1));
                if (swBE instanceof ScribedChalkBlockEntity neighborChalk && neighborChalk.isInActiveCircle()) {
                    swChalk = false;
                }
            }
            renderState.connectSouthWest = swChalk;

            // Compute animated color via BFS
            int defaultColor = getDefaultTierColor(state);
            renderState.defaultColor = defaultColor;
            int targetColor = defaultColor;
            if (be.getActiveAffinity() != EssenceType.REGULAR) {
                targetColor = getDynamicColor(be, renderState.time);
            }

            int distance = findDistanceToDyeSource(be, renderState.isCircuit);
            renderState.distance = distance;
            if (distance >= 0 && renderState.hasActiveDye) {
                float propagationTicks = distance * 8.0f;
                if (renderState.dyeTicks < propagationTicks) {
                    renderState.color = defaultColor;
                } else if (renderState.dyeTicks > propagationTicks + 15.0f) {
                    renderState.color = targetColor;
                } else {
                    float factor = (renderState.dyeTicks - propagationTicks) / 15.0f;
                    renderState.color = lerpColor(defaultColor, targetColor, factor);
                }
            } else {
                renderState.color = defaultColor;
            }

            // Circle detection on OUTPUT center node
            renderState.isInMagicCircle = be.isInActiveCircle();
            renderState.circleTier = 0;
            renderState.circleHasActiveDye = false;
            renderState.circleDyeColor = 0xFFCCCCCC;
            renderState.circleDyeTicks = 0.0f;
            renderState.circleDyeSourceAngle = 0.0f;

            if (renderState.nodeType == ScribedChalkBlock.NodeType.OUTPUT && renderState.isInMagicCircle) {
                int tier = detectCircleTier(level, pos, renderState.isCircuit);
                if (tier > 0) {
                    renderState.circleTier = tier;
                    // Scan all concentric layers up to tier for active dyeing input
                    for (int t = 1; t <= tier; t++) {
                        int[][] offsets = ScribedChalkBlock.getOffsetsForTier(t);
                        for (int[] offset : offsets) {
                            BlockPos p = pos.offset(offset[0], 0, offset[1]);
                            BlockEntity neighborBE = level.getBlockEntity(p);
                            if (neighborBE instanceof ScribedChalkBlockEntity neighborChalk) {
                                if (neighborChalk.getActiveAffinity() != EssenceType.REGULAR) {
                                    renderState.circleHasActiveDye = true;
                                    renderState.circleDyeColor = getDynamicColor(neighborChalk, renderState.time);
                                    renderState.circleDyeTicks = be.getDyeTicks() + partialTick;
                                    renderState.circleDyeSourceAngle = (float) Math.atan2(p.getZ() - pos.getZ(), p.getX() - pos.getX());
                                    break;
                                }
                            }
                        }
                        if (renderState.circleHasActiveDye) break;
                    }

                    // Extract all perimeter nodes for rotation across ALL layers!
                    renderState.rotatingNodes.clear();
                    java.util.List<ScribedChalkBlockEntity> perimeterNodeBEs = new java.util.ArrayList<>();
                    for (int t = 1; t <= tier; t++) {
                        int[][] offsets = ScribedChalkBlock.getOffsetsForTier(t);
                        for (int[] offset : offsets) {
                            BlockPos p = pos.offset(offset[0], 0, offset[1]);
                            BlockEntity neighborBE = level.getBlockEntity(p);
                            if (neighborBE instanceof ScribedChalkBlockEntity neighborChalk) {
                                BlockState neighborBS = level.getBlockState(p);
                                if (neighborBS.is(ModBlocks.SCRIBED_CHALK.get()) && 
                                    neighborBS.getValue(ScribedChalkBlock.NODE_TYPE) != ScribedChalkBlock.NodeType.DEFAULT) {
                                    perimeterNodeBEs.add(neighborChalk);
                                }
                            }
                        }
                    }

                    // Sort perimeter nodes by angle around center
                    BlockPos centerPos = pos;
                    perimeterNodeBEs.sort((be1, be2) -> {
                        double a1 = Math.atan2(be1.getBlockPos().getZ() - centerPos.getZ(), be1.getBlockPos().getX() - centerPos.getX());
                        double a2 = Math.atan2(be2.getBlockPos().getZ() - centerPos.getZ(), be2.getBlockPos().getX() - centerPos.getX());
                        if (a1 < 0) a1 += 2 * Math.PI;
                        if (a2 < 0) a2 += 2 * Math.PI;
                        return Double.compare(a1, a2);
                    });

                    // Populate rotatingNodes render state
                    for (ScribedChalkBlockEntity nodeBE : perimeterNodeBEs) {
                        ChalkRenderState.RotatingNodeState nodeState = new ChalkRenderState.RotatingNodeState();
                        BlockState nodeBS = level.getBlockState(nodeBE.getBlockPos());
                        if (nodeBS.is(ModBlocks.SCRIBED_CHALK.get())) {
                            nodeState.nodeType = nodeBS.getValue(ScribedChalkBlock.NODE_TYPE);
                        } else {
                            nodeState.nodeType = ScribedChalkBlock.NodeType.DEFAULT;
                        }
                        
                        String text = "";
                        if (nodeState.nodeType == ScribedChalkBlock.NodeType.INPUT) text = "I";
                        else if (nodeState.nodeType == ScribedChalkBlock.NodeType.SOURCE) text = "S";
                        else if (nodeState.nodeType == ScribedChalkBlock.NodeType.COLLECTION) text = "C";
                        else if (nodeState.nodeType == ScribedChalkBlock.NodeType.AMPLIFIER) text = "A";
                        else if (nodeState.nodeType == ScribedChalkBlock.NodeType.CAPACITOR) text = "P";
                        else if (nodeState.nodeType == ScribedChalkBlock.NodeType.RESONATOR) text = "R";
                        else if (nodeState.nodeType == ScribedChalkBlock.NodeType.DIODE) text = "D";
                        else if (nodeState.nodeType == ScribedChalkBlock.NodeType.AND_GATE) text = "&";
                        else if (nodeState.nodeType == ScribedChalkBlock.NodeType.OR_GATE) text = "|";
                        else if (nodeState.nodeType == ScribedChalkBlock.NodeType.NOT_GATE) text = "!";
                        else if (nodeState.nodeType == ScribedChalkBlock.NodeType.OUTPUT) text = "O";
                        else if (nodeState.nodeType == ScribedChalkBlock.NodeType.ESSENCE_BANK) text = "B";
                        else if (nodeState.nodeType == ScribedChalkBlock.NodeType.EXTRACTION) text = "X";
                        else if (nodeState.nodeType == ScribedChalkBlock.NodeType.DELAY) text = String.valueOf(nodeBE.getDelayTicks());
                        else if (nodeState.nodeType == ScribedChalkBlock.NodeType.RUNE) {
                            ItemStack r = nodeBE.getStoredRune();
                            text = (r.getItem() instanceof ddraig.net.entropica.item.RuneItem runeItem) ? runeItem.getUnicodeChar() : "E";
                        }
                        nodeState.textSymbol = text;
                        
                        int defCol = getDefaultTierColor(nodeBS);
                        nodeState.color = nodeBE.getActiveAffinity() != EssenceType.REGULAR ? getDynamicColor(nodeBE, renderState.time) : defCol;
                        nodeState.isProcessing = nodeBE.isProcessing();
                        nodeState.essenceLevel = nodeBE.getEssenceLevel();
                        
                        ItemStack cellItem = nodeBE.getStoredOrbisCell();
                        nodeState.hasStoredOrbisCell = !cellItem.isEmpty();
                        if (nodeState.hasStoredOrbisCell) {
                            int seed = (int) nodeBE.getBlockPos().asLong();
                            this.itemModelResolver.updateForTopItem(nodeState.storedOrbisCellState, cellItem, net.minecraft.world.item.ItemDisplayContext.GROUND, level, null, seed);
                        }

                        ItemStack runeItem = nodeBE.getStoredRune();
                        nodeState.hasStoredRune = !runeItem.isEmpty();
                        if (nodeState.hasStoredRune) {
                            int seed = (int) nodeBE.getBlockPos().asLong();
                            this.itemModelResolver.updateForTopItem(nodeState.storedRuneState, runeItem, net.minecraft.world.item.ItemDisplayContext.GROUND, level, null, seed);
                        }

                        ItemStack inputItem = nodeBE.getStoredItem();
                        nodeState.hasStoredItem = !inputItem.isEmpty();
                        if (nodeState.hasStoredItem) {
                            int seed = (int) nodeBE.getBlockPos().asLong();
                            this.itemModelResolver.updateForTopItem(nodeState.storedItemState, inputItem, net.minecraft.world.item.ItemDisplayContext.GROUND, level, null, seed);
                        }
                        
                        renderState.rotatingNodes.add(nodeState);
                    }
                }
            }
        } else {
            renderState.color = getDynamicColor(be, renderState.time);
            renderState.connectNorth = false;
            renderState.connectSouth = false;
            renderState.connectEast = false;
            renderState.connectWest = false;
            renderState.circleTier = 0;
        }

        // Extract basic fields
        renderState.isProcessing = be.isProcessing();
        renderState.processingProgress = be.getProcessingProgress();
        renderState.processingTimeTotal = be.getProcessingTimeTotal();
        renderState.essenceLevel = be.getEssenceLevel();
        renderState.propagationStrength = be.getPropagationStrength();
        renderState.delayTicks = be.getDelayTicks();
        renderState.timePulseOffset = (level != null ? (level.getGameTime() % 40) / 40.0f : 0.0f);

        BlockPos nextNodePos = null;
        if (renderState.isCircuit && renderState.nodeType != ScribedChalkBlock.NodeType.DEFAULT) {
            int currentStrength = be.getPropagationStrength();
            if (currentStrength > 0 && level != null) {
                BlockPos currentPos = be.getBlockPos();
                ScribedChalkBlockEntity currentBE = be;
                for (int step = 0; step < 32; step++) {
                    Direction nextStepDir = null;
                    int bestStrength = -1;
                    for (Direction dir : Direction.values()) {
                        if (dir.getAxis().isHorizontal()) {
                            BlockPos checkPos = currentPos.relative(dir);
                            BlockEntity checkBE = level.getBlockEntity(checkPos);
                            if (checkBE instanceof ScribedChalkBlockEntity neighborChalk) {
                                int neighborStrength = neighborChalk.getPropagationStrength();
                                if (neighborStrength > 0 && neighborStrength < currentBE.getPropagationStrength()) {
                                    if (neighborStrength > bestStrength) {
                                        bestStrength = neighborStrength;
                                        nextStepDir = dir;
                                    }
                                }
                            }
                        }
                    }
                    if (nextStepDir != null) {
                        currentPos = currentPos.relative(nextStepDir);
                        BlockEntity stepBE = level.getBlockEntity(currentPos);
                        if (stepBE instanceof ScribedChalkBlockEntity stepChalk) {
                            currentBE = stepChalk;
                            if (level.getBlockState(currentPos).getValue(ScribedChalkBlock.NODE_TYPE) != ScribedChalkBlock.NodeType.DEFAULT) {
                                nextNodePos = currentPos;
                                break;
                            }
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }
        }
        if (nextNodePos != null) {
            renderState.targetNodeOffset = new Vec3(
                nextNodePos.getX() - be.getBlockPos().getX(),
                0,
                nextNodePos.getZ() - be.getBlockPos().getZ()
            );
        } else {
            renderState.targetNodeOffset = null;
        }

        // Get Orbis Cell
        ItemStack cell = be.getStoredOrbisCell();
        renderState.hasStoredOrbisCell = !cell.isEmpty();
        if (renderState.hasStoredOrbisCell && be.getLevel() != null) {
            int seed = (int) be.getBlockPos().asLong();
            this.itemModelResolver.updateForTopItem(renderState.storedOrbisCellState, cell, ItemDisplayContext.GROUND, be.getLevel(), null, seed);
        } else {
            renderState.storedOrbisCellState.clear();
        }

        // Get Rune
        ItemStack rune = be.getStoredRune();
        renderState.hasStoredRune = !rune.isEmpty();
        if (renderState.hasStoredRune && be.getLevel() != null) {
            renderState.runeUnicode = (rune.getItem() instanceof ddraig.net.entropica.item.RuneItem runeItem) ? runeItem.getUnicodeChar() : "";
            int seed = (int) be.getBlockPos().asLong();
            this.itemModelResolver.updateForTopItem(renderState.storedRuneState, rune, ItemDisplayContext.GROUND, be.getLevel(), null, seed);
        } else {
            renderState.runeUnicode = "";
            renderState.storedRuneState.clear();
        }

        // Get Stored Item
        ItemStack stored = be.getStoredItem();
        renderState.hasStoredItem = !stored.isEmpty();
        if (renderState.hasStoredItem && be.getLevel() != null) {
            int seed = (int) be.getBlockPos().asLong();
            this.itemModelResolver.updateForTopItem(renderState.storedItemState, stored, ItemDisplayContext.GROUND, be.getLevel(), null, seed);
        } else {
            renderState.storedItemState.clear();
        }
    }

    private int getDefaultTierColor(BlockState state) {
        if (state.hasProperty(ScribedChalkBlock.TIER)) {
            int tier = state.getValue(ScribedChalkBlock.TIER);
            return switch (tier) {
                case 2 -> 0xFFD87040;
                case 3 -> 0xFFB040B0;
                case 4 -> 0xFF40B0E0;
                default -> 0xFFCCCCCC;
            };
        }
        return 0xFFCCCCCC;
    }

    private int findDistanceToDyeSource(ScribedChalkBlockEntity startBE, boolean circuit) {
        Level level = startBE.getLevel();
        if (level == null) return -1;

        BlockPos start = startBE.getBlockPos();
        java.util.Queue<BlockPos> queue = new java.util.LinkedList<>();
        java.util.Set<BlockPos> visited = new java.util.HashSet<>();
        java.util.Map<BlockPos, Integer> distances = new java.util.HashMap<>();

        queue.add(start);
        visited.add(start);
        distances.put(start, 0);

        int maxDepth = 16;

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            int currentDist = distances.get(current);

            BlockEntity currentBE = level.getBlockEntity(current);
            if (currentBE instanceof ScribedChalkBlockEntity chalkBE) {
                BlockState currentState = chalkBE.getBlockState();
                if (currentState.is(ModBlocks.SCRIBED_CHALK.get()) &&
                    chalkBE.getActiveAffinity() != EssenceType.REGULAR && 
                    currentState.getValue(ScribedChalkBlock.NODE_TYPE) == ScribedChalkBlock.NodeType.SOURCE) {
                    return currentDist;
                }

                if (currentDist < maxDepth) {
                    for (Direction dir : Direction.Plane.HORIZONTAL) {
                        BlockPos neighbor = current.relative(dir);
                        if (!visited.contains(neighbor)) {
                            BlockState neighborState = level.getBlockState(neighbor);
                            if (neighborState.is(ModBlocks.SCRIBED_CHALK.get()) && 
                                neighborState.getValue(ScribedChalkBlock.CIRCUIT) == circuit) {
                                visited.add(neighbor);
                                distances.put(neighbor, currentDist + 1);
                                queue.add(neighbor);
                            }
                        }
                    }
                }
            }
        }
        return -1;
    }

    private int detectCircleTier(Level level, BlockPos center, boolean circuit) {
        if (checkPattern(level, center, circuit, ScribedChalkBlock.getOffsetsForTier(4)) && ScribedChalkBlock.validateNodeCountsStatic(level, center, 4)) return 4;
        if (checkPattern(level, center, circuit, ScribedChalkBlock.getOffsetsForTier(3)) && ScribedChalkBlock.validateNodeCountsStatic(level, center, 3)) return 3;
        if (checkPattern(level, center, circuit, ScribedChalkBlock.getOffsetsForTier(2)) && ScribedChalkBlock.validateNodeCountsStatic(level, center, 2)) return 2;
        if (checkPattern(level, center, circuit, ScribedChalkBlock.getOffsetsForTier(1)) && ScribedChalkBlock.validateNodeCountsStatic(level, center, 1)) return 1;
        return 0;
    }

    private boolean checkPattern(Level level, BlockPos center, boolean circuit, int[][] offsets) {
        for (int[] offset : offsets) {
            BlockPos pos = center.offset(offset[0], 0, offset[1]);
            BlockState state = level.getBlockState(pos);
            if (!state.is(ModBlocks.SCRIBED_CHALK.get()) || state.getValue(ScribedChalkBlock.CIRCUIT) != circuit) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void submit(ChalkRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        Minecraft mc = Minecraft.getInstance();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(WHITE_TEXTURE));

        int color = renderState.color;
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8)  & 0xFF) / 255.0f;
        float b = (color & 0xFF)          / 255.0f;
        float alpha = 0.85f;
        int light = 15728880;

        poseStack.pushPose();
        float yOffset = 0.015f;

        // 1. Draw connection lines to neighbors
        if (!renderState.isInMagicCircle) {
            drawConnections(poseStack, consumer, renderState, yOffset, r, g, b, alpha, light);
            if (renderState.isCircuit && renderState.targetNodeOffset != null) {
                if (ddraig.net.entropica.client.LensOverlayRenderer.isPropagationVisionActive(mc.player)) {
                    float ar = r;
                    float ag = g;
                    float ab = b;
                    if (!renderState.hasActiveDye) {
                        ar = 0.2f;
                        ag = 0.9f;
                        ab = 1.0f;
                    }
                    drawNeonArrow(poseStack, consumer, renderState.targetNodeOffset, ar, ag, ab, renderState.time, light);
                }
            }
        }

        // 2. Draw node decoration overlays based on node type
        boolean drawNodeCircle = false;
        String textSymbol = "";

        switch (renderState.nodeType) {
            case INPUT -> {
                drawNodeCircle = true;
                textSymbol = "I";
            }
            case SOURCE -> {
                drawNodeCircle = true;
                textSymbol = "S";
            }
            case COLLECTION -> {
                drawNodeCircle = true;
                textSymbol = "C";
            }
            case AMPLIFIER -> {
                drawNodeCircle = true;
                textSymbol = "A";
            }
            case CAPACITOR -> {
                drawNodeCircle = true;
                textSymbol = "P";
            }
            case RESONATOR -> {
                drawNodeCircle = true;
                textSymbol = "R";
            }
            case DIODE -> {
                drawNodeCircle = true;
                textSymbol = "D";
            }
            case DELAY -> {
                drawNodeCircle = true;
                textSymbol = String.valueOf(renderState.delayTicks);
            }
            case AND_GATE -> {
                drawNodeCircle = true;
                textSymbol = "&";
            }
            case OR_GATE -> {
                drawNodeCircle = true;
                textSymbol = "|";
            }
            case NOT_GATE -> {
                drawNodeCircle = true;
                textSymbol = "!";
            }
            case OUTPUT -> {
                drawNodeCircle = true;
                textSymbol = "O";
            }
            case RUNE -> {
                drawNodeCircle = true;
                if (renderState.hasStoredRune) {
                    textSymbol = renderState.runeUnicode;
                } else {
                    textSymbol = "E"; // E for Editable Node, matching legend and references
                }
            }
            case ESSENCE_BANK -> {
                drawNodeCircle = true;
                textSymbol = "B";
            }
            default -> {
                // For default paths, we just draw the central connection hub, no circles or text symbols
            }
        }

        if (drawNodeCircle && !renderState.isInMagicCircle) {
            // Draw a small empty circle at the node position
            drawLocalRing(poseStack, consumer, 0.22f, yOffset + 0.001f, 0.03f, 24, r, g, b, alpha, light);
            
            // Draw circuit-board schematic symbols under the text!
            if (renderState.nodeType == ScribedChalkBlock.NodeType.AMPLIFIER) {
                drawLocalTriangle(poseStack, consumer, 0.18f, yOffset + 0.002f, r, g, b, alpha, light);
            } else if (renderState.nodeType == ScribedChalkBlock.NodeType.CAPACITOR) {
                drawLocalCapacitorPlates(poseStack, consumer, 0.18f, yOffset + 0.002f, r, g, b, alpha, light);
            } else if (renderState.nodeType == ScribedChalkBlock.NodeType.RESONATOR) {
                drawLocalResonator(poseStack, consumer, 0.22f, yOffset + 0.002f, r, g, b, alpha, light);
            } else if (renderState.nodeType == ScribedChalkBlock.NodeType.DIODE) {
                drawLocalDiode(poseStack, consumer, 0.18f, yOffset + 0.002f, r, g, b, alpha, light, renderState.facing);
            } else if (renderState.nodeType == ScribedChalkBlock.NodeType.ESSENCE_BANK) {
                drawLocalEssenceBank(poseStack, consumer, 0.22f, yOffset + 0.002f, r, g, b, alpha, light);
            }
        }

        // 3. Render Magic Circle centered on OUTPUT node if complete
        if (renderState.circleTier > 0) {
            float radius = (float) renderState.circleTier;
            int points = renderState.rotatingNodes.size();
            drawMagicCircle(poseStack, consumer, radius, yOffset + 0.0005f, r, g, b, alpha, light, renderState.time, renderState.circleDyeTicks, renderState.circleHasActiveDye, renderState.circleDyeColor, renderState.circleDyeSourceAngle, points);
            
            // Draw rotating nodes inside the spinning vertex circles!
            float speed = switch (renderState.circleTier) {
                case 1 -> 0.8f;
                case 2 -> 0.6f;
                case 3 -> 0.5f;
                default -> 0.4f;
            };
            float offsetDegrees = -90.0f + renderState.time * speed;
            float offsetRad = (float) Math.toRadians(offsetDegrees);
            float polyRadius = (renderState.circleTier == 4) ? radius * 0.75f : radius * 0.7f;
            int pointsCount = renderState.rotatingNodes.size();
            
            if (pointsCount > 0) {
                float angleStep = (float)(2.0 * Math.PI / pointsCount);
                boolean fancyAnimation = ddraig.net.entropica.config.EntropicaConfig.FANCY_MAGIC_CIRCLE_PROCESSING.get();
                boolean isProcessing = renderState.isProcessing;

                float animProgress = 0.0f;
                if (isProcessing && renderState.processingTimeTotal > 0) {
                    animProgress = renderState.processingProgress / (float) renderState.processingTimeTotal;
                }

                for (int i = 0; i < pointsCount; i++) {
                    ChalkRenderState.RotatingNodeState rNode = renderState.rotatingNodes.get(i);
                    if (rNode.nodeType == ScribedChalkBlock.NodeType.DEFAULT) continue;

                    float theta = offsetRad + i * angleStep;
                    float cx = polyRadius * (float) Math.cos(theta) + 0.5f;
                    float cz = polyRadius * (float) Math.sin(theta) + 0.5f;

                    float nr = ((rNode.color >> 16) & 0xFF) / 255.0f;
                    float ng = ((rNode.color >> 8) & 0xFF) / 255.0f;
                    float nb = (rNode.color & 0xFF) / 255.0f;

                    // Determine position and scaling based on fancy processing animation
                    float animX = cx;
                    float animZ = cz;
                    float animY = 1.2f;
                    float sphereScaleX = 0.18f;
                    float sphereScaleY = 0.18f;
                    float sphereScaleZ = 0.18f;
                    float sphereAlpha = 0.7f + 0.15f * (float) Math.sin(renderState.time * 0.1f);
                    
                    boolean drawSphere = true;
                    boolean drawMiniCircle = false;
                    float miniCircleRadius = 0.25f;
                    float miniCircleAlpha = 1.0f;

                    if (fancyAnimation && isProcessing) {
                        if (animProgress <= 0.3f) {
                            // Phase 1: Grow & Raise from y = 0.35 to y = 1.2
                            float t = animProgress / 0.3f;
                            animY = 0.35f + (1.2f - 0.35f) * t;
                            sphereScaleX = 0.18f;
                            sphereScaleY = 0.18f * t; // vertical scale grows from 0 to full sphere!
                            sphereScaleZ = 0.18f;
                        } else if (animProgress <= 0.6f) {
                            // Phase 2: Move to Center & Stack from y = 0.5 to y = 3.0
                            float t = (animProgress - 0.3f) / 0.3f;
                            animX = cx + (0.5f - cx) * t;
                            animZ = cz + (0.5f - cz) * t;
                            float targetY = 0.5f + (pointsCount > 1 ? 2.5f * (i / (float)(pointsCount - 1)) : 1.25f);
                            animY = 1.2f + (targetY - 1.2f) * t;
                        } else if (animProgress <= 0.8f) {
                            // Phase 3: Fading / Shrinking of spheres & showing miniature magic circles
                            float t = (animProgress - 0.6f) / 0.2f;
                            animX = 0.5f;
                            animZ = 0.5f;
                            float targetY = 0.5f + (pointsCount > 1 ? 2.5f * (i / (float)(pointsCount - 1)) : 1.25f);
                            animY = targetY;
                            // Sphere remains but smaller (30% scale)
                            sphereScaleX = 0.18f * (1.0f - 0.7f * t);
                            sphereScaleY = 0.18f * (1.0f - 0.7f * t);
                            sphereScaleZ = 0.18f * (1.0f - 0.7f * t);
                            drawMiniCircle = true;
                            miniCircleAlpha = 1.0f;
                        } else {
                            // Phase 4: Cascading Smash
                            float t = (animProgress - 0.8f) / 0.2f;
                            animX = 0.5f;
                            animZ = 0.5f;
                            float targetY = 0.5f + (pointsCount > 1 ? 2.5f * (i / (float)(pointsCount - 1)) : 1.25f);
                            float Y_front = 3.0f - 3.0f * t;
                            animY = Math.max(yOffset, Math.min(targetY, Y_front));
                            sphereScaleX = 0.18f * 0.3f;
                            sphereScaleY = 0.18f * 0.3f;
                            sphereScaleZ = 0.18f * 0.3f;
                            drawMiniCircle = true;
                            miniCircleAlpha = 1.0f;
                        }
                    } else {
                        // Not processing or fancyAnimation disabled
                        if (fancyAnimation) {
                            drawSphere = false;
                            drawMiniCircle = false;
                        } else {
                            drawSphere = false;
                            drawMiniCircle = true;
                            animX = cx;
                            animZ = cz;
                            animY = 1.2f;
                        }
                    }

                    // Render the Sphere
                    if (drawSphere) {
                        poseStack.pushPose();
                        poseStack.translate(animX, animY, animZ);
                        poseStack.scale(sphereScaleX / 0.18f, sphereScaleY / 0.18f, sphereScaleZ / 0.18f);
                        drawSphere(consumer, poseStack.last().pose(), 0.18f, nr, ng, nb, sphereAlpha, light);
                        poseStack.popPose();
                    }

                    // Render the Miniature flat horizontal Magic Circle
                    if (drawMiniCircle) {
                        int subPoints = Math.max(3, (int) Math.ceil(pointsCount * 2.0 / 3.0));
                        poseStack.pushPose();
                        poseStack.translate(animX - 0.5f, 0.0f, animZ - 0.5f);
                        drawMagicCircle(poseStack, consumer, miniCircleRadius, animY, nr, ng, nb, miniCircleAlpha * 0.85f, light, renderState.time, renderState.circleDyeTicks, renderState.circleHasActiveDye, renderState.circleDyeColor, renderState.circleDyeSourceAngle, subPoints);
                        poseStack.popPose();
                    }
                }
            }
        }

        if (renderState.nodeType != ScribedChalkBlock.NodeType.DEFAULT && !renderState.isInMagicCircle) {
            drawFloatingNodeGeometry(poseStack, consumer, 0.5f, 1.2f, 0.5f, r, g, b, renderState.time, light, renderState.isProcessing, renderState.isCircuit, renderState.targetNodeOffset, renderState.timePulseOffset);
        }

        // --- 4. Render All Text/Font Overlays ---
        Font font = mc.font;

        // A. Node Orbiting Symbols (for standard circuit nodes)
        if (drawNodeCircle && !renderState.isInMagicCircle) {
            int nodeColor = (Math.clamp((int)(r*255), 0, 255) << 16) | (Math.clamp((int)(g*255), 0, 255) << 8) | Math.clamp((int)(b*255), 0, 255);
            drawOrbitingSymbols(poseStack, font, bufferSource, 0.5f, 0.5f, yOffset + 0.001f, 0.22f, nodeColor, renderState.time, light);
        }

        // B. Node central character symbols (for standard circuit nodes)
        if (drawNodeCircle && !textSymbol.isEmpty() && !renderState.isInMagicCircle) {
            poseStack.pushPose();
            poseStack.translate(0.5f, yOffset + 0.005f, 0.5f);
            poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90.0f));
            float textScale = (renderState.nodeType == ScribedChalkBlock.NodeType.RUNE && renderState.hasStoredRune) ? 0.018f : 0.015f;
            poseStack.scale(textScale, -textScale, textScale);
            int width = font.width(textSymbol);
            font.drawInBatch(textSymbol, -width / 2.0f, -font.lineHeight / 2.0f, color | 0xFF000000, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, light);
            poseStack.popPose();
        }

        // B2. Node floating symbol text (for standard circuit nodes)
        if (renderState.nodeType != ScribedChalkBlock.NodeType.DEFAULT && !renderState.isInMagicCircle) {
            drawFloatingNodeText(poseStack, bufferSource, 0.5f, 1.2f, 0.5f, r, g, b, renderState.time, light, renderState.nodeType, renderState.isProcessing, renderState.isCircuit, renderState.targetNodeOffset);
        }

        // C. Magic Circle boundary symbols & Vertex orbiting symbols & Text symbols
        if (renderState.circleTier > 0) {
            float radius = (float) renderState.circleTier;
            int pointsCount = renderState.rotatingNodes.size();
            
            // C1. Outer circle alchemical symbols
            drawMagicCircleSymbols(poseStack, font, bufferSource, radius, yOffset + 0.0005f, r, g, b, renderState.time, renderState.circleHasActiveDye, renderState.circleDyeColor);
            
            // C2. Vertex nodes orbiting symbols and text
            if (pointsCount > 0) {
                float speed = switch (renderState.circleTier) {
                    case 1 -> 0.8f;
                    case 2 -> 0.6f;
                    case 3 -> 0.5f;
                    default -> 0.4f;
                };
                float offsetDegrees = -90.0f + renderState.time * speed;
                float offsetRad = (float) Math.toRadians(offsetDegrees);
                float polyRadius = (renderState.circleTier == 4) ? radius * 0.75f : radius * 0.7f;
                float angleStep = (float)(2.0 * Math.PI / pointsCount);

                for (int i = 0; i < pointsCount; i++) {
                    ChalkRenderState.RotatingNodeState rNode = renderState.rotatingNodes.get(i);
                    float theta = offsetRad + i * angleStep;
                    float cx = polyRadius * (float) Math.cos(theta) + 0.5f;
                    float cz = polyRadius * (float) Math.sin(theta) + 0.5f;
                    
                    // Vertex orbiting symbols
                    float vertexCircleRadius = (radius < 1.5f) ? 0.15f : (radius < 2.5f) ? 0.2f : (radius < 3.5f) ? 0.25f : 0.3f;
                    drawOrbitingSymbols(poseStack, font, bufferSource, cx, cz, yOffset + 0.001f, vertexCircleRadius, rNode.color, renderState.time, light);
                    
                    // Vertex text symbol or custom 2D schematic layout
                    boolean hasSchematic = rNode.nodeType == ScribedChalkBlock.NodeType.AMPLIFIER ||
                                           rNode.nodeType == ScribedChalkBlock.NodeType.CAPACITOR ||
                                           rNode.nodeType == ScribedChalkBlock.NodeType.RESONATOR ||
                                           rNode.nodeType == ScribedChalkBlock.NodeType.DIODE ||
                                           rNode.nodeType == ScribedChalkBlock.NodeType.ESSENCE_BANK;

                    if (hasSchematic) {
                        float nr = ((rNode.color >> 16) & 0xFF) / 255.0f;
                        float ng = ((rNode.color >> 8) & 0xFF) / 255.0f;
                        float nb = (rNode.color & 0xFF) / 255.0f;
                        
                        if (rNode.nodeType == ScribedChalkBlock.NodeType.AMPLIFIER) {
                            poseStack.pushPose();
                            poseStack.translate(cx - 0.5f, 0.0f, cz - 0.5f);
                            drawLocalTriangle(poseStack, consumer, 0.18f, yOffset + 0.002f, nr, ng, nb, 0.85f, light);
                            poseStack.popPose();
                        } else if (rNode.nodeType == ScribedChalkBlock.NodeType.CAPACITOR) {
                            poseStack.pushPose();
                            poseStack.translate(cx - 0.5f, 0.0f, cz - 0.5f);
                            drawLocalCapacitorPlates(poseStack, consumer, 0.18f, yOffset + 0.002f, nr, ng, nb, 0.85f, light);
                            poseStack.popPose();
                        } else if (rNode.nodeType == ScribedChalkBlock.NodeType.RESONATOR) {
                            poseStack.pushPose();
                            poseStack.translate(cx - 0.5f, 0.0f, cz - 0.5f);
                            drawLocalResonator(poseStack, consumer, 0.22f, yOffset + 0.002f, nr, ng, nb, 0.85f, light);
                            poseStack.popPose();
                        } else if (rNode.nodeType == ScribedChalkBlock.NodeType.DIODE) {
                            poseStack.pushPose();
                            poseStack.translate(cx - 0.5f, 0.0f, cz - 0.5f);
                            drawLocalDiode(poseStack, consumer, 0.18f, yOffset + 0.002f, nr, ng, nb, 0.85f, light, renderState.facing);
                            poseStack.popPose();
                        } else if (rNode.nodeType == ScribedChalkBlock.NodeType.ESSENCE_BANK) {
                            poseStack.pushPose();
                            poseStack.translate(cx - 0.5f, 0.0f, cz - 0.5f);
                            drawLocalEssenceBank(poseStack, consumer, 0.22f, yOffset + 0.002f, nr, ng, nb, 0.85f, light);
                            poseStack.popPose();
                        }
                    } else if (!rNode.textSymbol.isEmpty()) {
                        poseStack.pushPose();
                        poseStack.translate(cx, yOffset + 0.005f, cz);
                        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90.0f));
                        float textScale = (rNode.nodeType == ScribedChalkBlock.NodeType.RUNE && rNode.textSymbol.length() > 1) ? 0.018f : 0.015f;
                        poseStack.scale(textScale, -textScale, textScale);
                        int w = font.width(rNode.textSymbol);
                        font.drawInBatch(rNode.textSymbol, -w / 2.0f, -font.lineHeight / 2.0f, 0xFF000000, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, light);
                        poseStack.popPose();
                    }

                    // Vertex floating symbol text
                    if (rNode.nodeType != ScribedChalkBlock.NodeType.DEFAULT) {
                        float nr = ((rNode.color >> 16) & 0xFF) / 255.0f;
                        float ng = ((rNode.color >> 8) & 0xFF) / 255.0f;
                        float nb = (rNode.color & 0xFF) / 255.0f;
                        drawFloatingNodeText(poseStack, bufferSource, cx, 1.2f, cz, nr, ng, nb, renderState.time, light, rNode.nodeType, rNode.isProcessing, false, null);
                    }
                }
            }
        }
        bufferSource.endBatch();

        poseStack.popPose();

        // 5. Draw Floating Item models (Orbis cells, runes, items)
        if (renderState.circleTier > 0) {
            float radius = (float) renderState.circleTier;
            int pointsCount = renderState.rotatingNodes.size();
            if (pointsCount > 0) {
                float speed = switch (renderState.circleTier) {
                    case 1 -> 0.8f;
                    case 2 -> 0.6f;
                    case 3 -> 0.5f;
                    default -> 0.4f;
                };
                float offsetDegrees = -90.0f + renderState.time * speed;
                float offsetRad = (float) Math.toRadians(offsetDegrees);
                float polyRadius = (renderState.circleTier == 4) ? radius * 0.75f : radius * 0.7f;
                float angleStep = (float)(2.0 * Math.PI / pointsCount);

                boolean fancyAnimation = ddraig.net.entropica.config.EntropicaConfig.FANCY_MAGIC_CIRCLE_PROCESSING.get();
                boolean isProcessing = renderState.isProcessing;
                float animProgress = 0.0f;
                if (isProcessing && renderState.processingTimeTotal > 0) {
                    animProgress = renderState.processingProgress / (float) renderState.processingTimeTotal;
                }

                float itemScaleFactor = 1.0f;
                if (fancyAnimation && isProcessing) {
                    if (animProgress <= 0.3f) {
                        itemScaleFactor = 1.0f - (animProgress / 0.3f);
                    } else {
                        itemScaleFactor = 0.0f;
                    }
                }

                if (itemScaleFactor > 0.001f) {
                    for (int i = 0; i < pointsCount; i++) {
                        ChalkRenderState.RotatingNodeState rNode = renderState.rotatingNodes.get(i);
                        float theta = offsetRad + i * angleStep;
                        float cx = polyRadius * (float) Math.cos(theta) + 0.5f;
                        float cz = polyRadius * (float) Math.sin(theta) + 0.5f;
                        
                        if (rNode.hasStoredOrbisCell) {
                            poseStack.pushPose();
                            poseStack.translate(cx, 0.35f, cz);
                            poseStack.scale(0.5f * itemScaleFactor, 0.5f * itemScaleFactor, 0.5f * itemScaleFactor);
                            float rotation = (renderState.time * 1.5f) % 360.0f;
                            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotation));
                            rNode.storedOrbisCellState.submit(poseStack, submitNodeCollector, light, OverlayTexture.NO_OVERLAY, 0);
                            poseStack.popPose();
                        }
                        if (rNode.hasStoredRune) {
                            poseStack.pushPose();
                            poseStack.translate(cx, 0.3f, cz);
                            poseStack.scale(0.4f * itemScaleFactor, 0.4f * itemScaleFactor, 0.4f * itemScaleFactor);
                            float rotation = (renderState.time * 1.0f) % 360.0f;
                            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotation));
                            rNode.storedRuneState.submit(poseStack, submitNodeCollector, light, OverlayTexture.NO_OVERLAY, 0);
                            poseStack.popPose();
                        }
                        if (rNode.hasStoredItem) {
                            poseStack.pushPose();
                            poseStack.translate(cx, 0.35f, cz);
                            poseStack.scale(0.5f * itemScaleFactor, 0.5f * itemScaleFactor, 0.5f * itemScaleFactor);
                            float rotation = (renderState.time * 1.5f) % 360.0f;
                            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotation));
                            rNode.storedItemState.submit(poseStack, submitNodeCollector, light, OverlayTexture.NO_OVERLAY, 0);
                            poseStack.popPose();
                        }
                    }
                }
            }
        }

        // Render floating Orbis Cell
        if (renderState.hasStoredOrbisCell && !renderState.isInMagicCircle) {
            poseStack.pushPose();
            poseStack.translate(0.5f, 0.35f, 0.5f);
            poseStack.scale(0.5f, 0.5f, 0.5f);
            float rotation = (renderState.time * 1.5f) % 360.0f;
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotation));
            renderState.storedOrbisCellState.submit(poseStack, submitNodeCollector, light, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }

        // Render floating Rune item
        if (renderState.hasStoredRune && !renderState.isInMagicCircle) {
            poseStack.pushPose();
            poseStack.translate(0.5f, 0.3f, 0.5f);
            poseStack.scale(0.4f, 0.4f, 0.4f);
            float rotation = (renderState.time * 1.0f) % 360.0f;
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotation));
            renderState.storedRuneState.submit(poseStack, submitNodeCollector, light, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }

        // Render floating input item
        if (renderState.hasStoredItem && !renderState.isInMagicCircle) {
            poseStack.pushPose();
            poseStack.translate(0.5f, 0.35f, 0.5f);
            poseStack.scale(0.5f, 0.5f, 0.5f);
            float rotation = (renderState.time * 1.5f) % 360.0f;
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotation));
            renderState.storedItemState.submit(poseStack, submitNodeCollector, light, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }

    private void drawConnections(PoseStack poseStack, VertexConsumer consumer, ChalkRenderState state, float y, float r, float g, float b, float a, int light) {
        if (!state.isCircuit) {
            float min = 0.46f;
            float max = 0.54f;
            drawQuad(poseStack, consumer, min, y, min, max, y, max, r, g, b, a, light);
            if (state.connectNorth) {
                drawQuad(poseStack, consumer, min, y, 0.0f, max, y, min, r, g, b, a, light);
            }
            if (state.connectSouth) {
                drawQuad(poseStack, consumer, min, y, max, max, y, 1.0f, r, g, b, a, light);
            }
            if (state.connectEast) {
                drawQuad(poseStack, consumer, max, y, min, 1.0f, y, max, r, g, b, a, light);
            }
            if (state.connectWest) {
                drawQuad(poseStack, consumer, 0.0f, y, min, min, y, max, r, g, b, a, light);
            }
            return;
        }

        // Check if it is a 90-degree corner
        int connCount = 0;
        if (state.connectNorth) connCount++;
        if (state.connectSouth) connCount++;
        if (state.connectEast) connCount++;
        if (state.connectWest) connCount++;
        
        boolean isCorner = (connCount == 2) && !(state.connectNorth && state.connectSouth) && !(state.connectEast && state.connectWest)
            && !state.connectNorthEast && !state.connectNorthWest && !state.connectSouthEast && !state.connectSouthWest;

        float sR = (((state.defaultColor >> 16) & 0xFF) / 255f) * 0.7f;
        float sG = (((state.defaultColor >> 8) & 0xFF) / 255f) * 0.7f;
        float sB = ((state.defaultColor & 0xFF) / 255f) * 0.7f;

        if (state.isCircuit && state.distance >= 0) {
            float wavePhase = (state.time * 0.2f) % 16.0f; // 0.2f speed = ~4 ticks per block, loops every 16 blocks
            float diff = Math.abs(state.distance - wavePhase);
            if (diff > 8.0f) diff = 16.0f - diff;
            if (diff < 2.0f) {
                float highlight = 1.0f - (diff / 2.0f);
                float waveBrightness = highlight * 0.4f; // up to 40% brighter/lighter wave
                r = Math.min(1.0f, r + waveBrightness);
                g = Math.min(1.0f, g + waveBrightness);
                b = Math.min(1.0f, b + waveBrightness);
            }
        }

        if (isCorner) {
            if (state.connectNorth && state.connectEast) {
                drawCurvedCorner(poseStack, consumer, y, Direction.NORTH, Direction.EAST, r, g, b, sR, sG, sB, a, light);
            } 
            else if (state.connectNorth && state.connectWest) {
                drawCurvedCorner(poseStack, consumer, y, Direction.NORTH, Direction.WEST, r, g, b, sR, sG, sB, a, light);
            } 
            else if (state.connectSouth && state.connectEast) {
                drawCurvedCorner(poseStack, consumer, y, Direction.SOUTH, Direction.EAST, r, g, b, sR, sG, sB, a, light);
            } 
            else if (state.connectSouth && state.connectWest) {
                drawCurvedCorner(poseStack, consumer, y, Direction.SOUTH, Direction.WEST, r, g, b, sR, sG, sB, a, light);
            }
            return;
        }

        float min = 0.47f;
        float max = 0.53f;

        float s1Min = 0.38f;
        float s1Max = 0.41f;
        float s2Min = 0.59f;
        float s2Max = 0.62f;

        // Draw central hub center pad and corner pads (using diamonds for soft corners)
        drawDiamond(poseStack, consumer, min, y, min, max, y, max, r, g, b, a, light);
        drawDiamond(poseStack, consumer, s1Min, y, s1Min, s1Max, y, s1Max, sR, sG, sB, a, light);
        drawDiamond(poseStack, consumer, s2Min, y, s1Min, s2Max, y, s1Max, sR, sG, sB, a, light);
        drawDiamond(poseStack, consumer, s1Min, y, s2Min, s1Max, y, s2Max, sR, sG, sB, a, light);
        drawDiamond(poseStack, consumer, s2Min, y, s2Min, s2Max, y, s2Max, sR, sG, sB, a, light);

        // Draw segments to neighbors
        if (state.connectNorth) {
            drawTraceSegment(poseStack, consumer, y, 0.5f, 0.0f, true, 0.500f, 0.06f, r, g, b, a, light);
            drawTraceSegment(poseStack, consumer, y, 0.5f, 0.0f, true, 0.395f, 0.03f, sR, sG, sB, a, light);
            drawTraceSegment(poseStack, consumer, y, 0.5f, 0.0f, true, 0.605f, 0.03f, sR, sG, sB, a, light);
        }
        if (state.connectSouth) {
            drawTraceSegment(poseStack, consumer, y, 0.5f, 1.0f, true, 0.500f, 0.06f, r, g, b, a, light);
            drawTraceSegment(poseStack, consumer, y, 0.5f, 1.0f, true, 0.395f, 0.03f, sR, sG, sB, a, light);
            drawTraceSegment(poseStack, consumer, y, 0.5f, 1.0f, true, 0.605f, 0.03f, sR, sG, sB, a, light);
        }
        if (state.connectEast) {
            drawTraceSegment(poseStack, consumer, y, 0.5f, 1.0f, false, 0.500f, 0.06f, r, g, b, a, light);
            drawTraceSegment(poseStack, consumer, y, 0.5f, 1.0f, false, 0.395f, 0.03f, sR, sG, sB, a, light);
            drawTraceSegment(poseStack, consumer, y, 0.5f, 1.0f, false, 0.605f, 0.03f, sR, sG, sB, a, light);
        }
        if (state.connectWest) {
            drawTraceSegment(poseStack, consumer, y, 0.5f, 0.0f, false, 0.500f, 0.06f, r, g, b, a, light);
            drawTraceSegment(poseStack, consumer, y, 0.5f, 0.0f, false, 0.395f, 0.03f, sR, sG, sB, a, light);
            drawTraceSegment(poseStack, consumer, y, 0.5f, 0.0f, false, 0.605f, 0.03f, sR, sG, sB, a, light);
        }

        // Draw diagonal segments
        if (state.connectNorthEast) {
            float px = 1.0f, pz = 1.0f;
            drawDiagonalSegment(poseStack, consumer, y, 0.5f, 0.5f, 1.0f, 0.0f, px, pz, 0.06f, r, g, b, a, light);
            drawDiagonalSegment(poseStack, consumer, y, 0.5f - 0.0778f * px, 0.5f - 0.0778f * pz, 1.0f - 0.0778f * px, 0.0f - 0.0778f * pz, px, pz, 0.03f, sR, sG, sB, a, light);
            drawDiagonalSegment(poseStack, consumer, y, 0.5f + 0.0778f * px, 0.5f + 0.0778f * pz, 1.0f + 0.0778f * px, 0.0f + 0.0778f * pz, px, pz, 0.03f, sR, sG, sB, a, light);
        }
        if (state.connectNorthWest) {
            float px = -1.0f, pz = 1.0f;
            drawDiagonalSegment(poseStack, consumer, y, 0.5f, 0.5f, 0.0f, 0.0f, px, pz, 0.06f, r, g, b, a, light);
            drawDiagonalSegment(poseStack, consumer, y, 0.5f - 0.0778f * px, 0.5f - 0.0778f * pz, 0.0f - 0.0778f * px, 0.0f - 0.0778f * pz, px, pz, 0.03f, sR, sG, sB, a, light);
            drawDiagonalSegment(poseStack, consumer, y, 0.5f + 0.0778f * px, 0.5f + 0.0778f * pz, 0.0f + 0.0778f * px, 0.0f + 0.0778f * pz, px, pz, 0.03f, sR, sG, sB, a, light);
        }
        if (state.connectSouthEast) {
            float px = 1.0f, pz = -1.0f;
            drawDiagonalSegment(poseStack, consumer, y, 0.5f, 0.5f, 1.0f, 1.0f, px, pz, 0.06f, r, g, b, a, light);
            drawDiagonalSegment(poseStack, consumer, y, 0.5f - 0.0778f * px, 0.5f - 0.0778f * pz, 1.0f - 0.0778f * px, 1.0f - 0.0778f * pz, px, pz, 0.03f, sR, sG, sB, a, light);
            drawDiagonalSegment(poseStack, consumer, y, 0.5f + 0.0778f * px, 0.5f + 0.0778f * pz, 1.0f + 0.0778f * px, 1.0f + 0.0778f * pz, px, pz, 0.03f, sR, sG, sB, a, light);
        }
        if (state.connectSouthWest) {
            float px = -1.0f, pz = -1.0f;
            drawDiagonalSegment(poseStack, consumer, y, 0.5f, 0.5f, 0.0f, 1.0f, px, pz, 0.06f, r, g, b, a, light);
            drawDiagonalSegment(poseStack, consumer, y, 0.5f - 0.0778f * px, 0.5f - 0.0778f * pz, 0.0f - 0.0778f * px, 1.0f - 0.0778f * pz, px, pz, 0.03f, sR, sG, sB, a, light);
            drawDiagonalSegment(poseStack, consumer, y, 0.5f + 0.0778f * px, 0.5f + 0.0778f * pz, 0.0f + 0.0778f * px, 1.0f + 0.0778f * pz, px, pz, 0.03f, sR, sG, sB, a, light);
        }
    }

    private void drawTraceSegment(PoseStack poseStack, VertexConsumer consumer, float y,
                                  float startVal, float endVal, boolean isVertical, 
                                  float offsetAxisVal, float width, 
                                  float r, float g, float b, float a, int light) {
        float minX, maxX, minZ, maxZ;
        if (isVertical) {
            minX = offsetAxisVal - width / 2.0f;
            maxX = offsetAxisVal + width / 2.0f;
            minZ = Math.min(startVal, endVal);
            maxZ = Math.max(startVal, endVal);
        } else {
            minX = Math.min(startVal, endVal);
            maxX = Math.max(startVal, endVal);
            minZ = offsetAxisVal - width / 2.0f;
            maxZ = offsetAxisVal + width / 2.0f;
        }
        drawQuad(poseStack, consumer, minX, y, minZ, maxX, y, maxZ, r, g, b, a, light);
    }

    private void drawDiagonalSegment(PoseStack poseStack, VertexConsumer consumer, float y,
                                     float xStart, float zStart, float xEnd, float zEnd,
                                     float px, float pz, float width,
                                     float r, float g, float b, float a, int light) {
        float halfW = (width / 2.0f) / 1.414f;
        float x1 = xStart - halfW * px;
        float z1 = zStart - halfW * pz;
        float x2 = xStart + halfW * px;
        float z2 = zStart + halfW * pz;
        float x3 = xEnd + halfW * px;
        float z3 = zEnd + halfW * pz;
        float x4 = xEnd - halfW * px;
        float z4 = zEnd - halfW * pz;
        drawDiagonalQuad(poseStack, consumer, x1, x2, z1, z2, x3, x4, z3, z4, y, r, g, b, a, light);
    }

    private void drawBezierCurve(PoseStack poseStack, VertexConsumer consumer, float y,
                                 float x1, float z1, float cx, float cz, float x2, float z2,
                                 float width, int steps, float r, float g, float b, float a, int light) {
        float prevX = x1;
        float prevZ = z1;
        
        for (int i = 1; i <= steps; i++) {
            float t = (float) i / steps;
            float omt = 1.0f - t;
            
            float currX = omt * omt * x1 + 2.0f * omt * t * cx + t * t * x2;
            float currZ = omt * omt * z1 + 2.0f * omt * t * cz + t * t * z2;
            
            float dx = currX - prevX;
            float dz = currZ - prevZ;
            float len = (float) Math.sqrt(dx * dx + dz * dz);
            float px = 0.0f, pz = 0.0f;
            if (len > 0.0f) {
                px = -dz / len;
                pz = dx / len;
            }
            
            float halfW = width / 2.0f;
            drawDiagonalQuad(poseStack, consumer,
                             prevX - halfW * px, prevX + halfW * px, prevZ - halfW * pz, prevZ + halfW * pz,
                             currX + halfW * px, currX - halfW * px, currZ + halfW * pz, currZ - halfW * pz,
                             y, r, g, b, a, light);
            
            prevX = currX;
            prevZ = currZ;
        }
    }

    private void drawSphere(VertexConsumer consumer, Matrix4f matrix, float radius, float r, float g, float b, float a, int light) {
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

                consumer.addVertex(matrix, x * zr0, z0, y * zr0).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(x, z0/radius, y);
                consumer.addVertex(matrix, x1 * zr0, z0, y1 * zr0).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(x1, z0/radius, y1);
                consumer.addVertex(matrix, x1 * zr1, z1, y1 * zr1).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(x1, z1/radius, y1);
                consumer.addVertex(matrix, x * zr1, z1, y * zr1).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(x, z1/radius, y);
            }
        }
    }

    private void drawHalo(PoseStack poseStack, MultiBufferSource bufferSource, float radius, float yawDegrees, float pitchDegrees, float r, float g, float b, float time, int light) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        String runes = "ᚠᚢᚦᚨᚲᚷᚹᚺᚾᛁᛃᛇᛈᛉᛊᛏᛒᛖᛗᛚᛜᛞᛟ";
        int count = runes.length();
        float angleStep = 360.0f / count;

        poseStack.pushPose();
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(yawDegrees));
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(pitchDegrees));
        poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(time * 2.0f));

        int colorVal = ((int)(r * 255) << 16) | ((int)(g * 255) << 8) | (int)(b * 255);

        for (int i = 0; i < count; i++) {
            poseStack.pushPose();
            float angle = i * angleStep;
            poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(angle));
            poseStack.translate(0, radius, 0);
            
            poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90.0f));
            poseStack.scale(0.015f, -0.015f, 0.015f);
            
            String charStr = String.valueOf(runes.charAt(i));
            int w = font.width(charStr);
            font.drawInBatch(charStr, -w / 2.0f, -font.lineHeight / 2.0f, colorVal | 0xFF000000, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, light);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private void drawSymbolInsideSphere(PoseStack poseStack, MultiBufferSource bufferSource, String symbol, float r, float g, float b, int light) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        poseStack.pushPose();
        int colorVal = ((int)(r * 255) << 16) | ((int)(g * 255) << 8) | (int)(b * 255);
        
        for (int angle = 0; angle < 360; angle += 90) {
            poseStack.pushPose();
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(angle));
            poseStack.scale(0.018f, -0.018f, 0.018f);
            int w = font.width(symbol);
            font.drawInBatch(symbol, -w / 2.0f, -font.lineHeight / 2.0f, colorVal | 0xFF000000, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, light);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private void drawFloatingNodeGeometry(PoseStack poseStack, VertexConsumer consumer,
                                         float tx, float ty, float tz,
                                         float r, float g, float b, float time, int light,
                                         boolean isProcessing, boolean isCircuit,
                                         @Nullable Vec3 nextNodeOffset, float timePulseOffset) {
        poseStack.pushPose();
        poseStack.translate(tx, ty, tz);
        
        float alpha = 0.7f + 0.15f * (float) Math.sin(time * 0.1f);
        drawSphere(consumer, poseStack.last().pose(), 0.18f, r, g, b, alpha, light);
        poseStack.popPose();

        if (isCircuit && nextNodeOffset != null && isProcessing) {
            poseStack.pushPose();
            Vec3 start = new Vec3(tx, ty, tz);
            Vec3 end = start.add(nextNodeOffset);
            Vec3 wispPos = start.lerp(end, timePulseOffset);
            poseStack.translate(wispPos.x, wispPos.y, wispPos.z);
            
            float wispSize = 0.05f + 0.02f * (float) Math.sin(time * 0.3f);
            drawSphere(consumer, poseStack.last().pose(), wispSize, r * 1.2f, g * 1.2f, b * 1.2f, 0.9f, light);
            poseStack.popPose();
        }
    }

    private void drawFloatingNodeText(PoseStack poseStack, MultiBufferSource bufferSource,
                                     float tx, float ty, float tz,
                                     float r, float g, float b, float time, int light,
                                     ScribedChalkBlock.NodeType nodeType, boolean isProcessing,
                                     boolean isCircuit, @Nullable Vec3 nextNodeOffset) {
        poseStack.pushPose();
        poseStack.translate(tx, ty, tz);
        
        String symbol = switch (nodeType) {
            case AND_GATE -> "⚭";
            case OR_GATE -> "▽";
            case NOT_GATE -> "⦸";
            default -> "";
        };
        if (!symbol.isEmpty()) {
            drawSymbolInsideSphere(poseStack, bufferSource, symbol, r, g, b, light);
        }
        
        float haloRadius = 0.24f;
        float yaw = 0.0f;
        float pitch = isProcessing ? 90.0f : 0.0f;

        if (nextNodeOffset != null) {
            yaw = (float) Math.toDegrees(Math.atan2(nextNodeOffset.z, nextNodeOffset.x));
        } else if (!isCircuit) {
            float dx = 0.5f - tx;
            float dz = 0.5f - tz;
            yaw = (float) Math.toDegrees(Math.atan2(dz, dx));
        }
        
        drawHalo(poseStack, bufferSource, haloRadius, yaw, pitch, r, g, b, time, light);
        poseStack.popPose();
    }

    private void drawNeonArrow(PoseStack poseStack, VertexConsumer consumer, Vec3 direction, float r, float g, float b, float time, int light) {
        poseStack.pushPose();
        poseStack.translate(0.5f, 0.06f, 0.5f);
        
        float yaw = (float) Math.toDegrees(Math.atan2(direction.z, direction.x));
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-yaw));
        
        float pulse = (time * 0.15f) % 1.0f;
        poseStack.translate(-0.3f + pulse * 0.6f, 0.0f, 0.0f);
        
        int overlay = OverlayTexture.NO_OVERLAY;
        Matrix4f mat = poseStack.last().pose();
        float a = 0.8f + 0.2f * (float) Math.sin(time * 0.5f);
        
        consumer.addVertex(mat, -0.05f, 0.0f, 0.08f).setColor(r, g, b, a).setUv(0, 0).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(mat, 0.15f, 0.0f, 0.0f).setColor(r, g, b, a).setUv(1, 0).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(mat, -0.05f, 0.0f, -0.08f).setColor(r, g, b, a).setUv(1, 1).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        consumer.addVertex(mat, 0.02f, 0.0f, 0.0f).setColor(r, g, b, a).setUv(0, 1).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
        
        poseStack.popPose();
    }

    private void drawCurvedCorner(PoseStack poseStack, VertexConsumer consumer, float y,
                                  Direction d1, Direction d2,
                                  float r, float g, float b, float sR, float sG, float sB, float a, int light) {
        float cx = 0.5f, cz = 0.5f;
        float x1 = cx + 0.2f * d1.getStepX();
        float z1 = cz + 0.2f * d1.getStepZ();
        float x2 = cx + 0.2f * d2.getStepX();
        float z2 = cz + 0.2f * d2.getStepZ();
        
        drawBezierCurve(poseStack, consumer, y, x1, z1, cx, cz, x2, z2, 0.06f, 6, r, g, b, a, light);
        
        drawTraceSegment(poseStack, consumer, y, cx + 0.5f * d1.getStepX(), cx + 0.2f * d1.getStepX(), d1.getAxis() == Direction.Axis.Z, cx, 0.06f, r, g, b, a, light);
        drawTraceSegment(poseStack, consumer, y, cx + 0.5f * d2.getStepX(), cx + 0.2f * d2.getStepX(), d2.getAxis() == Direction.Axis.Z, cx, 0.06f, r, g, b, a, light);
        
        float offset1 = -0.105f;
        float offset2 = 0.105f;
        
        // Side Line 1 (inner):
        float s1_x1 = x1 + offset1 * d2.getStepX();
        float s1_z1 = z1 + offset1 * d2.getStepZ();
        float s1_cx = cx + offset1 * d2.getStepX() + offset1 * d1.getStepX();
        float s1_cz = cz + offset1 * d2.getStepZ() + offset1 * d1.getStepZ();
        float s1_x2 = x2 + offset1 * d1.getStepX();
        float s1_z2 = z2 + offset1 * d1.getStepZ();
        drawBezierCurve(poseStack, consumer, y, s1_x1, s1_z1, s1_cx, s1_cz, s1_x2, s1_z2, 0.03f, 6, sR, sG, sB, a, light);
        
        drawTraceSegment(poseStack, consumer, y, cx + 0.5f * d1.getStepX(), cx + 0.2f * d1.getStepX(), d1.getAxis() == Direction.Axis.Z, cx + offset1 * d2.getStepX(), 0.03f, sR, sG, sB, a, light);
        drawTraceSegment(poseStack, consumer, y, cx + 0.5f * d2.getStepX(), cx + 0.2f * d2.getStepX(), d2.getAxis() == Direction.Axis.Z, cx + offset1 * d1.getStepX(), 0.03f, sR, sG, sB, a, light);
        
        // Side Line 2 (outer):
        float s2_x1 = x1 + offset2 * d2.getStepX();
        float s2_z1 = z1 + offset2 * d2.getStepZ();
        float s2_cx = cx + offset2 * d2.getStepX() + offset2 * d1.getStepX();
        float s2_cz = cz + offset2 * d2.getStepZ() + offset2 * d1.getStepZ();
        float s2_x2 = x2 + offset2 * d1.getStepX();
        float s2_z2 = z2 + offset2 * d1.getStepZ();
        drawBezierCurve(poseStack, consumer, y, s2_x1, s2_z1, s2_cx, s2_cz, s2_x2, s2_z2, 0.03f, 6, sR, sG, sB, a, light);
        
        drawTraceSegment(poseStack, consumer, y, cx + 0.5f * d1.getStepX(), cx + 0.2f * d1.getStepX(), d1.getAxis() == Direction.Axis.Z, cx + offset2 * d2.getStepX(), 0.03f, sR, sG, sB, a, light);
        drawTraceSegment(poseStack, consumer, y, cx + 0.5f * d2.getStepX(), cx + 0.2f * d2.getStepX(), d2.getAxis() == Direction.Axis.Z, cx + offset2 * d1.getStepX(), 0.03f, sR, sG, sB, a, light);
    }

    private void drawMagicCircle(PoseStack poseStack, VertexConsumer consumer, float radius, float y, float r, float g, float b, float a, int light, float time, float dyeTicks, boolean hasActiveDye, int dyeColor, float dyeSourceAngle, int points) {
        int segments = 64;
        // Compute dye source color rgb
        float dr = ((dyeColor >> 16) & 0xFF) / 255.0f;
        float dg = ((dyeColor >> 8)  & 0xFF) / 255.0f;
        float db = (dyeColor & 0xFF)          / 255.0f;

        // Concentric circular layers (circles in circles) based on tier
        if (radius < 1.5f) {
            // Tier 1 (3x3): 3 concentric layers of circles
            drawRing(poseStack, consumer, 0.5f, 0.5f, radius, y, 0.03f, segments, r, g, b, dr, dg, db, a, light, dyeTicks, hasActiveDye, radius, dyeSourceAngle, time * 0.1f);
            drawRing(poseStack, consumer, 0.5f, 0.5f, radius * 0.85f, y, 0.02f, segments, r, g, b, dr, dg, db, a, light, dyeTicks, hasActiveDye, radius, dyeSourceAngle, time * -0.15f);
            drawRing(poseStack, consumer, 0.5f, 0.5f, radius * 0.7f, y, 0.02f, segments, r, g, b, dr, dg, db, a, light, dyeTicks, hasActiveDye, radius, dyeSourceAngle, time * 0.2f);
        } else if (radius < 2.5f) {
            // Tier 2 (5x5): 4 concentric layers of circles
            drawRing(poseStack, consumer, 0.5f, 0.5f, radius, y, 0.04f, segments, r, g, b, dr, dg, db, a, light, dyeTicks, hasActiveDye, radius, dyeSourceAngle, time * 0.1f);
            drawRing(poseStack, consumer, 0.5f, 0.5f, radius * 0.85f, y, 0.025f, segments, r, g, b, dr, dg, db, a, light, dyeTicks, hasActiveDye, radius, dyeSourceAngle, time * -0.15f);
            drawRing(poseStack, consumer, 0.5f, 0.5f, radius * 0.7f, y, 0.025f, segments, r, g, b, dr, dg, db, a, light, dyeTicks, hasActiveDye, radius, dyeSourceAngle, time * 0.2f);
            drawRing(poseStack, consumer, 0.5f, 0.5f, radius * 0.55f, y, 0.02f, segments, r, g, b, dr, dg, db, a, light, dyeTicks, hasActiveDye, radius, dyeSourceAngle, time * -0.25f);
        } else if (radius < 3.5f) {
            // Tier 3 (7x7): 5 concentric layers of circles
            drawRing(poseStack, consumer, 0.5f, 0.5f, radius, y, 0.05f, segments, r, g, b, dr, dg, db, a, light, dyeTicks, hasActiveDye, radius, dyeSourceAngle, time * 0.1f);
            drawRing(poseStack, consumer, 0.5f, 0.5f, radius * 0.83f, y, 0.03f, segments, r, g, b, dr, dg, db, a, light, dyeTicks, hasActiveDye, radius, dyeSourceAngle, time * -0.15f);
            drawRing(poseStack, consumer, 0.5f, 0.5f, radius * 0.7f, y, 0.03f, segments, r, g, b, dr, dg, db, a, light, dyeTicks, hasActiveDye, radius, dyeSourceAngle, time * 0.2f);
            drawRing(poseStack, consumer, 0.5f, 0.5f, radius * 0.5f, y, 0.02f, segments, r, g, b, dr, dg, db, a, light, dyeTicks, hasActiveDye, radius, dyeSourceAngle, time * -0.25f);
            drawRing(poseStack, consumer, 0.5f, 0.5f, radius * 0.33f, y, 0.02f, segments, r, g, b, dr, dg, db, a, light, dyeTicks, hasActiveDye, radius, dyeSourceAngle, time * 0.3f);
        } else {
            // Tier 4 (9x9): 6 concentric layers of circles
            drawRing(poseStack, consumer, 0.5f, 0.5f, radius, y, 0.06f, segments, r, g, b, dr, dg, db, a, light, dyeTicks, hasActiveDye, radius, dyeSourceAngle, time * 0.1f);
            drawRing(poseStack, consumer, 0.5f, 0.5f, radius * 0.88f, y, 0.04f, segments, r, g, b, dr, dg, db, a, light, dyeTicks, hasActiveDye, radius, dyeSourceAngle, time * -0.15f);
            drawRing(poseStack, consumer, 0.5f, 0.5f, radius * 0.75f, y, 0.04f, segments, r, g, b, dr, dg, db, a, light, dyeTicks, hasActiveDye, radius, dyeSourceAngle, time * 0.2f);
            drawRing(poseStack, consumer, 0.5f, 0.5f, radius * 0.62f, y, 0.03f, segments, r, g, b, dr, dg, db, a, light, dyeTicks, hasActiveDye, radius, dyeSourceAngle, time * -0.25f);
            drawRing(poseStack, consumer, 0.5f, 0.5f, radius * 0.5f, y, 0.03f, segments, r, g, b, dr, dg, db, a, light, dyeTicks, hasActiveDye, radius, dyeSourceAngle, time * 0.3f);
            drawRing(poseStack, consumer, 0.5f, 0.5f, radius * 0.25f, y, 0.02f, segments, r, g, b, dr, dg, db, a, light, dyeTicks, hasActiveDye, radius, dyeSourceAngle, time * -0.35f);
        }

        // Draw polygrams, stars, and vertex circles dynamically based on points count
        if (points > 0) {
            float speed = (radius < 1.5f) ? 0.8f : (radius < 2.5f) ? 0.6f : (radius < 3.5f) ? 0.5f : 0.4f;
            float rotationAngle = -90.0f + time * speed;
            float vertexRadius = (radius >= 3.5f) ? radius * 0.75f : radius * 0.7f;
            float vertexCircleRadius = (radius < 1.5f) ? 0.15f : (radius < 2.5f) ? 0.2f : (radius < 3.5f) ? 0.25f : 0.3f;

            // Draw polygon outline (for 3+ points, or 2 points which draws a line back/forth)
            if (points >= 2) {
                drawPolygram(poseStack, consumer, points, 1, vertexRadius, rotationAngle, y, 0.02f, r, g, b, dr, dg, db, a, light, dyeTicks, hasActiveDye, radius, dyeSourceAngle);
            }

            // Draw star outline for 5+ points
            if (points >= 5) {
                int starStep = points / 2;
                drawPolygram(poseStack, consumer, points, starStep, vertexRadius, rotationAngle, y, 0.015f, r, g, b, dr, dg, db, a, light, dyeTicks, hasActiveDye, radius, dyeSourceAngle);
            }

            // Draw inner star for 7+ points
            if (points >= 7) {
                int innerStep = (points - 1) / 2;
                drawPolygram(poseStack, consumer, points, innerStep, vertexRadius * 0.7f, rotationAngle * -1.2f, y, 0.015f, r, g, b, dr, dg, db, a, light, dyeTicks, hasActiveDye, radius, dyeSourceAngle);
            }

            // Draw inner polygon for 9+ points
            if (points >= 9) {
                drawPolygram(poseStack, consumer, 3, 1, vertexRadius * 0.33f, rotationAngle * -1.5f, y, 0.02f, r, g, b, dr, dg, db, a, light, dyeTicks, hasActiveDye, radius, dyeSourceAngle);
            }

            // Draw circles at the vertices
            drawVertexCircles(poseStack, consumer, points, vertexRadius, vertexCircleRadius, rotationAngle, y, 0.015f, r, g, b, dr, dg, db, a, light, dyeTicks, hasActiveDye, radius, dyeSourceAngle);
        }
    }

    private void drawVertexCircles(PoseStack poseStack, VertexConsumer consumer, int points, float polyRadius, float vertexCircleRadius, float offsetDegrees, float y, float width, float r, float g, float b, float dr, float dg, float db, float a, int light, float dyeTicks, boolean hasActiveDye, float circleRadius, float dyeSourceAngle) {
        float angleStep = (float)(2.0 * Math.PI / points);
        float offsetRad = (float) Math.toRadians(offsetDegrees);
  
        for (int i = 0; i < points; i++) {
            float theta = offsetRad + i * angleStep;
            float cos = (float) Math.cos(theta);
            float sin = (float) Math.sin(theta);
  
            float cx = polyRadius * cos + 0.5f;
            float cz = polyRadius * sin + 0.5f;
  
            drawRing(poseStack, consumer, cx, cz, vertexCircleRadius, y, width, 24, r, g, b, dr, dg, db, a, light, dyeTicks, hasActiveDye, circleRadius, dyeSourceAngle, 0.0f);
            drawRing(poseStack, consumer, cx, cz, vertexCircleRadius * 0.4f, y, vertexCircleRadius * 0.4f, 16, r, g, b, dr, dg, db, a, light, dyeTicks, hasActiveDye, circleRadius, dyeSourceAngle, 0.0f);
        }
    }

    private void drawRing(PoseStack poseStack, VertexConsumer consumer, float centerX, float centerZ, float rOuter, float y, float width, int segments, float r, float g, float b, float dr, float dg, float db, float a, int light, float dyeTicks, boolean hasActiveDye, float circleRadius, float dyeSourceAngle, float offsetDegrees) {
        PoseStack.Pose pose = poseStack.last();
        
        float rInner = rOuter - width;
        float segmentAngle = (float)(2.0 * Math.PI / segments);
        float offsetRad = (float) Math.toRadians(offsetDegrees);

        for (int i = 0; i < segments; i++) {
            float theta1 = offsetRad + i * segmentAngle;
            float theta2 = offsetRad + (i + 1) * segmentAngle;

            float cos1 = (float) Math.cos(theta1);
            float sin1 = (float) Math.sin(theta1);
            float cos2 = (float) Math.cos(theta2);
            float sin2 = (float) Math.sin(theta2);

            // Compute vertex coords relative to output center (centerX, centerZ)
            float x1_o = rOuter * cos1 + centerX;
            float z1_o = rOuter * sin1 + centerZ;
            float x2_o = rOuter * cos2 + centerX;
            float z2_o = rOuter * sin2 + centerZ;

            float x1_i = rInner * cos1 + centerX;
            float z1_i = rInner * sin1 + centerZ;
            float x2_i = rInner * cos2 + centerX;
            float z2_i = rInner * sin2 + centerZ;

            // Compute dyeing colors
            int color1_o = getAnimatedColor(x1_o - 0.5f, z1_o - 0.5f, circleRadius, r, g, b, dr, dg, db, dyeTicks, hasActiveDye, dyeSourceAngle);
            int color2_o = getAnimatedColor(x2_o - 0.5f, z2_o - 0.5f, circleRadius, r, g, b, dr, dg, db, dyeTicks, hasActiveDye, dyeSourceAngle);
            int color1_i = getAnimatedColor(x1_i - 0.5f, z1_i - 0.5f, circleRadius, r, g, b, dr, dg, db, dyeTicks, hasActiveDye, dyeSourceAngle);
            int color2_i = getAnimatedColor(x2_i - 0.5f, z2_i - 0.5f, circleRadius, r, g, b, dr, dg, db, dyeTicks, hasActiveDye, dyeSourceAngle);

            float r1_o = ((color1_o >> 16) & 0xFF) / 255f;
            float g1_o = ((color1_o >> 8) & 0xFF) / 255f;
            float b1_o = (color1_o & 0xFF) / 255f;

            float r2_o = ((color2_o >> 16) & 0xFF) / 255f;
            float g2_o = ((color2_o >> 8) & 0xFF) / 255f;
            float b2_o = (color2_o & 0xFF) / 255f;

            float r1_i = ((color1_i >> 16) & 0xFF) / 255f;
            float g1_i = ((color1_i >> 8) & 0xFF) / 255f;
            float b1_i = (color1_i & 0xFF) / 255f;

            float r2_i = ((color2_i >> 16) & 0xFF) / 255f;
            float g2_i = ((color2_i >> 8) & 0xFF) / 255f;
            float b2_i = (color2_i & 0xFF) / 255f;

            consumer.addVertex(pose, x1_o, y, z1_o).setColor(r1_o, g1_o, b1_o, a).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
            consumer.addVertex(pose, x2_o, y, z2_o).setColor(r2_o, g2_o, b2_o, a).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
            consumer.addVertex(pose, x2_i, y, z2_i).setColor(r2_i, g2_i, b2_i, a).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
            consumer.addVertex(pose, x1_i, y, z1_i).setColor(r1_i, g1_i, b1_i, a).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
        }
    }

    private void drawPolygram(PoseStack poseStack, VertexConsumer consumer, int points, int step, float radius, float offsetDegrees, float y, float width, float r, float g, float b, float dr, float dg, float db, float a, int light, float dyeTicks, boolean hasActiveDye, float circleRadius, float dyeSourceAngle) {
        PoseStack.Pose pose = poseStack.last();
        float angleStep = (float)(2.0 * Math.PI / points);
        float offsetRad = (float) Math.toRadians(offsetDegrees);

        for (int i = 0; i < points; i++) {
            float theta1 = offsetRad + i * angleStep;
            float theta2 = offsetRad + ((i + step) % points) * angleStep;

            float cos1 = (float) Math.cos(theta1);
            float sin1 = (float) Math.sin(theta1);
            float cos2 = (float) Math.cos(theta2);
            float sin2 = (float) Math.sin(theta2);

            // Center: 0.5f, 0.5f
            float x1 = radius * cos1 + 0.5f;
            float z1 = radius * sin1 + 0.5f;
            float x2 = radius * cos2 + 0.5f;
            float z2 = radius * sin2 + 0.5f;

            // Draw line with width
            float dx = x2 - x1;
            float dz = z2 - z1;
            float len = (float) Math.sqrt(dx * dx + dz * dz);
            if (len == 0) continue;

            float nx = -dz / len * (width / 2.0f);
            float nz = dx / len * (width / 2.0f);

            int color1 = getAnimatedColor(radius * cos1, radius * sin1, circleRadius, r, g, b, dr, dg, db, dyeTicks, hasActiveDye, dyeSourceAngle);
            int color2 = getAnimatedColor(radius * cos2, radius * sin2, circleRadius, r, g, b, dr, dg, db, dyeTicks, hasActiveDye, dyeSourceAngle);

            float r1 = ((color1 >> 16) & 0xFF) / 255f;
            float g1 = ((color1 >> 8) & 0xFF) / 255f;
            float b1 = (color1 & 0xFF) / 255f;

            float r2 = ((color2 >> 16) & 0xFF) / 255f;
            float g2 = ((color2 >> 8) & 0xFF) / 255f;
            float b2 = (color2 & 0xFF) / 255f;

            consumer.addVertex(pose, x1 - nx, y, z1 - nz).setColor(r1, g1, b1, a).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
            consumer.addVertex(pose, x2 - nx, y, z2 - nz).setColor(r2, g2, b2, a).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
            consumer.addVertex(pose, x2 + nx, y, z2 + nz).setColor(r2, g2, b2, a).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
            consumer.addVertex(pose, x1 + nx, y, z1 + nz).setColor(r1, g1, b1, a).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
        }
    }

    private void drawLocalTriangle(PoseStack poseStack, VertexConsumer consumer, float size, float y, float r, float g, float b, float a, int light) {
        PoseStack.Pose pose = poseStack.last();
        float half = size / 2.0f;
        consumer.addVertex(pose, 0.5f, y, 0.5f - half).setColor(r, g, b, a).setUv(0.5f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
        consumer.addVertex(pose, 0.5f + half, y, 0.5f + half).setColor(r, g, b, a).setUv(1.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
        consumer.addVertex(pose, 0.5f - half, y, 0.5f + half).setColor(r, g, b, a).setUv(0.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
        consumer.addVertex(pose, 0.5f, y, 0.5f - half).setColor(r, g, b, a).setUv(0.5f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
    }

    private void drawLocalCapacitorPlates(PoseStack poseStack, VertexConsumer consumer, float size, float y, float r, float g, float b, float a, int light) {
        float half = size / 2.0f;
        float thickness = 0.03f;
        drawQuad(poseStack, consumer, 0.5f - half, y, 0.5f - 0.05f - thickness, 0.5f + half, y, 0.5f - 0.05f + thickness, r, g, b, a, light);
        drawQuad(poseStack, consumer, 0.5f - half, y, 0.5f + 0.05f - thickness, 0.5f + half, y, 0.5f + 0.05f + thickness, r, g, b, a, light);
    }

    private void drawLocalResonator(PoseStack poseStack, VertexConsumer consumer, float size, float y, float r, float g, float b, float a, int light) {
        float half = size / 2.0f;
        float thickness = 0.02f;
        drawFrame(poseStack, consumer, 0.5f - half/2, y, 0.5f - half/2, 0.5f + half/2, y, 0.5f + half/2, thickness, r, g, b, a, light);
        drawQuad(poseStack, consumer, 0.5f - half, y, 0.5f - half - thickness, 0.5f + half, y, 0.5f - half + thickness, r, g, b, a, light);
        drawQuad(poseStack, consumer, 0.5f - half, y, 0.5f + half - thickness, 0.5f + half, y, 0.5f + half + thickness, r, g, b, a, light);
    }

    private void drawDiagonalQuad(PoseStack poseStack, VertexConsumer consumer, 
                                  float x1, float x2, float z1, float z2, 
                                  float x3, float x4, float z3, float z4, 
                                  float y, float r, float g, float b, float a, int light) {
        PoseStack.Pose pose = poseStack.last();
        consumer.addVertex(pose, x1, y, z1).setColor(r, g, b, a).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
        consumer.addVertex(pose, x2, y, z2).setColor(r, g, b, a).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
        consumer.addVertex(pose, x3, y, z3).setColor(r, g, b, a).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
        consumer.addVertex(pose, x4, y, z4).setColor(r, g, b, a).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
    }

    private void drawLocalDiode(PoseStack poseStack, VertexConsumer consumer, float size, float y, float r, float g, float b, float a, int light, Direction facing) {
        poseStack.pushPose();
        poseStack.translate(0.5f, 0.0f, 0.5f);
        float angle = switch (facing) {
            case NORTH -> 0.0f;
            case SOUTH -> 180.0f;
            case EAST -> 90.0f;
            case WEST -> 270.0f;
            default -> 0.0f;
        };
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(angle));
        poseStack.translate(-0.5f, 0.0f, -0.5f);

        PoseStack.Pose pose = poseStack.last();
        float half = size / 2.0f;
        
        consumer.addVertex(pose, 0.5f, y, 0.5f - half).setColor(r, g, b, a).setUv(0.5f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
        consumer.addVertex(pose, 0.5f + half, y, 0.5f + half).setColor(r, g, b, a).setUv(1.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
        consumer.addVertex(pose, 0.5f - half, y, 0.5f + half).setColor(r, g, b, a).setUv(0.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
        consumer.addVertex(pose, 0.5f, y, 0.5f - half).setColor(r, g, b, a).setUv(0.5f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);

        float barThickness = 0.02f;
        drawQuad(poseStack, consumer, 0.5f - half, y, 0.5f - half - barThickness, 0.5f + half, y, 0.5f - half + barThickness, r, g, b, a, light);

        poseStack.popPose();
    }

    private int getDynamicColor(ScribedChalkBlockEntity chalkBE, float time) {
        EssenceType affinity = chalkBE.getActiveAffinity();
        if (affinity == EssenceType.REGULAR) {
            return chalkBE.getColor();
        }
        if (affinity.isDynamic()) {
            float speed = 0.05f;
            float cycle = time * speed;
            int r = (int) ((Math.sin(cycle) * 0.5f + 0.5f) * 255.0f);
            int g = (int) ((Math.sin(cycle + 2.094f) * 0.5f + 0.5f) * 255.0f);
            int b = (int) ((Math.sin(cycle + 4.188f) * 0.5f + 0.5f) * 255.0f);
            return (r << 16) | (g << 8) | b;
        } else {
            int[] rgb = affinity.getCurrentRGB(time);
            return (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
        }
    }

    private boolean shouldConnectSmart8(Level level, BlockPos pos, ScribedChalkBlockEntity.Direction8 dir8, boolean circuit) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ScribedChalkBlockEntity chalkBE) {
            int override = chalkBE.getConnectionOverride(dir8);
            if (override == 1) return true;
            if (override == 2) return false;
        }

        BlockPos neighborPos = pos.offset(dir8.getXOffset(), 0, dir8.getZOffset());
        BlockState state = level.getBlockState(pos);
        BlockState neighborState = level.getBlockState(neighborPos);
        if (!state.is(ModBlocks.SCRIBED_CHALK.get()) || !neighborState.is(ModBlocks.SCRIBED_CHALK.get())) {
            return false;
        }

        Direction dir = switch (dir8) {
            case NORTH -> Direction.NORTH;
            case SOUTH -> Direction.SOUTH;
            case EAST -> Direction.EAST;
            case WEST -> Direction.WEST;
            default -> null;
        };

        if (dir == null) {
            // Diagonal connection
            if (state.getValue(ScribedChalkBlock.NODE_TYPE) == ScribedChalkBlock.NodeType.DIODE) {
                return false;
            }
            ScribedChalkBlock.NodeType neighborType = neighborState.getValue(ScribedChalkBlock.NODE_TYPE);
            if (neighborType == ScribedChalkBlock.NodeType.DIODE || 
                neighborType == ScribedChalkBlock.NodeType.AND_GATE ||
                neighborType == ScribedChalkBlock.NodeType.OR_GATE ||
                neighborType == ScribedChalkBlock.NodeType.NOT_GATE) {
                return false;
            }
            return shouldConnectSmart(level, pos, neighborPos, null, circuit);
        }

        return shouldConnectSmart(level, pos, neighborPos, dir, circuit);
    }

    private boolean shouldConnectSmart(Level level, BlockPos pos, BlockPos neighborPos, Direction dir, boolean circuit) {
        BlockState state = level.getBlockState(pos);
        if (!state.is(ModBlocks.SCRIBED_CHALK.get())) {
            return false;
        }
        if (state.getValue(ScribedChalkBlock.NODE_TYPE) == ScribedChalkBlock.NodeType.DIODE) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ScribedChalkBlockEntity chalkBE) {
                if (dir != chalkBE.getFacing() && dir != chalkBE.getFacing().getOpposite()) {
                    return false;
                }
            }
        }

        BlockState neighborState = level.getBlockState(neighborPos);
        if (!neighborState.is(ModBlocks.SCRIBED_CHALK.get()) || 
            neighborState.getValue(ScribedChalkBlock.CIRCUIT) != circuit) {
            return false;
        }

        if (neighborState.getValue(ScribedChalkBlock.NODE_TYPE) == ScribedChalkBlock.NodeType.DIODE) {
            BlockEntity neighborBE = level.getBlockEntity(neighborPos);
            if (neighborBE instanceof ScribedChalkBlockEntity neighborChalk) {
                if (dir.getOpposite() != neighborChalk.getFacing() && dir.getOpposite() != neighborChalk.getFacing().getOpposite()) {
                    return false;
                }
            }
        }

        if (!circuit) {
            return true;
        }

        // Check for parallel lines running side-by-side in the same direction
        if (dir == Direction.NORTH || dir == Direction.SOUTH) {
            boolean selfHasEast = hasNeighborInDirection(level, pos, Direction.EAST, circuit);
            boolean neighborHasEast = hasNeighborInDirection(level, neighborPos, Direction.EAST, circuit);
            if (selfHasEast && neighborHasEast) {
                return false;
            }
            boolean selfHasWest = hasNeighborInDirection(level, pos, Direction.WEST, circuit);
            boolean neighborHasWest = hasNeighborInDirection(level, neighborPos, Direction.WEST, circuit);
            if (selfHasWest && neighborHasWest) {
                return false;
            }
        } else if (dir == Direction.EAST || dir == Direction.WEST) {
            boolean selfHasNorth = hasNeighborInDirection(level, pos, Direction.NORTH, circuit);
            boolean neighborHasNorth = hasNeighborInDirection(level, neighborPos, Direction.NORTH, circuit);
            if (selfHasNorth && neighborHasNorth) {
                return false;
            }
            boolean selfHasSouth = hasNeighborInDirection(level, pos, Direction.SOUTH, circuit);
            boolean neighborHasSouth = hasNeighborInDirection(level, neighborPos, Direction.SOUTH, circuit);
            if (selfHasSouth && neighborHasSouth) {
                return false;
            }
        }

        return true;
    }

    private boolean hasNeighborInDirection(Level level, BlockPos pos, Direction dir, boolean circuit) {
        BlockState state = level.getBlockState(pos.relative(dir));
        return state.is(ModBlocks.SCRIBED_CHALK.get()) && state.getValue(ScribedChalkBlock.CIRCUIT) == circuit;
    }

    private int getAnimatedColor(float x, float z, float radius, float r, float g, float b, float dr, float dg, float db, float dyeTicks, boolean hasActiveDye, float dyeSourceAngle) {
        int defaultColor = (((int)(r * 255)) << 16) | (((int)(g * 255)) << 8) | ((int)(b * 255));
        if (!hasActiveDye) return defaultColor;

        int targetColor = (((int)(dr * 255)) << 16) | (((int)(dg * 255)) << 8) | ((int)(db * 255));

        float theta = (float) Math.atan2(z, x);
        float distR = (float) Math.sqrt(x * x + z * z);

        float distPerimeter = radius * getAngularDistance(theta, dyeSourceAngle);

        float distRadial = radius - distR;
        float totalDist = distPerimeter + distRadial;

        float propagationTicks = totalDist * 8.0f; // 8 ticks per block
        if (dyeTicks < propagationTicks) {
            return defaultColor;
        } else if (dyeTicks > propagationTicks + 15.0f) {
            return targetColor;
        } else {
            float factor = (dyeTicks - propagationTicks) / 15.0f;
            return lerpColor(defaultColor, targetColor, factor);
        }
    }

    private float getAngularDistance(float a1, float a2) {
        float diff = Math.abs(a1 - a2) % (float)(2 * Math.PI);
        return diff > Math.PI ? (float)(2 * Math.PI) - diff : diff;
    }

    private int lerpColor(int c1, int c2, float f) {
        int r1 = (c1 >> 16) & 0xFF;
        int g1 = (c1 >> 8) & 0xFF;
        int b1 = c1 & 0xFF;

        int r2 = (c2 >> 16) & 0xFF;
        int g2 = (c2 >> 8) & 0xFF;
        int b2 = c2 & 0xFF;

        int red = (int)(r1 + (r2 - r1) * f);
        int green = (int)(g1 + (g2 - g1) * f);
        int blue = (int)(b1 + (b2 - b1) * f);

        return (red << 16) | (green << 8) | blue;
    }

    private void drawFrame(PoseStack poseStack, VertexConsumer consumer, float minX, float y, float minZ, float maxX, float y2, float maxZ, float width, float r, float g, float b, float a, int light) {
        drawQuad(poseStack, consumer, minX, y, minZ, maxX, y, minZ + width, r, g, b, a, light); // North
        drawQuad(poseStack, consumer, minX, y, maxZ - width, maxX, y, maxZ, r, g, b, a, light); // South
        drawQuad(poseStack, consumer, minX, y, minZ + width, minX + width, y, maxZ - width, r, g, b, a, light); // West
        drawQuad(poseStack, consumer, maxX - width, y, minZ + width, maxX, y, maxZ - width, r, g, b, a, light); // East
    }

    private void drawQuad(PoseStack poseStack, VertexConsumer consumer, float minX, float y, float minZ, float maxX, float y2, float maxZ, float r, float g, float b, float a, int light) {
        PoseStack.Pose pose = poseStack.last();
        consumer.addVertex(pose, minX, y, maxZ).setColor(r, g, b, a).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
        consumer.addVertex(pose, maxX, y, maxZ).setColor(r, g, b, a).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
        consumer.addVertex(pose, maxX, y, minZ).setColor(r, g, b, a).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
        consumer.addVertex(pose, minX, y, minZ).setColor(r, g, b, a).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
    }

    private void drawDiamond(PoseStack poseStack, VertexConsumer consumer, float minX, float y, float minZ, float maxX, float y2, float maxZ, float r, float g, float b, float a, int light) {
        PoseStack.Pose pose = poseStack.last();
        float midX = (minX + maxX) / 2f;
        float midZ = (minZ + maxZ) / 2f;

        consumer.addVertex(pose, midX, y, maxZ).setColor(r, g, b, a).setUv(0.5f, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
        consumer.addVertex(pose, maxX, y, midZ).setColor(r, g, b, a).setUv(1, 0.5f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
        consumer.addVertex(pose, midX, y, minZ).setColor(r, g, b, a).setUv(0.5f, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
        consumer.addVertex(pose, minX, y, midZ).setColor(r, g, b, a).setUv(0, 0.5f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
    }

    private void drawLocalRing(PoseStack poseStack, VertexConsumer consumer, float rOuter, float y, float width, int segments, float r, float g, float b, float a, int light) {
        PoseStack.Pose pose = poseStack.last();
        float rInner = rOuter - width;
        float segmentAngle = (float)(2.0 * Math.PI / segments);

        for (int i = 0; i < segments; i++) {
            float theta1 = i * segmentAngle;
            float theta2 = (i + 1) * segmentAngle;

            float cos1 = (float) Math.cos(theta1);
            float sin1 = (float) Math.sin(theta1);
            float cos2 = (float) Math.cos(theta2);
            float sin2 = (float) Math.sin(theta2);

            float x1_o = rOuter * cos1 + 0.5f;
            float z1_o = rOuter * sin1 + 0.5f;
            float x2_o = rOuter * cos2 + 0.5f;
            float z2_o = rOuter * sin2 + 0.5f;

            float x1_i = rInner * cos1 + 0.5f;
            float z1_i = rInner * sin1 + 0.5f;
            float x2_i = rInner * cos2 + 0.5f;
            float z2_i = rInner * sin2 + 0.5f;

            consumer.addVertex(pose, x1_o, y, z1_o).setColor(r, g, b, a).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
            consumer.addVertex(pose, x2_o, y, z2_o).setColor(r, g, b, a).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
            consumer.addVertex(pose, x2_i, y, z2_i).setColor(r, g, b, a).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
            consumer.addVertex(pose, x1_i, y, z1_i).setColor(r, g, b, a).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
        }
    }

    public static class ChalkRenderState extends BlockEntityRenderState {
        public int color;
        public int defaultColor;
        public ScribedChalkBlock.NodeType nodeType;
        public boolean hasStoredOrbisCell;
        public final ItemStackRenderState storedOrbisCellState = new ItemStackRenderState();
        public boolean hasStoredRune;
        public final ItemStackRenderState storedRuneState = new ItemStackRenderState();
        public boolean hasStoredItem;
        public final ItemStackRenderState storedItemState = new ItemStackRenderState();
        public float time;
        // Connections:
        public boolean connectNorth;
        public boolean connectSouth;
        public boolean connectEast;
        public boolean connectWest;
        public boolean connectNorthEast;
        public boolean connectNorthWest;
        public boolean connectSouthEast;
        public boolean connectSouthWest;
        public boolean isCircuit;
        public Direction facing = Direction.NORTH;
        public String runeUnicode = "";
        public float dyeTicks;
        public boolean hasActiveDye;
        public int activeDyeColor;
        public int distance;
        public boolean isProcessing;
        public int essenceLevel;
        public int propagationStrength;
        public Vec3 targetNodeOffset;
        public float timePulseOffset = 0.0f; // for wisp traveling animation
        public int delayTicks = 1;
        public int processingProgress = 0;
        public int processingTimeTotal = 0;

        // Magic circle state:
        public int circleTier = 0;
        public boolean circleHasActiveDye;
        public int circleDyeColor;
        public float circleDyeTicks;
        public float circleDyeSourceAngle = 0.0f;
        public boolean isInMagicCircle = false;

        public static class RotatingNodeState {
            public ScribedChalkBlock.NodeType nodeType;
            public String textSymbol = "";
            public int color;
            public boolean hasStoredOrbisCell;
            public final ItemStackRenderState storedOrbisCellState = new ItemStackRenderState();
            public boolean hasStoredRune;
            public final ItemStackRenderState storedRuneState = new ItemStackRenderState();
            public boolean hasStoredItem;
            public final ItemStackRenderState storedItemState = new ItemStackRenderState();
            public boolean isProcessing;
            public int essenceLevel;
        }
        public final java.util.List<RotatingNodeState> rotatingNodes = new java.util.ArrayList<>();
    }

    private static final String[] ALCHEMICAL_SYMBOLS = {
        "✦", "✧", "☼", "☾", "▲", "▼", "◆", "★", "☆", "α", "β", "γ", "δ", "θ", "λ", "μ", "ξ", "π", "σ", "φ", "ψ", "ω", "Ω", "Ψ", "Φ", "Δ", "Σ"
    };

    private static int getInvertedColor(int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        return 0xFF000000 | ((255 - r) << 16) | ((255 - g) << 8) | (255 - b);
    }

    private void drawOrbitingSymbols(PoseStack poseStack, Font font, MultiBufferSource bufferSource, float cx, float cz, float y, float orbitRadius, int color, float time, int light) {
        int numOrbitSymbols = 4;
        float angleStep = (float)(2.0 * Math.PI / numOrbitSymbols);
        float rotation = time * 0.5f; // orbit speed
        int invCol = getInvertedColor(color);
        
        String[] symbols = {"α", "δ", "λ", "Ω"}; // orbiting glyphs
        
        for (int j = 0; j < numOrbitSymbols; j++) {
            float theta = rotation + j * angleStep;
            float sx = cx + orbitRadius * (float) Math.cos(theta);
            float sz = cz + orbitRadius * (float) Math.sin(theta);
            
            poseStack.pushPose();
            poseStack.translate(sx, y + 0.003f, sz);
            poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90.0f));
            poseStack.mulPose(com.mojang.math.Axis.ZP.rotation((float)(theta + Math.PI / 2.0)));
            poseStack.scale(0.008f, -0.008f, 0.008f);
            
            String sym = symbols[j % symbols.length];
            int w = font.width(sym);
            font.drawInBatch(sym, -w / 2.0f, -font.lineHeight / 2.0f, invCol, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 15728880); // full bright glow!
            poseStack.popPose();
        }
    }

    private void drawMagicCircleSymbols(PoseStack poseStack, Font font, MultiBufferSource bufferSource, float radius, float y, float r, float g, float b, float time, boolean hasActiveDye, int dyeColor) {
        int numSymbols = (radius < 1.5f) ? 18 : (radius < 2.5f) ? 30 : (radius < 3.5f) ? 42 : 54;
        float symbolAngleStep = (float)(2.0 * Math.PI / numSymbols);
        float symbolRotation = time * (0.4f / radius); // rotate slower for larger circles for premium feel
        
        int circleColor;
        if (hasActiveDye) {
            circleColor = dyeColor;
        } else {
            int cr = Math.clamp((int)(r * 255), 0, 255);
            int cg = Math.clamp((int)(g * 255), 0, 255);
            int cb = Math.clamp((int)(b * 255), 0, 255);
            circleColor = (cr << 16) | (cg << 8) | cb;
        }
        int circleInvCol = getInvertedColor(circleColor);

        for (int j = 0; j < numSymbols; j++) {
            float theta = symbolRotation + j * symbolAngleStep;
            float sx = radius * 0.93f * (float) Math.cos(theta) + 0.5f;
            float sz = radius * 0.93f * (float) Math.sin(theta) + 0.5f;
            
            String sym = ALCHEMICAL_SYMBOLS[j % ALCHEMICAL_SYMBOLS.length];
            
            poseStack.pushPose();
            poseStack.translate(sx, y + 0.002f, sz);
            poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90.0f));
            poseStack.mulPose(com.mojang.math.Axis.ZP.rotation((float)(theta + Math.PI / 2.0)));
            poseStack.scale(0.012f, -0.012f, 0.012f);
            
            int w = font.width(sym);
            font.drawInBatch(sym, -w / 2.0f, -font.lineHeight / 2.0f, circleInvCol, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 15728880); // full bright glow!
            poseStack.popPose();
        }
    }

    private void drawLocalEssenceBank(PoseStack poseStack, VertexConsumer consumer, float size, float y, float r, float g, float b, float a, int light) {
        float r1 = size * 0.4f;
        float r2 = size * 0.8f;
        float thickness = 0.015f;
        
        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(i * 60.0);
            float dx1 = 0.5f + (float) Math.cos(angle) * r1;
            float dz1 = 0.5f + (float) Math.sin(angle) * r1;
            float dx2 = 0.5f + (float) Math.cos(angle) * r2;
            float dz2 = 0.5f + (float) Math.sin(angle) * r2;
            
            drawLocalLine(poseStack, consumer, dx1, dz1, dx2, dz2, y, thickness, r, g, b, a, light);
        }
        
        drawHexagon(poseStack, consumer, r1, y, thickness, r, g, b, a, light);
        drawHexagon(poseStack, consumer, r2, y, thickness, r, g, b, a, light);
    }
    
    private void drawHexagon(PoseStack poseStack, VertexConsumer consumer, float radius, float y, float thickness, float r, float g, float b, float a, int light) {
        for (int i = 0; i < 6; i++) {
            double angle1 = Math.toRadians(i * 60.0);
            double angle2 = Math.toRadians(((i + 1) % 6) * 60.0);
            float x1 = 0.5f + (float) Math.cos(angle1) * radius;
            float z1 = 0.5f + (float) Math.sin(angle1) * radius;
            float x2 = 0.5f + (float) Math.cos(angle2) * radius;
            float z2 = 0.5f + (float) Math.sin(angle2) * radius;
            
            drawLocalLine(poseStack, consumer, x1, z1, x2, z2, y, thickness, r, g, b, a, light);
        }
    }

    private void drawLocalLine(PoseStack poseStack, VertexConsumer consumer, float x1, float z1, float x2, float z2, float y, float thickness, float r, float g, float b, float a, int light) {
        float dx = x2 - x1;
        float dz = z2 - z1;
        float len = (float) Math.sqrt(dx * dx + dz * dz);
        if (len < 1e-5) return;
        
        float nx = -dz / len * (thickness / 2f);
        float nz = dx / len * (thickness / 2f);
        
        float xa = x1 - nx;
        float za = z1 - nz;
        float xb = x1 + nx;
        float zb = z1 + nz;
        float xc = x2 + nx;
        float zc = z2 + nz;
        float xd = x2 - nx;
        float zd = z2 - nz;
        
        PoseStack.Pose pose = poseStack.last();
        consumer.addVertex(pose, xa, y, za).setColor(r, g, b, a).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
        consumer.addVertex(pose, xb, y, zb).setColor(r, g, b, a).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
        consumer.addVertex(pose, xc, y, zc).setColor(r, g, b, a).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
        consumer.addVertex(pose, xd, y, zd).setColor(r, g, b, a).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0, 1, 0);
    }
}
