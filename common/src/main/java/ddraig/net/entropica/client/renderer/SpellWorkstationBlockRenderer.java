package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.block.ArcaneScribingTableBlock;
import ddraig.net.entropica.block.AutocastPedestalBlock;
import ddraig.net.entropica.block.RunicAnvilBlock;
import ddraig.net.entropica.block.entity.ArcaneScribingTableBlockEntity;
import ddraig.net.entropica.block.entity.AutocastPedestalBlockEntity;
import ddraig.net.entropica.block.entity.RunicAnvilBlockEntity;
import ddraig.net.entropica.client.renderer.spell.BlockbenchCustomMesh;
import ddraig.net.entropica.spell.vfx.SpellParticleHelper;
import net.minecraft.client.renderer.LevelRenderer;
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
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.Set;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/**
 * World {@link BlockEntityRenderer} implementations for Entropica's 3D custom Blockbench spellcrafting workstations:
 * <ul>
 *     <li>{@link ArcaneScribingTableBlockEntity} (2-block horizontal table rendered on primary {@code LEFT} anchor)</li>
 *     <li>{@link RunicAnvilBlockEntity} (2-block horizontal anvil + floating 2-part convergence, 3D Magic Circle,
 *         Ethereal Hammer smash, and floating finished item)</li>
 *     <li>{@link AutocastPedestalBlockEntity} (4 stone variants + conditional Orbis port & tinted Spellgem)</li>
 * </ul>
 */
public final class SpellWorkstationBlockRenderer {

    private static final ResourceLocation SCRIBING_TABLE_TEX =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/block/arcane_scribing_table.png");
    private static final ResourceLocation RUNIC_ANVIL_TEX =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "textures/block/runic_anvil.png");
    private static final ResourceLocation WHITE_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/misc/white.png");

    private static final int FULL_LIGHT = 15728880;

    private SpellWorkstationBlockRenderer() {
    }

    public static BlockEntityRenderer<ArcaneScribingTableBlockEntity, DoubleWorkstationRenderState> createScribingTable(
            BlockEntityRendererProvider.Context ctx
    ) {
        return new DoubleWorkstationRenderer<>("arcane_scribing_table", SCRIBING_TABLE_TEX);
    }

    public static BlockEntityRenderer<RunicAnvilBlockEntity, RunicAnvilRenderState> createRunicAnvil(
            BlockEntityRendererProvider.Context ctx
    ) {
        return new RunicAnvilRenderer(ctx);
    }

    public static BlockEntityRenderer<AutocastPedestalBlockEntity, PedestalRenderState> createAutocastPedestal(
            BlockEntityRendererProvider.Context ctx
    ) {
        return new PedestalRenderer();
    }

    public static class DoubleWorkstationRenderState extends BlockEntityRenderState {
        public boolean primaryHalf = true;
        public Direction facing = Direction.SOUTH;
        public int packedLight = FULL_LIGHT;
    }

    public static class RunicAnvilRenderState extends BlockEntityRenderState {
        public boolean primaryHalf = true;
        public Direction facing = Direction.SOUTH;
        public int packedLight = FULL_LIGHT;
        public float animTime = 0.0F;
        public int forgeTicks = 0;
        public float partialTick = 0.0F;
        public int primaryRgb = 0x62E8FF;
        public int accentRgb = 0xFFD45C;

        public boolean hasBase = false;
        public boolean hasCatalyst = false;
        public boolean hasOutput = false;
        public boolean hasReturnedGem = false;

        public final ItemStackRenderState baseItemState = new ItemStackRenderState();
        public final ItemStackRenderState catalystItemState = new ItemStackRenderState();
        public final ItemStackRenderState outputItemState = new ItemStackRenderState();
        public final ItemStackRenderState returnedGemState = new ItemStackRenderState();
    }

    public static class PedestalRenderState extends BlockEntityRenderState {
        public AutocastPedestalBlock.PedestalVariant variant = AutocastPedestalBlock.PedestalVariant.STONE;
        public Direction facing = Direction.NORTH;
        public boolean hasOrbisPort = false;
        public boolean hasMountedGem = false;
        public int gemTintRgb = 0xFFFFFF;
        public int packedLight = FULL_LIGHT;
    }

    // =========================================================================
    // SCRIBING TABLE RENDERER
    // =========================================================================

    private static final class DoubleWorkstationRenderer<T extends net.minecraft.world.level.block.entity.BlockEntity>
            implements BlockEntityRenderer<T, DoubleWorkstationRenderState> {

        private final String modelName;
        private final ResourceLocation texture;

        private DoubleWorkstationRenderer(String modelName, ResourceLocation texture) {
            this.modelName = modelName;
            this.texture = texture;
        }

        @Override
        public DoubleWorkstationRenderState createRenderState() {
            return new DoubleWorkstationRenderState();
        }

        @Override
        public void extractRenderState(
                T be,
                DoubleWorkstationRenderState state,
                float partialTick,
                Vec3 cameraPos,
                @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
        ) {
            BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
            BlockState bs = be.getBlockState();
            if (be instanceof ArcaneScribingTableBlockEntity st) {
                state.primaryHalf = st.isPrimaryHalf();
                state.facing = bs.hasProperty(ArcaneScribingTableBlock.FACING)
                        ? bs.getValue(ArcaneScribingTableBlock.FACING)
                        : Direction.SOUTH;
            }
            state.packedLight = be.getLevel() != null
                    ? LevelRenderer.getLightColor(be.getLevel(), be.getBlockPos())
                    : FULL_LIGHT;
        }

        @Override
        public void submit(
                DoubleWorkstationRenderState renderState,
                PoseStack poseStack,
                SubmitNodeCollector collector,
                CameraRenderState cameraRenderState
        ) {
            if (!renderState.primaryHalf) {
                return;
            }
            BlockbenchCustomMesh mesh = BlockbenchCustomMesh.getOrLoad(this.modelName);
            float rotY = switch (renderState.facing) {
                case EAST -> 90.0F;
                case NORTH -> 180.0F;
                case WEST -> -90.0F;
                default -> 0.0F; // SOUTH
            };

            poseStack.pushPose();
            poseStack.translate(0.5F, 0.0F, 0.5F);
            poseStack.mulPose(Axis.YP.rotationDegrees(rotY));
            poseStack.translate(0.0F, 0.0F, -0.5F);

            final int light = renderState.packedLight;
            collector.submitCustomGeometry(
                    poseStack,
                    RenderType.entityCutoutNoCull(this.texture),
                    (pose, consumer) -> mesh.emit(pose, consumer, light, OverlayTexture.NO_OVERLAY, null, null)
            );
            poseStack.popPose();
        }

        @Override
        public boolean shouldRenderOffScreen() {
            return true;
        }
    }

    // =========================================================================
    // RUNIC ANVIL RENDERER (2-PART CONVERGENCE + MAGIC CIRCLE + ETHEREAL HAMMER)
    // =========================================================================

    private static final class RunicAnvilRenderer
            implements BlockEntityRenderer<RunicAnvilBlockEntity, RunicAnvilRenderState> {

        private final ItemModelResolver itemModelResolver;

        private RunicAnvilRenderer(BlockEntityRendererProvider.Context ctx) {
            this.itemModelResolver = ctx.itemModelResolver();
        }

        @Override
        public RunicAnvilRenderState createRenderState() {
            return new RunicAnvilRenderState();
        }

        @Override
        public boolean shouldRenderOffScreen() {
            return true;
        }

        @Override
        public int getViewDistance() {
            return 128;
        }

        @Override
        public void extractRenderState(
                RunicAnvilBlockEntity be,
                RunicAnvilRenderState state,
                float partialTick,
                Vec3 cameraPos,
                @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
        ) {
            BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
            BlockState bs = be.getBlockState();
            state.primaryHalf = be.isPrimaryHalf();
            state.facing = bs.hasProperty(RunicAnvilBlock.FACING)
                    ? bs.getValue(RunicAnvilBlock.FACING)
                    : Direction.SOUTH;
            Level level = be.getLevel();
            state.packedLight = level != null
                    ? LevelRenderer.getLightColor(level, be.getBlockPos())
                    : FULL_LIGHT;
            state.animTime = (level != null ? (level.getGameTime() % 360000L) : 0L) + partialTick;
            state.forgeTicks = be.getForgeTicks();
            state.partialTick = partialTick;

            EssenceType visEssence = be.getVisualEssence();
            if (visEssence.isDynamic()) {
                state.primaryRgb = SpellParticleHelper.resolveVibrantPrimaryRgb(visEssence, state.animTime);
                state.accentRgb = SpellParticleHelper.resolveVibrantAccentRgb(visEssence, null, state.animTime);
            } else {
                state.primaryRgb = be.getPrimaryRgb();
                state.accentRgb = be.getAccentRgb();
            }

            if (!state.primaryHalf || level == null) {
                state.hasBase = false;
                state.hasCatalyst = false;
                state.hasOutput = false;
                state.hasReturnedGem = false;
                state.baseItemState.clear();
                state.catalystItemState.clear();
                state.outputItemState.clear();
                state.returnedGemState.clear();
                return;
            }

            int seed = (int) be.getBlockPos().asLong();

            ItemStack baseStack = be.getItem(RunicAnvilBlockEntity.SLOT_BASE);
            state.hasBase = !baseStack.isEmpty();
            if (state.hasBase) {
                this.itemModelResolver.updateForTopItem(state.baseItemState, baseStack, ItemDisplayContext.FIXED, level, null, seed);
            } else {
                state.baseItemState.clear();
            }

            ItemStack catStack = be.getItem(RunicAnvilBlockEntity.SLOT_CATALYST);
            state.hasCatalyst = !catStack.isEmpty();
            if (state.hasCatalyst) {
                this.itemModelResolver.updateForTopItem(state.catalystItemState, catStack, ItemDisplayContext.FIXED, level, null, seed + 1);
            } else {
                state.catalystItemState.clear();
            }

            ItemStack outStack = be.getItem(RunicAnvilBlockEntity.SLOT_OUTPUT);
            state.hasOutput = !outStack.isEmpty();
            if (state.hasOutput) {
                this.itemModelResolver.updateForTopItem(state.outputItemState, outStack, ItemDisplayContext.FIXED, level, null, seed + 2);
            } else {
                state.outputItemState.clear();
            }

            ItemStack retStack = be.getItem(RunicAnvilBlockEntity.SLOT_RETURNED_GEM);
            state.hasReturnedGem = !retStack.isEmpty();
            if (state.hasReturnedGem) {
                this.itemModelResolver.updateForTopItem(state.returnedGemState, retStack, ItemDisplayContext.FIXED, level, null, seed + 3);
            } else {
                state.returnedGemState.clear();
            }
        }

        @Override
        public void submit(
                RunicAnvilRenderState state,
                PoseStack poseStack,
                SubmitNodeCollector collector,
                CameraRenderState cameraRenderState
        ) {
            if (!state.primaryHalf) {
                return;
            }

            BlockbenchCustomMesh mesh = BlockbenchCustomMesh.getOrLoad("runic_anvil");
            float rotY = switch (state.facing) {
                case EAST -> 90.0F;
                case NORTH -> 180.0F;
                case WEST -> -90.0F;
                default -> 0.0F; // SOUTH
            };

            poseStack.pushPose();
            poseStack.translate(0.5F, 0.0F, 0.5F);
            poseStack.mulPose(Axis.YP.rotationDegrees(rotY));
            poseStack.translate(0.0F, 0.0F, -0.5F);

            // 1. Render the 2-block-wide custom 3D Runic Anvil mesh
            final int light = state.packedLight;
            collector.submitCustomGeometry(
                    poseStack,
                    RenderType.entityCutoutNoCull(RUNIC_ANVIL_TEX),
                    (pose, consumer) -> mesh.emit(pose, consumer, light, OverlayTexture.NO_OVERLAY, null, null)
            );

            float pr = ((state.primaryRgb >> 16) & 0xFF) / 255.0F;
            float pg = ((state.primaryRgb >> 8) & 0xFF) / 255.0F;
            float pb = (state.primaryRgb & 0xFF) / 255.0F;

            float ar = ((state.accentRgb >> 16) & 0xFF) / 255.0F;
            float ag = ((state.accentRgb >> 8) & 0xFF) / 255.0F;
            float ab = (state.accentRgb & 0xFF) / 255.0F;

            float time = state.animTime;
            float bob = Mth.sin(time * 0.09F) * 0.035F;

            // 2. Active Forging Sequence (Two Parts Converge -> Magic Circle Between Them -> Ethereal Hammer Smash!)
            if (state.forgeTicks > 0) {
                float t = Math.min(RunicAnvilBlockEntity.TOTAL_FORGE_TICKS, state.forgeTicks + state.partialTick);

                if (t < RunicAnvilBlockEntity.SMASH_IMPACT_TICK) {
                    float p1 = Mth.clamp(t / 28.0F, 0.0F, 1.0F);
                    float e1 = p1 * p1 * (3.0F - 2.0F * p1);

                    float p2 = Mth.clamp((t - 28.0F) / 16.0F, 0.0F, 1.0F);
                    float e2 = p2 * p2 * (3.0F - 2.0F * p2);

                    float catX = Mth.lerp(e2, Mth.lerp(e1, 0.23F, 0.30F), 0.52F);
                    float baseX = Mth.lerp(e2, Mth.lerp(e1, 0.81F, 0.74F), 0.52F);
                    float partY = Mth.lerp(e2, Mth.lerp(e1, 1.25F, 1.48F), 1.13F);
                    float spinSpeed = 3.5F + p1 * 7.5F + p2 * 10.0F;

                    if (state.hasCatalyst) {
                        poseStack.pushPose();
                        poseStack.translate(catX, partY, 0.50F);
                        poseStack.mulPose(Axis.YP.rotationDegrees(time * spinSpeed));
                        poseStack.scale(0.46F, 0.46F, 0.46F);
                        state.catalystItemState.submit(poseStack, collector, FULL_LIGHT, OverlayTexture.NO_OVERLAY, 0);
                        poseStack.popPose();
                    }

                    if (state.hasBase) {
                        poseStack.pushPose();
                        poseStack.translate(baseX, partY, 0.50F);
                        poseStack.mulPose(Axis.YP.rotationDegrees(-time * spinSpeed));
                        poseStack.mulPose(Axis.ZP.rotationDegrees(20.0F * (1.0F - e2)));
                        poseStack.scale(0.56F, 0.56F, 0.56F);
                        state.baseItemState.submit(poseStack, collector, FULL_LIGHT, OverlayTexture.NO_OVERLAY, 0);
                        poseStack.popPose();
                    }

                    // Magic Circle suspended between the two parts (tilting onto the anvil deck for the strike!)
                    final float circleScale = Math.max(0.05F, e1);
                    final float circleY = Mth.lerp(e2, 1.48F, 1.03F);
                    final float tiltToHorizontal = e2;
                    final float curCatX = catX;
                    final float curBaseX = baseX;
                    final float curPartY = partY;
                    final float rotDeg = time * 5.0F;

                    collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, vc) -> {
                        Matrix4f mat = pose.pose();
                        // Grounding anvil-deck magic seal
                        renderHorizontalMagicCircle(mat, vc, 0.52F, 1.02F, 0.50F, 0.36F * circleScale, -rotDeg * 0.7F,
                                pr, pg, pb, ar, ag, ab, 0.75F * circleScale);

                        // Primary Magic Circle created directly BETWEEN the two parts!
                        renderBetweenPartsMagicCircle(mat, vc, 0.52F, circleY, 0.50F, 0.30F * circleScale,
                                tiltToHorizontal, rotDeg, pr, pg, pb, ar, ag, ab, 0.92F);

                        // Energy tether beams linking Left Part -> Magic Circle <- Right Part
                        if (tiltToHorizontal < 0.92F) {
                            float beamAlpha = 0.80F * (1.0F - tiltToHorizontal);
                            renderEnergyBeam(mat, vc,
                                    new Vec3(curCatX, curPartY, 0.50D),
                                    new Vec3(0.52D, circleY, 0.50D),
                                    0.028F, pr, pg, pb, beamAlpha);
                            renderEnergyBeam(mat, vc,
                                    new Vec3(curBaseX, curPartY, 0.50D),
                                    new Vec3(0.52D, circleY, 0.50D),
                                    0.028F, ar, ag, ab, beamAlpha);
                        }
                    });

                    // Ethereal Hammer materializes, cocks back overhead, and smashes down!
                    if (t >= 26.0F) {
                        float windUp = Mth.clamp((t - 26.0F) / 18.0F, 0.0F, 1.0F);
                        float windEase = windUp * windUp * (3.0F - 2.0F * windUp);
                        float swingDeg;
                        float hammerAlpha;
                        if (t < 44.0F) {
                            swingDeg = Mth.lerp(windEase, -32.0F, -76.0F);
                            hammerAlpha = Mth.clamp((t - 26.0F) / 8.0F, 0.0F, 0.92F);
                        } else {
                            float slam = Mth.clamp((t - 44.0F) / 4.0F, 0.0F, 1.0F);
                            float slamAccel = slam * slam * slam;
                            swingDeg = Mth.lerp(slamAccel, -76.0F, 0.0F);
                            hammerAlpha = 0.96F;
                        }

                        poseStack.pushPose();
                        poseStack.translate(0.52F, 1.22F, -0.22F);
                        poseStack.mulPose(Axis.XP.rotationDegrees(swingDeg));
                        final float hAlpha = hammerAlpha;
                        final boolean showSlamWake = t >= 44.0F;
                        collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, vc) -> {
                            renderEtherealHammer(pose.pose(), vc, pr, pg, pb, ar, ag, ab, hAlpha, showSlamWake);
                        });
                        poseStack.popPose();
                    }
                } else {
                    // Post-Impact (ticks 48..68): Shockwave bursts, Ethereal Hammer dissolves, Finished Item rises!
                    float post = Mth.clamp((t - RunicAnvilBlockEntity.SMASH_IMPACT_TICK)
                            / (float) (RunicAnvilBlockEntity.TOTAL_FORGE_TICKS - RunicAnvilBlockEntity.SMASH_IMPACT_TICK), 0.0F, 1.0F);
                    float riseEase = 1.0F - (1.0F - post) * (1.0F - post) * (1.0F - post);

                    if (state.hasOutput) {
                        float outY = Mth.lerp(riseEase, 1.08F, 1.38F);
                        poseStack.pushPose();
                        poseStack.translate(0.52F, outY, 0.50F);
                        poseStack.mulPose(Axis.YP.rotationDegrees(time * 4.0F));
                        float outScale = Mth.lerp(riseEase, 0.45F, 0.68F);
                        poseStack.scale(outScale, outScale, outScale);
                        state.outputItemState.submit(poseStack, collector, FULL_LIGHT, OverlayTexture.NO_OVERLAY, 0);
                        poseStack.popPose();
                    }

                    // Shockwave ring on anvil deck
                    final float shockProg = post;
                    final float spin = time * 3.0F;
                    collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, vc) -> {
                        Matrix4f mat = pose.pose();
                        float ringR = Mth.lerp(shockProg, 0.20F, 0.95F);
                        float shockAlpha = (1.0F - shockProg) * (1.0F - shockProg) * 0.90F;
                        renderFlatRing(mat, vc, 0.52F, 1.03F, 0.50F, ringR * 0.78F, ringR, 24, 1.0F, 1.0F, 1.0F, shockAlpha);
                        renderHorizontalMagicCircle(mat, vc, 0.52F, 1.02F, 0.50F, 0.30F, spin, pr, pg, pb, ar, ag, ab, 0.65F);
                    });

                    // Dissolving Ethereal Hammer rebound
                    float hammerFade = (1.0F - post) * (1.0F - post);
                    if (hammerFade > 0.03F) {
                        float reboundDeg = -16.0F * Mth.sin(Math.min(1.0F, post * 2.0F) * Mth.HALF_PI);
                        poseStack.pushPose();
                        poseStack.translate(0.52F, 1.22F + post * 0.18F, -0.22F);
                        poseStack.mulPose(Axis.XP.rotationDegrees(reboundDeg));
                        final float hAlpha = 0.90F * hammerFade;
                        collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, vc) -> {
                            renderEtherealHammer(pose.pose(), vc, pr, pg, pb, ar, ag, ab, hAlpha, false);
                        });
                        poseStack.popPose();
                    }
                }

                poseStack.popPose();
                return;
            }

            // 3. Idle State: Either finished forged item floating above center OR waiting input part(s)
            if (state.hasOutput) {
                final float haloSpin = time * 1.6F;
                collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, vc) -> {
                    renderHorizontalMagicCircle(pose.pose(), vc, 0.52F, 1.02F, 0.50F, 0.28F, haloSpin,
                            pr, pg, pb, ar, ag, ab, 0.62F);
                });

                poseStack.pushPose();
                poseStack.translate(0.52F, 1.38F + bob, 0.50F);
                poseStack.mulPose(Axis.YP.rotationDegrees(time * 2.4F));
                poseStack.scale(0.68F, 0.68F, 0.68F);
                state.outputItemState.submit(poseStack, collector, FULL_LIGHT, OverlayTexture.NO_OVERLAY, 0);
                poseStack.popPose();

                if (state.hasReturnedGem) {
                    float orbitAng = time * 0.06F;
                    poseStack.pushPose();
                    poseStack.translate(0.52F + Mth.cos(orbitAng) * 0.30F, 1.28F - bob, 0.50F + Mth.sin(orbitAng) * 0.30F);
                    poseStack.mulPose(Axis.YP.rotationDegrees(-time * 3.2F));
                    poseStack.scale(0.42F, 0.42F, 0.42F);
                    state.returnedGemState.submit(poseStack, collector, FULL_LIGHT, OverlayTexture.NO_OVERLAY, 0);
                    poseStack.popPose();
                }
            } else {
                if (state.hasCatalyst) {
                    collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, vc) -> {
                        renderFlatRing(pose.pose(), vc, 0.23F, 1.02F, 0.50F, 0.11F, 0.15F, 16, pr, pg, pb, 0.55F);
                    });
                    poseStack.pushPose();
                    poseStack.translate(0.23F, 1.24F + bob, 0.50F);
                    poseStack.mulPose(Axis.YP.rotationDegrees(time * 2.5F));
                    poseStack.scale(0.48F, 0.48F, 0.48F);
                    state.catalystItemState.submit(poseStack, collector, FULL_LIGHT, OverlayTexture.NO_OVERLAY, 0);
                    poseStack.popPose();
                }

                if (state.hasBase) {
                    collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(WHITE_TEXTURE), (pose, vc) -> {
                        renderFlatRing(pose.pose(), vc, 0.81F, 1.02F, 0.50F, 0.14F, 0.19F, 16, ar, ag, ab, 0.55F);
                    });
                    poseStack.pushPose();
                    poseStack.translate(0.81F, 1.28F - bob, 0.50F);
                    poseStack.mulPose(Axis.YP.rotationDegrees(-time * 2.2F));
                    poseStack.mulPose(Axis.ZP.rotationDegrees(15.0F));
                    poseStack.scale(0.58F, 0.58F, 0.58F);
                    state.baseItemState.submit(poseStack, collector, FULL_LIGHT, OverlayTexture.NO_OVERLAY, 0);
                    poseStack.popPose();
                }
            }

            poseStack.popPose();
        }
    }

    // =========================================================================
    // PROCEDURAL 3D MAGIC CIRCLE & ETHEREAL HAMMER GEOMETRY HELPERS
    // =========================================================================

    private static void renderBetweenPartsMagicCircle(
            Matrix4f mat, VertexConsumer vc,
            float cx, float cy, float cz, float radius,
            float tiltToHorizontal, float spinDeg,
            float pr, float pg, float pb,
            float ar, float ag, float ab,
            float alpha
    ) {
        // Interpolate plane basis from vertical YZ-plane (normal +X, right between left & right parts)
        // to horizontal XZ-plane (normal +Y on the anvil deck)
        float tiltRad = tiltToHorizontal * Mth.HALF_PI;
        Vec3 uAxis = new Vec3(Mth.sin(tiltRad), Mth.cos(tiltRad), 0.0D);
        Vec3 vAxis = new Vec3(0.0D, 0.0D, 1.0D);
        Vec3 center = new Vec3(cx, cy, cz);

        renderOrientedMagicCircle(mat, vc, center, uAxis, vAxis, radius, spinDeg, pr, pg, pb, ar, ag, ab, alpha);

        // Secondary front-facing halo ring while vertical so players standing dead-center in front see full depth!
        if (tiltToHorizontal < 0.85F) {
            float crossAlpha = alpha * 0.55F * (1.0F - tiltToHorizontal);
            Vec3 frontU = new Vec3(1.0D, 0.0D, 0.0D);
            Vec3 frontV = new Vec3(0.0D, 1.0D, 0.0D);
            renderOrientedRing(mat, vc, center, frontU, frontV, radius * 0.78F, radius * 0.86F, 20, ar, ag, ab, crossAlpha);
        }
    }

    private static void renderHorizontalMagicCircle(
            Matrix4f mat, VertexConsumer vc,
            float cx, float cy, float cz, float radius, float spinDeg,
            float pr, float pg, float pb,
            float ar, float ag, float ab,
            float alpha
    ) {
        renderOrientedMagicCircle(
                mat, vc,
                new Vec3(cx, cy, cz),
                new Vec3(1.0D, 0.0D, 0.0D),
                new Vec3(0.0D, 0.0D, 1.0D),
                radius, spinDeg, pr, pg, pb, ar, ag, ab, alpha
        );
    }

    private static void renderOrientedMagicCircle(
            Matrix4f mat, VertexConsumer vc,
            Vec3 center, Vec3 uAxis, Vec3 vAxis,
            float radius, float spinDeg,
            float pr, float pg, float pb,
            float ar, float ag, float ab,
            float alpha
    ) {
        // Outer & inner concentric runic rings
        renderOrientedRing(mat, vc, center, uAxis, vAxis, radius * 0.90F, radius, 24, pr, pg, pb, alpha);
        renderOrientedRing(mat, vc, center, uAxis, vAxis, radius * 0.56F, radius * 0.63F, 20, ar, ag, ab, alpha * 0.88F);
        renderOrientedRing(mat, vc, center, uAxis, vAxis, 0.0F, radius * 0.16F, 12, 1.0F, 1.0F, 1.0F, alpha * 0.85F);

        // Counter-rotating hexagram star chords
        float spinRad = spinDeg * Mth.DEG_TO_RAD;
        float lineHalfW = Math.max(0.008F, radius * 0.032F);
        for (int tri = 0; tri < 2; tri++) {
            float offset = spinRad * (tri == 0 ? 1.0F : -0.8F) + tri * (Mth.PI / 3.0F);
            for (int k = 0; k < 3; k++) {
                float a0 = offset + k * (Mth.TWO_PI / 3.0F);
                float a1 = offset + ((k + 1) % 3) * (Mth.TWO_PI / 3.0F);
                Vec3 p0 = pointOnPlane(center, uAxis, vAxis, Mth.cos(a0) * radius * 0.88F, Mth.sin(a0) * radius * 0.88F);
                Vec3 p1 = pointOnPlane(center, uAxis, vAxis, Mth.cos(a1) * radius * 0.88F, Mth.sin(a1) * radius * 0.88F);
                Vec3 chordDir = p1.subtract(p0).normalize();
                Vec3 normal = uAxis.cross(vAxis).normalize();
                Vec3 perp = chordDir.cross(normal).normalize().scale(lineHalfW);
                float cr = tri == 0 ? pr : ar;
                float cg = tri == 0 ? pg : ag;
                float cb = tri == 0 ? pb : ab;
                emitDoubleQuad(mat, vc, p0.subtract(perp), p0.add(perp), p1.add(perp), p1.subtract(perp), cr, cg, cb, alpha * 0.82F);
            }
        }

        // 6 glowing rune pips around the perimeter
        float pipR = radius * 0.085F;
        for (int i = 0; i < 6; i++) {
            float a = -spinRad * 1.2F + i * (Mth.TWO_PI / 6.0F);
            Vec3 pc = pointOnPlane(center, uAxis, vAxis, Mth.cos(a) * radius * 0.76F, Mth.sin(a) * radius * 0.76F);
            Vec3 du = uAxis.scale(pipR);
            Vec3 dv = vAxis.scale(pipR);
            emitDoubleQuad(mat, vc, pc.subtract(du), pc.add(dv), pc.add(du), pc.subtract(dv), 1.0F, 1.0F, 1.0F, alpha * 0.92F);
        }
    }

    /**
     * Renders the translucent emissive 3D Ethereal Runic Warhammer (haft + collars + beveled warhammer head + core).
     * Local space: pivot at {@code (0, 0, 0)}, shaft extends along {@code +Z} to {@code +0.72F},
     * head strikes downward at {@code Y = -0.18F, Z = +0.72F}.
     */
    private static void renderEtherealHammer(
            Matrix4f mat, VertexConsumer vc,
            float pr, float pg, float pb,
            float ar, float ag, float ab,
            float alpha, boolean showSlamWake
    ) {
        // 1. Inner bright starlight core of the haft + outer translucent Materia shaft
        emitBox(mat, vc, -0.022F, -0.022F, -0.06F, 0.022F, 0.022F, 0.68F, 1.0F, 1.0F, 1.0F, alpha * 0.90F);
        emitBox(mat, vc, -0.042F, -0.042F, -0.08F, 0.042F, 0.042F, 0.66F, pr, pg, pb, alpha * 0.58F);

        // Pommel & 3 runic grip collars along the haft
        emitBox(mat, vc, -0.062F, -0.062F, -0.12F, 0.062F, 0.062F, -0.05F, ar, ag, ab, alpha * 0.82F);
        emitBox(mat, vc, -0.056F, -0.056F, 0.16F, 0.056F, 0.056F, 0.20F, ar, ag, ab, alpha * 0.78F);
        emitBox(mat, vc, -0.056F, -0.056F, 0.42F, 0.056F, 0.056F, 0.46F, ar, ag, ab, alpha * 0.78F);

        // 2. Massive Beveled Ethereal Warhammer Head centered at Z = 0.72F, Y = -0.18F..+0.18F
        // Radiant inner crystal heart
        emitBox(mat, vc, -0.09F, -0.13F, 0.63F, 0.09F, 0.13F, 0.81F, 1.0F, 1.0F, 1.0F, alpha * 0.92F);
        // Main translucent glowing hammer block
        emitBox(mat, vc, -0.15F, -0.16F, 0.57F, 0.15F, 0.16F, 0.87F, pr, pg, pb, alpha * 0.66F);
        // Lower striking face plate (contacts the Magic Circle at Y = -0.18F -> world Y = 1.04F!)
        emitBox(mat, vc, -0.17F, -0.185F, 0.55F, 0.17F, -0.145F, 0.89F, ar, ag, ab, alpha * 0.88F);
        // Upper crown plate & arcane wing crests
        emitBox(mat, vc, -0.165F, 0.145F, 0.56F, 0.165F, 0.185F, 0.88F, ar, ag, ab, alpha * 0.84F);
        emitBox(mat, vc, -0.19F, -0.06F, 0.66F, 0.19F, 0.06F, 0.78F, ar, ag, ab, alpha * 0.75F);

        // 3. Motion-blur arc ribbon trailing above the hammer head during the downward smash
        if (showSlamWake) {
            emitBox(mat, vc, -0.13F, 0.18F, 0.60F, 0.13F, 0.52F, 0.84F, pr, pg, pb, alpha * 0.34F);
            emitBox(mat, vc, -0.07F, 0.18F, 0.66F, 0.07F, 0.66F, 0.78F, 1.0F, 1.0F, 1.0F, alpha * 0.40F);
        }
    }

    private static void renderEnergyBeam(
            Matrix4f mat, VertexConsumer vc,
            Vec3 from, Vec3 to, float halfWidth,
            float r, float g, float b, float alpha
    ) {
        Vec3 diff = to.subtract(from);
        if (diff.lengthSqr() < 1.0E-6D) return;
        Vec3 u = new Vec3(0.0D, halfWidth, 0.0D);
        Vec3 v = new Vec3(0.0D, 0.0D, halfWidth);
        emitDoubleQuad(mat, vc, from.subtract(u), from.add(u), to.add(u), to.subtract(u), r, g, b, alpha);
        emitDoubleQuad(mat, vc, from.subtract(v), from.add(v), to.add(v), to.subtract(v), 1.0F, 1.0F, 1.0F, alpha * 0.85F);
    }

    private static void renderFlatRing(
            Matrix4f mat, VertexConsumer vc,
            float cx, float cy, float cz,
            float innerR, float outerR, int segments,
            float r, float g, float b, float alpha
    ) {
        renderOrientedRing(
                mat, vc,
                new Vec3(cx, cy, cz),
                new Vec3(1.0D, 0.0D, 0.0D),
                new Vec3(0.0D, 0.0D, 1.0D),
                innerR, outerR, segments, r, g, b, alpha
        );
    }

    private static void renderOrientedRing(
            Matrix4f mat, VertexConsumer vc,
            Vec3 center, Vec3 uAxis, Vec3 vAxis,
            float innerR, float outerR, int segments,
            float r, float g, float b, float alpha
    ) {
        if (alpha <= 0.005F || outerR <= 0.001F) return;
        for (int i = 0; i < segments; i++) {
            float a0 = (i / (float) segments) * Mth.TWO_PI;
            float a1 = ((i + 1) / (float) segments) * Mth.TWO_PI;
            float c0 = Mth.cos(a0), s0 = Mth.sin(a0);
            float c1 = Mth.cos(a1), s1 = Mth.sin(a1);

            Vec3 p0In = pointOnPlane(center, uAxis, vAxis, c0 * innerR, s0 * innerR);
            Vec3 p0Out = pointOnPlane(center, uAxis, vAxis, c0 * outerR, s0 * outerR);
            Vec3 p1Out = pointOnPlane(center, uAxis, vAxis, c1 * outerR, s1 * outerR);
            Vec3 p1In = pointOnPlane(center, uAxis, vAxis, c1 * innerR, s1 * innerR);

            emitDoubleQuad(mat, vc, p0In, p0Out, p1Out, p1In, r, g, b, alpha);
        }
    }

    private static Vec3 pointOnPlane(Vec3 center, Vec3 uAxis, Vec3 vAxis, float u, float v) {
        return new Vec3(
                center.x + uAxis.x * u + vAxis.x * v,
                center.y + uAxis.y * u + vAxis.y * v,
                center.z + uAxis.z * u + vAxis.z * v
        );
    }

    private static void emitBox(
            Matrix4f mat, VertexConsumer vc,
            float x0, float y0, float z0,
            float x1, float y1, float z1,
            float r, float g, float b, float a
    ) {
        if (a <= 0.005F) return;
        // +Y / -Y
        emitSingleQuad(mat, vc, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0, r, g, b, a);
        emitSingleQuad(mat, vc, x0, y0, z1, x0, y0, z0, x1, y0, z0, x1, y0, z1, r, g, b, a);
        // +Z / -Z
        emitSingleQuad(mat, vc, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, r, g, b, a);
        emitSingleQuad(mat, vc, x1, y0, z0, x0, y0, z0, x0, y1, z0, x1, y1, z0, r, g, b, a);
        // +X / -X
        emitSingleQuad(mat, vc, x1, y0, z1, x1, y0, z0, x1, y1, z0, x1, y1, z1, r, g, b, a);
        emitSingleQuad(mat, vc, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0, r, g, b, a);
    }

    private static void emitDoubleQuad(
            Matrix4f mat, VertexConsumer vc,
            Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3,
            float r, float g, float b, float a
    ) {
        if (a <= 0.005F) return;
        emitSingleQuad(mat, vc,
                (float) p0.x, (float) p0.y, (float) p0.z,
                (float) p1.x, (float) p1.y, (float) p1.z,
                (float) p2.x, (float) p2.y, (float) p2.z,
                (float) p3.x, (float) p3.y, (float) p3.z,
                r, g, b, a);
        emitSingleQuad(mat, vc,
                (float) p3.x, (float) p3.y, (float) p3.z,
                (float) p2.x, (float) p2.y, (float) p2.z,
                (float) p1.x, (float) p1.y, (float) p1.z,
                (float) p0.x, (float) p0.y, (float) p0.z,
                r, g, b, a);
    }

    private static void emitSingleQuad(
            Matrix4f mat, VertexConsumer vc,
            float x0, float y0, float z0,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            float r, float g, float b, float a
    ) {
        vc.addVertex(mat, x0, y0, z0).setColor(r, g, b, a).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0.0F, 1.0F, 0.0F);
        vc.addVertex(mat, x1, y1, z1).setColor(r, g, b, a).setUv(0.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0.0F, 1.0F, 0.0F);
        vc.addVertex(mat, x2, y2, z2).setColor(r, g, b, a).setUv(1.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0.0F, 1.0F, 0.0F);
        vc.addVertex(mat, x3, y3, z3).setColor(r, g, b, a).setUv(1.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0.0F, 1.0F, 0.0F);
    }

    // =========================================================================
    // AUTOCAST PEDESTAL RENDERER
    // =========================================================================

    private static final class PedestalRenderer
            implements BlockEntityRenderer<AutocastPedestalBlockEntity, PedestalRenderState> {

        @Override
        public PedestalRenderState createRenderState() {
            return new PedestalRenderState();
        }

        @Override
        public void extractRenderState(
                AutocastPedestalBlockEntity be,
                PedestalRenderState state,
                float partialTick,
                Vec3 cameraPos,
                @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
        ) {
            BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
            BlockState bs = be.getBlockState();
            if (bs.getBlock() instanceof AutocastPedestalBlock pedBlock) {
                state.variant = pedBlock.getVariant();
            } else {
                state.variant = AutocastPedestalBlock.PedestalVariant.STONE;
            }
            state.facing = bs.hasProperty(AutocastPedestalBlock.FACING)
                    ? bs.getValue(AutocastPedestalBlock.FACING)
                    : Direction.NORTH;
            state.hasOrbisPort = be.hasOrbisConnection();
            state.hasMountedGem = be.hasMountedSpellgem();
            state.gemTintRgb = be.getSpellTintRgb();
            state.packedLight = be.getLevel() != null
                    ? LevelRenderer.getLightColor(be.getLevel(), be.getBlockPos())
                    : FULL_LIGHT;
        }

        @Override
        public void submit(
                PedestalRenderState renderState,
                PoseStack poseStack,
                SubmitNodeCollector collector,
                CameraRenderState cameraRenderState
        ) {
            BlockbenchCustomMesh mesh = BlockbenchCustomMesh.getOrLoad("autocast_pedestal");
            ResourceLocation tex = ResourceLocation.fromNamespaceAndPath(
                    Entropica.MODID,
                    renderState.variant.getTexturePath()
            );

            // FACING is the turret firing direction (-Z NORTH in model space, Orbis port at +Z SOUTH)
            float rotY = switch (renderState.facing) {
                case WEST -> 90.0F;
                case SOUTH -> 180.0F;
                case EAST -> -90.0F;
                default -> 0.0F; // NORTH
            };

            final boolean showOrbis = renderState.hasOrbisPort;
            final boolean showGem = renderState.hasMountedGem;
            final int rawTint = renderState.gemTintRgb;
            int r = Math.min(255, (((rawTint >> 16) & 0xFF) * 3 + 255) / 4);
            int g = Math.min(255, (((rawTint >> 8) & 0xFF) * 3 + 255) / 4);
            int b = Math.min(255, ((rawTint & 0xFF) * 3 + 255) / 4);
            final int brightGemTint = (r << 16) | (g << 8) | b;

            Predicate<Set<String>> visFilter = groups -> {
                if (!showOrbis && groups.contains("Orbis_Connection_Port_Conditional")) return false;
                if (!showGem && groups.contains("Slotted_Spellgem")) return false;
                return true;
            };
            ToIntFunction<Set<String>> tintResolver = groups ->
                    groups.contains("Slotted_Spellgem") ? brightGemTint : 0xFFFFFF;

            poseStack.pushPose();
            poseStack.translate(0.5F, 0.0F, 0.5F);
            poseStack.mulPose(Axis.YP.rotationDegrees(rotY));
            poseStack.translate(-0.5F, 0.0F, -0.5F);

            final int light = renderState.packedLight;
            collector.submitCustomGeometry(
                    poseStack,
                    RenderType.entityCutoutNoCull(tex),
                    (pose, consumer) -> mesh.emit(pose, consumer, light, OverlayTexture.NO_OVERLAY, visFilter, tintResolver)
            );
            poseStack.popPose();
        }
    }
}
