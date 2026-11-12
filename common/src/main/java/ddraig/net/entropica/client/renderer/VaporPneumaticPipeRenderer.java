package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.api.materia.MateriaFumusStack;
import ddraig.net.entropica.api.materia.MateriaStack;
import ddraig.net.entropica.api.materia.MateriaSublimataStack;
import ddraig.net.entropica.api.materia.MateriaLiquidaStack;
import ddraig.net.entropica.api.materia.MateriaVolatilisStack;
import ddraig.net.entropica.api.materia.MateriaCoagulataStack;
import ddraig.net.entropica.api.materia.MateriaIchorStack;
import ddraig.net.entropica.api.materia.MateriaTransmutataStack;
import ddraig.net.entropica.api.materia.MateriaPerfectaStack;
import ddraig.net.entropica.api.materia.MateriaLiminaliaStack;
import ddraig.net.entropica.api.pressure.IPressureHandler;
import ddraig.net.entropica.block.VaporPneumaticDiverterBlock;
import ddraig.net.entropica.block.VaporPneumaticOneWayValveBlock;
import ddraig.net.entropica.block.VaporPneumaticPipeBlock;
import ddraig.net.entropica.block.entity.VaporPneumaticPipeBlockEntity;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class VaporPneumaticPipeRenderer implements BlockEntityRenderer<BlockEntity, VaporPneumaticPipeRenderer.VisFumePipeRenderState> {

    private static final ResourceLocation GAS_ANIM_TEXTURE = ResourceLocation.fromNamespaceAndPath("entropica", "textures/misc/fume_gas_anim.png");

    public VaporPneumaticPipeRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public VisFumePipeRenderState createRenderState() {
        return new VisFumePipeRenderState();
    }

    private void setDirectionState(VisFumePipeRenderState state, Direction dir, boolean value) {
        switch (dir) {
            case NORTH -> state.north = value;
            case SOUTH -> state.south = value;
            case EAST  -> state.east = value;
            case WEST  -> state.west = value;
            case UP    -> state.up = value;
            case DOWN  -> state.down = value;
        }
    }

    @Override
    public void extractRenderState(BlockEntity be, VisFumePipeRenderState state, float partialTick,
                                   Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);

        MateriaStack fumeStack = MateriaFumusStack.EMPTY;
        int safeCapacity = 4000;
        if (be instanceof IPressureHandler handler) {
            fumeStack = handler.getMateriaInTank();
            safeCapacity = handler.getSafeCapacity();
        }

        if (fumeStack.isEmpty()) {
            state.type = null;
            state.amount = 0;
        } else {
            state.type = fumeStack.getType();
            state.amount = fumeStack.getAmount();
        }

        state.maxCapacity = safeCapacity;
        state.time = (be.getLevel() != null ? be.getLevel().getGameTime() : 0) + partialTick;
        state.isPurging = be instanceof VaporPneumaticPipeBlockEntity pipe && pipe.isBeingOverpowered();

        Player player = Minecraft.getInstance().player;
        state.isHoldingDetector = player != null && (
                player.getMainHandItem().is(ModItems.MATERIA_VALUE_DETECTOR.get()) ||
                        player.getOffhandItem().is(ModItems.MATERIA_VALUE_DETECTOR.get())
        );

        BlockState blockState = be.getBlockState();

        if (blockState.getBlock() instanceof VaporPneumaticOneWayValveBlock) {
            state.isOneWayValve = true;
            state.valveFacing = blockState.getValue(VaporPneumaticOneWayValveBlock.FACING);
        } else {
            state.isOneWayValve = false;
            state.valveFacing = null;
        }

        // Reset all directions to false first
        state.north = false;
        state.south = false;
        state.east  = false;
        state.west  = false;
        state.up    = false;
        state.down  = false;

        // Populate connection directions using the blockstate properties generically
        state.north = blockState.hasProperty(BlockStateProperties.NORTH) && blockState.getValue(BlockStateProperties.NORTH);
        state.south = blockState.hasProperty(BlockStateProperties.SOUTH) && blockState.getValue(BlockStateProperties.SOUTH);
        state.east  = blockState.hasProperty(BlockStateProperties.EAST)  && blockState.getValue(BlockStateProperties.EAST);
        state.west  = blockState.hasProperty(BlockStateProperties.WEST)  && blockState.getValue(BlockStateProperties.WEST);
        state.up    = blockState.hasProperty(BlockStateProperties.UP)    && blockState.getValue(BlockStateProperties.UP);
        state.down  = blockState.hasProperty(BlockStateProperties.DOWN)  && blockState.getValue(BlockStateProperties.DOWN);

        if (blockState.getBlock() instanceof VaporPneumaticDiverterBlock) {
            Direction inputFace = blockState.getValue(VaporPneumaticDiverterBlock.FACING);
            Direction playerPerspective = blockState.getValue(VaporPneumaticDiverterBlock.HORIZONTAL_FACING);

            Direction forwardFace = inputFace.getOpposite();
            Direction rightFace;
            Direction leftFace;

            if (inputFace.getAxis().isVertical()) {
                leftFace = playerPerspective.getCounterClockWise();
                rightFace = playerPerspective.getClockWise();
            } else {
                leftFace = inputFace.getClockWise();
                rightFace = inputFace.getCounterClockWise();
            }

            setDirectionState(state, inputFace, true);
            setDirectionState(state, forwardFace, blockState.getValue(VaporPneumaticDiverterBlock.FORWARD_OPEN));
            setDirectionState(state, leftFace, blockState.getValue(VaporPneumaticDiverterBlock.LEFT_OPEN));
            setDirectionState(state, rightFace, blockState.getValue(VaporPneumaticDiverterBlock.RIGHT_OPEN));
        }

        // Resolve Materia tier from class name
        state.materiaTier = 2; // Default to T2
        String beClass = be.getClass().getSimpleName();
        if (beClass.contains("HydraulicPipeline")) state.materiaTier = 4;
        else if (beClass.contains("VoltaicConduit")) state.materiaTier = 5;
        else if (beClass.contains("ViscousAgitator")) state.materiaTier = 6;
        else if (beClass.contains("SanguineConduit")) state.materiaTier = 7;
        else if (beClass.contains("EquilibriumConduit")) state.materiaTier = 8;
        else if (beClass.contains("PristineConduit")) state.materiaTier = 9;
        else if (beClass.contains("AthanorConduit")) state.materiaTier = 10;

        state.blockEntity = be;
    }

    @Override
    public void submit(VisFumePipeRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
                       CameraRenderState cameraState) {
        if (state.type == null || state.amount <= 0) return;

        int colorInt;
        if (state.type.isDynamic()) {
            float speed = 0.05f;
            float cycle = (System.currentTimeMillis() / 50) * speed;
            int rChan = (int) ((Math.sin(cycle) * 0.5f + 0.5f) * 255.0f);
            int gChan = (int) ((Math.sin(cycle + 2.094f) * 0.5f + 0.5f) * 255.0f);
            int bChan = (int) ((Math.sin(cycle + 4.188f) * 0.5f + 0.5f) * 255.0f);
            colorInt = (rChan << 16) | (gChan << 8) | bChan;
        } else {
            int[] rgb = state.type.getCurrentRGB(System.currentTimeMillis() / 50);
            colorInt = (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
        }
        float r = ((colorInt >> 16) & 0xFF) / 255.0f;
        float g = ((colorInt >> 8)  & 0xFF) / 255.0f;
        float b = (colorInt & 0xFF)          / 255.0f;

        // Transparency (alpha) is dynamically based on actual fill ratio
        float fillRatio = state.maxCapacity > 0 ? (float) state.amount / state.maxCapacity : 1.0f;
        float alpha = Math.max(0.2f, Math.min(1.0f, fillRatio));

        // heartbeat shimmer for T7 (Ichor - Divine Blood): slower, subtler breathing oscillation
        if (state.materiaTier == 7) {
            float pulse = 0.95f + 0.05f * (float) Math.sin(state.time * 1.5f);
            r *= pulse;
            g *= pulse;
            b *= pulse;
            alpha = Math.max(0.2f, Math.min(1.0f, alpha * pulse));
        }

        int frameCount = 32;
        float flowSpeed = 0.4f;
        int currentFrame = (int) (state.time * flowSpeed) % frameCount;
        float vMin = (float) currentFrame / frameCount;
        float vMax = (float) (currentFrame + 1) / frameCount;

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderType renderType = RenderType.entityTranslucent(GAS_ANIM_TEXTURE);
        VertexConsumer consumer = bufferSource.getBuffer(renderType);

        PoseStack.Pose pose = poseStack.last();
        float min = 0.385f;
        float max = 0.615f;
        int light = 15728880;

        // Bounding box connections
        float leftX = state.west ? 0.001f : 0.385f;
        float rightX = state.east ? 0.999f : 0.615f;
        float downY = state.down ? 0.001f : 0.385f;
        float upY = state.up ? 0.999f : 0.615f;
        float northZ = state.north ? 0.001f : 0.385f;
        float southZ = state.south ? 0.999f : 0.615f;

        // Calculate bottom-to-top fluid level clipping bounds if T4, T5, T6, or T7
        float clipMaxY = 0.999f;
        boolean isLiquid = state.materiaTier >= 4 && state.materiaTier <= 7;
        if (isLiquid) {
            float liquidFill = Math.max(0.05f, Math.min(1.0f, fillRatio));
            clipMaxY = downY + (upY - downY) * liquidFill;
        }

        // Fetch camera look vector for T10 Chrome reflections
        org.joml.Vector3f camLook = Minecraft.getInstance().gameRenderer.getMainCamera().getLookVector();

        // Render main core and active arms
        renderClippedCuboid(pose, consumer, min, min, min, max, max, max, r, g, b, alpha, light, vMin, vMax, clipMaxY, state.materiaTier, camLook, state.time);

        if (state.north) renderClippedCuboid(pose, consumer, min, min, 0.001f, max, max, min,   r, g, b, alpha, light, vMin, vMax, clipMaxY, state.materiaTier, camLook, state.time);
        if (state.south) renderClippedCuboid(pose, consumer, min, min, max,   max, max, 0.999f, r, g, b, alpha, light, vMin, vMax, clipMaxY, state.materiaTier, camLook, state.time);
        if (state.east)  renderClippedCuboid(pose, consumer, max, min, min,   0.999f, max, max, r, g, b, alpha, light, vMin, vMax, clipMaxY, state.materiaTier, camLook, state.time);
        if (state.west)  renderClippedCuboid(pose, consumer, 0.001f, min, min, min,  max, max,  r, g, b, alpha, light, vMin, vMax, clipMaxY, state.materiaTier, camLook, state.time);
        if (state.up)    renderClippedCuboid(pose, consumer, min, max, min,   max, 0.999f, max, r, g, b, alpha, light, vMin, vMax, clipMaxY, state.materiaTier, camLook, state.time);
        if (state.down)  renderClippedCuboid(pose, consumer, min, 0.001f, min, max,  min, max,  r, g, b, alpha, light, vMin, vMax, clipMaxY, state.materiaTier, camLook, state.time);

        // --- 1. T5 VOLTAIC SPARKS EFFECT (Jagged 3D electric arcs jumping between liquid and pipe walls) ---
        if (state.materiaTier == 5 && state.blockEntity != null) {
            // Rapidly jump positions based on time ticks
            long timeTicks = (long) (state.time * 15.0f); // jumps 15 times/sec (every 4 frames)
            java.util.Random rand = new java.util.Random(state.blockEntity.getBlockPos().hashCode() * 31L + timeTicks);
            
            for (int i = 0; i < 2; i++) {
                // P0: Start inside the liquid volume
                float x0 = min + rand.nextFloat() * (max - min);
                float z0 = min + rand.nextFloat() * (max - min);
                float y0 = downY + rand.nextFloat() * (clipMaxY - downY);
                
                // P3: End on a random pipe wall boundary
                float x3, y3, z3;
                if (rand.nextBoolean()) {
                    x3 = rand.nextBoolean() ? min : max;
                    y3 = min + rand.nextFloat() * (max - min);
                    z3 = min + rand.nextFloat() * (max - min);
                } else {
                    z3 = rand.nextBoolean() ? min : max;
                    x3 = min + rand.nextFloat() * (max - min);
                    y3 = min + rand.nextFloat() * (max - min);
                }
                
                // Render bright electric cyan sparks
                renderElectricArc(pose, consumer, x0, y0, z0, x3, y3, z3, 0.35f, 0.88f, 1.0f, 0.95f, light, rand);
            }
        }

        // --- 2. PINNED VALUE TEXT RENDERING ---
        if (state.isHoldingDetector) {
            poseStack.pushPose();

            boolean isLowPressure = state.amount > 0 && state.amount < 10;
            float xShake = 0;
            float yShake = 0;
            float scaleModifier = 1.0f;
            int renderColor = colorInt | 0xFF000000;
            String text = state.amount + " Vf";

            if (state.isPurging) {
                renderColor = 0xFFFF5555;
                scaleModifier = 1.2f + (float) Math.sin(state.time * 15.0f) * 0.1f;
                xShake = (float) Math.sin(state.time * 30.0f) * 0.04f;
                text = "!! PURGE !! " + text;
            } else if (isLowPressure) {
                float shakeStrength = 0.02f;
                float shakeSpeed = 20.0f;
                xShake = (float) Math.sin(state.time * shakeSpeed) * shakeStrength;
                yShake = (float) Math.cos(state.time * shakeSpeed * 1.2f) * shakeStrength;
                float flash = (float) (Math.sin(state.time * 10.0f) * 0.5 + 0.5);
                if (flash < 0.3f) renderColor = 0xFF555555;
                text = "⚠ " + text;
            }

            poseStack.translate(0.5 + xShake, 1.0 + yShake, 0.5);
            poseStack.mulPose(cameraState.orientation);

            float baseScale = -0.02F;
            poseStack.scale(baseScale * scaleModifier, baseScale * scaleModifier, baseScale * scaleModifier);

            Font font = Minecraft.getInstance().font;
            float xOffset = (float)(-font.width(text) / 2);
            Matrix4f textMatrix = poseStack.last().pose();

            font.drawInBatch(text, xOffset, 0, renderColor, true, textMatrix, bufferSource, Font.DisplayMode.NORMAL, 0, light);
            poseStack.popPose();
        }

        // --- 3. ONE-WAY VALVE ARROW RENDERING ---
        if (state.isOneWayValve && state.valveFacing != null) {
            poseStack.pushPose();

            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.mulPose(state.valveFacing.getRotation());
            poseStack.mulPose(Axis.YP.rotationDegrees(180));
            poseStack.translate(0, 0, 0.26);
            poseStack.mulPose(Axis.YP.rotationDegrees(180));

            poseStack.mulPose(Axis.ZP.rotationDegrees(-90));

            poseStack.scale(-0.03F, -0.03F, 0.03F);

            int arrowColor = state.amount > 0 ? colorInt | 0xFF000000 : 0xFFFFFFFF;
            String arrowStr = "➔";

            Font font = Minecraft.getInstance().font;
            float xOffset = (float)(-font.width(arrowStr) / 2);
            float yOffset = (float)(-font.lineHeight / 2);

            font.drawInBatch(arrowStr, xOffset, yOffset, arrowColor, true, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);

            poseStack.popPose();
        }

        bufferSource.endBatch();
    }

    private void renderClippedCuboid(PoseStack.Pose pose, VertexConsumer consumer,
                                     float minX, float minY, float minZ,
                                     float maxX, float maxY, float maxZ,
                                     float r, float g, float b, float a, int light, float vMin, float vMax,
                                     float clipMaxY, int tier, org.joml.Vector3f camLook, float time) {
        float cMaxY = Math.min(maxY, clipMaxY);
        if (minY >= cMaxY) return;

        boolean isLiminalis = (tier == 10);
        boolean isPerfecta = (tier == 9);

        renderQuadChrome(pose, consumer, minX, minY, maxZ, minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, Direction.DOWN,  r, g, b, a, light, vMin, vMax, isLiminalis, isPerfecta, camLook, time);
        renderQuadChrome(pose, consumer, minX, cMaxY, minZ, minX, cMaxY, maxZ, maxX, cMaxY, maxZ, maxX, cMaxY, minZ, Direction.UP,    r, g, b, a, light, vMin, vMax, isLiminalis, isPerfecta, camLook, time);
        renderQuadChrome(pose, consumer, maxX, minY, minZ, minX, minY, minZ, minX, cMaxY, minZ, maxX, cMaxY, minZ, Direction.NORTH, r, g, b, a, light, vMin, vMax, isLiminalis, isPerfecta, camLook, time);
        renderQuadChrome(pose, consumer, minX, minY, maxZ, maxX, minY, maxZ, maxX, cMaxY, maxZ, minX, cMaxY, maxZ, Direction.SOUTH, r, g, b, a, light, vMin, vMax, isLiminalis, isPerfecta, camLook, time);
        renderQuadChrome(pose, consumer, minX, minY, minZ, minX, minY, maxZ, minX, cMaxY, maxZ, minX, cMaxY, minZ, Direction.WEST,  r, g, b, a, light, vMin, vMax, isLiminalis, isPerfecta, camLook, time);
        renderQuadChrome(pose, consumer, maxX, minY, maxZ, maxX, minY, minZ, maxX, cMaxY, minZ, maxX, cMaxY, maxZ, Direction.EAST,  r, g, b, a, light, vMin, vMax, isLiminalis, isPerfecta, camLook, time);
    }

    private void renderQuadChrome(PoseStack.Pose pose, VertexConsumer consumer,
                                  float x1, float y1, float z1, float x2, float y2, float z2,
                                  float x3, float y3, float z3, float x4, float y4, float z4,
                                  Direction normal, float r, float g, float b, float a, int light, float vMin, float vMax,
                                  boolean isLiminalis, boolean isPerfecta, org.joml.Vector3f camLook, float time) {
        float nx = normal.getStepX();
        float ny = normal.getStepY();
        float nz = normal.getStepZ();

        if (isLiminalis && camLook != null) {
            // Chrome-like metallic reflection shading preserving original color
            float dot = Math.abs(nx * camLook.x() + ny * camLook.y() + nz * camLook.z());
            
            // Specular reflection term (sharp white reflection)
            float specular = (float) Math.pow(dot, 4.0);
            
            // Shaded ambient term (silhouette edges are darker but retain essence color)
            float ambient = 0.35f + 0.65f * dot;
            
            // Subtle shimmer pulse to keep metallic surfaces feeling alive
            float timePulse = 0.05f * (float) Math.sin(time * 2.0f + normal.get3DDataValue() * 1.5f);
            
            r = Math.min(1.0f, r * ambient + specular * 0.5f + timePulse);
            g = Math.min(1.0f, g * ambient + specular * 0.5f + timePulse);
            b = Math.min(1.0f, b * ambient + specular * 0.5f + timePulse);
            a = Math.max(a, 0.92f); 
        } else if (isPerfecta) {
            // Smooth, rapid sheen sweep with long cooldown (80% of the time faded to actual color)
            float cycle = time % 4.0f; // 4 second total cycle
            float sweepDuration = 0.8f; // sweep lasts 0.8 seconds
            
            float sheen = 0.0f;
            if (cycle < sweepDuration) {
                float progress = cycle / sweepDuration; // 0.0 to 1.0
                
                // Sweep position: from -1.0 to 2.0
                float sweepPos = -1.0f + 3.0f * progress;
                
                float avgX = (x1 + x2 + x3 + x4) / 4.0f;
                float avgY = (y1 + y2 + y3 + y4) / 4.0f;
                float avgZ = (z1 + z2 + z3 + z4) / 4.0f;
                float coord = avgX + avgY + avgZ;
                
                // Distance to the sweep line
                float dist = Math.abs(coord - sweepPos);
                
                // Sheen intensity curve: sharp peak
                float intensity = Math.max(0.0f, 1.0f - dist * 4.0f); // width of the sheen line
                if (intensity > 0.0f) {
                    float smoothIntensity = (float) Math.sin(intensity * Math.PI / 2);
                    // Fade in/out at the start and end of the sweep
                    float envelope = (float) Math.sin(progress * Math.PI);
                    sheen = smoothIntensity * envelope;
                    sheen = (float) Math.pow(sheen, 4.0); // make it sharper
                }
            }
            
            if (sheen > 0.0f) {
                r = Math.min(1.0f, r + sheen * 0.9f);
                g = Math.min(1.0f, g + sheen * 0.9f);
                b = Math.min(1.0f, b + sheen * 0.9f);
                a = Math.min(1.0f, a + sheen * 0.3f);
            }
        }

        consumer.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).setUv(0, vMin).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
        consumer.addVertex(pose, x2, y2, z2).setColor(r, g, b, a).setUv(1, vMin).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
        consumer.addVertex(pose, x3, y3, z3).setColor(r, g, b, a).setUv(1, vMax).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
        consumer.addVertex(pose, x4, y4, z4).setColor(r, g, b, a).setUv(0, vMax).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
    }

    private void renderElectricArc(PoseStack.Pose pose, VertexConsumer consumer,
                                   float x1, float y1, float z1,
                                   float x2, float y2, float z2,
                                   float r, float g, float b, float a, int light, java.util.Random rand) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float dz = z2 - z1;
        
        float dev = 0.04f; // jagged deviation
        float px1 = x1 + dx * 0.33f + (rand.nextFloat() - 0.5f) * dev;
        float py1 = y1 + dy * 0.33f + (rand.nextFloat() - 0.5f) * dev;
        float pz1 = z1 + dz * 0.33f + (rand.nextFloat() - 0.5f) * dev;

        float px2 = x1 + dx * 0.66f + (rand.nextFloat() - 0.5f) * dev;
        float py2 = y1 + dy * 0.66f + (rand.nextFloat() - 0.5f) * dev;
        float pz2 = z1 + dz * 0.66f + (rand.nextFloat() - 0.5f) * dev;
        
        float thickness = 0.008f;
        // Render 3 segments
        drawArcSegment(pose, consumer, x1, y1, z1, px1, py1, pz1, thickness, r, g, b, a, light);
        drawArcSegment(pose, consumer, px1, py1, pz1, px2, py2, pz2, thickness, r, g, b, a, light);
        drawArcSegment(pose, consumer, px2, py2, pz2, x2, y2, z2, thickness, r, g, b, a, light);
    }

    private void drawArcSegment(PoseStack.Pose pose, VertexConsumer consumer,
                                float x1, float y1, float z1,
                                float x2, float y2, float z2,
                                float thickness, float r, float g, float b, float a, int light) {
        Direction normal = Direction.UP;
        float nx = normal.getStepX(), ny = normal.getStepY(), nz = normal.getStepZ();
        consumer.addVertex(pose, x1 - thickness, y1, z1 - thickness).setColor(r, g, b, a).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
        consumer.addVertex(pose, x1 + thickness, y1, z1 + thickness).setColor(r, g, b, a).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
        consumer.addVertex(pose, x2 + thickness, y2, z2 + thickness).setColor(r, g, b, a).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
        consumer.addVertex(pose, x2 - thickness, y2, z2 - thickness).setColor(r, g, b, a).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
    }

    public static class VisFumePipeRenderState extends BlockEntityRenderState {
        public EssenceType type;
        public int amount;
        public int maxCapacity;
        public float time;
        public boolean isPurging;
        public boolean isHoldingDetector;
        public boolean isOneWayValve;
        public Direction valveFacing;
        public int materiaTier;
        public BlockEntity blockEntity;
        public boolean north, south, east, west, up, down;
    }
}