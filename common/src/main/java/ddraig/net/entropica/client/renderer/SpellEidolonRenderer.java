package ddraig.net.entropica.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.entity.SpellEidolonEntity;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * Renders {@link SpellEidolonEntity} using three distinct 3D Triad base models (Section 4.9):
 * <ul>
 *     <li><b>{@link SpellEidolonEntity.TriadArchetype#AGENT}</b>: Mobile spectral warrior ({@link EidolicShadowModel})
 *         with twin elemental forearm blades, glowing core, and mirrored equipment.</li>
 *     <li><b>{@link SpellEidolonEntity.TriadArchetype#SENTRY}</b>: Ground-anchored rotating crystalline turret pylon
 *         with stepped octagonal plinth, levitating faceted obelisk crystal, and counter-rotating gyroscopic rings.</li>
 *     <li><b>{@link SpellEidolonEntity.TriadArchetype#DECOY}</b>: Shimmering elemental cyclone / spectral effigy
 *         with multi-tiered spinning vortex funnel bands and pulsing mirage core.</li>
 * </ul>
 */
public class SpellEidolonRenderer extends EntityRenderer<SpellEidolonEntity, SpellEidolonRenderer.EidolonRenderState> {

    private static final ResourceLocation EIDOLON_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    private static final int FULL_LIGHT = 15728880;
    private static final int NO_OVERLAY = OverlayTexture.NO_OVERLAY;

    private final EidolicShadowModel model;
    private final Font font;
    private final ItemModelResolver itemModelResolver;

    public SpellEidolonRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.font = context.getFont();
        this.itemModelResolver = context.getItemModelResolver();
        this.model = new EidolicShadowModel(context.bakeLayer(EidolicShadowModel.LAYER_LOCATION));
    }

    public static class EidolonRenderState extends EidolicShadowRenderer.ShadowRenderState {
        public SpellEidolonEntity.TriadArchetype archetype = SpellEidolonEntity.TriadArchetype.AGENT;
        public EssenceType materia = EssenceType.REGULAR;
        public boolean mutated = false;
        public int primaryRgb = 0x88CCFF;
        public int argbTint = 0xD888CCFF;
        public int runeRgb = 0xFF88CCFF;
        public final ItemStackRenderState offhandRenderState = new ItemStackRenderState();
        public boolean hasOffhandItem = false;
        public final ItemStackRenderState headEquipRenderState = new ItemStackRenderState();
        public boolean hasHeadEquip = false;
        public final ItemStackRenderState chestEquipRenderState = new ItemStackRenderState();
        public boolean hasChestEquip = false;
        public final ItemStackRenderState legsEquipRenderState = new ItemStackRenderState();
        public boolean hasLegsEquip = false;
        public final ItemStackRenderState feetEquipRenderState = new ItemStackRenderState();
        public boolean hasFeetEquip = false;
    }

    @Override
    public EidolonRenderState createRenderState() {
        return new EidolonRenderState();
    }

    @Override
    public void extractRenderState(SpellEidolonEntity entity, EidolonRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);

        state.archetype = entity.getArchetype();
        EssenceType mat = entity.getEffectiveMateria();
        state.materia = mat;
        state.mutated = entity.isMutated();

        ItemStack displayItem = entity.getDisplayItem();
        state.hasItem = !displayItem.isEmpty();
        if (state.hasItem) {
            this.itemModelResolver.updateForTopItem(
                    state.itemRenderState,
                    displayItem,
                    ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                    entity.level(),
                    entity,
                    0
            );
        }

        ItemStack offhandItem = entity.getItemBySlot(EquipmentSlot.OFFHAND);
        state.hasOffhandItem = !offhandItem.isEmpty();
        if (state.hasOffhandItem) {
            this.itemModelResolver.updateForTopItem(
                    state.offhandRenderState,
                    offhandItem,
                    ItemDisplayContext.THIRD_PERSON_LEFT_HAND,
                    entity.level(),
                    entity,
                    1
            );
        }

        ItemStack headItem = entity.getItemBySlot(EquipmentSlot.HEAD);
        state.hasHeadEquip = !headItem.isEmpty();
        if (state.hasHeadEquip) {
            this.itemModelResolver.updateForTopItem(
                    state.headEquipRenderState,
                    headItem,
                    ItemDisplayContext.HEAD,
                    entity.level(),
                    entity,
                    2
            );
        }

        ItemStack chestItem = entity.getItemBySlot(EquipmentSlot.CHEST);
        state.hasChestEquip = !chestItem.isEmpty();
        if (state.hasChestEquip) {
            this.itemModelResolver.updateForTopItem(
                    state.chestEquipRenderState,
                    chestItem,
                    ItemDisplayContext.FIXED,
                    entity.level(),
                    entity,
                    3
            );
        }

        ItemStack legsItem = entity.getItemBySlot(EquipmentSlot.LEGS);
        state.hasLegsEquip = !legsItem.isEmpty();
        if (state.hasLegsEquip) {
            this.itemModelResolver.updateForTopItem(
                    state.legsEquipRenderState,
                    legsItem,
                    ItemDisplayContext.FIXED,
                    entity.level(),
                    entity,
                    4
            );
        }

        ItemStack feetItem = entity.getItemBySlot(EquipmentSlot.FEET);
        state.hasFeetEquip = !feetItem.isEmpty();
        if (state.hasFeetEquip) {
            this.itemModelResolver.updateForTopItem(
                    state.feetEquipRenderState,
                    feetItem,
                    ItemDisplayContext.FIXED,
                    entity.level(),
                    entity,
                    5
            );
        }

        int rgb;
        if (mat == EssenceType.ENTROPICA) {
            rgb = ddraig.net.entropica.spell.vfx.SpellParticleHelper.resolveVibrantPrimaryRgb(EssenceType.ENTROPICA, entity.level().getGameTime() + partialTick);
        } else if (mat != null && mat != EssenceType.REGULAR && mat.isDynamic()) {
            int[] cycleRgb = mat.getCurrentRGB(entity.level().getGameTime() + partialTick);
            rgb = ((cycleRgb[0] & 0xFF) << 16) | ((cycleRgb[1] & 0xFF) << 8) | (cycleRgb[2] & 0xFF);
        } else {
            rgb = entity.getTintRgb() & 0xFFFFFF;
        }
        state.primaryRgb = rgb;
        state.argbTint = (0xDC << 24) | rgb;
        state.runeRgb = 0xFF000000 | Math.min(0xFFFFFF, rgb | 0x383838);

        String glyph = state.archetype.getRuneGlyph();
        state.runes[0] = glyph;
        state.runes[1] = "";
        state.runes[2] = "";
        state.runes[3] = "";

        state.yRot = entity.getViewYRot(partialTick);
        state.walkDist = Mth.lerp(partialTick, entity.prevWalkDistAnim, entity.walkDistAnim);
        state.ageInTicks = entity.tickCount + partialTick;
    }

    @Override
    public void submit(EidolonRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        float pr = ((state.primaryRgb >> 16) & 0xFF) / 255.0F;
        float pg = ((state.primaryRgb >> 8) & 0xFF) / 255.0F;
        float pb = (state.primaryRgb & 0xFF) / 255.0F;
        float ar = Math.min(1.0F, pr * 0.55F + 0.45F);
        float ag = Math.min(1.0F, pg * 0.55F + 0.45F);
        float ab = Math.min(1.0F, pb * 0.55F + 0.45F);

        if (state.archetype == SpellEidolonEntity.TriadArchetype.SENTRY) {
            submitAnchoredSentryPylon(state, poseStack, collector, pr, pg, pb, ar, ag, ab);
            super.submit(state, poseStack, collector, cameraState);
            return;
        }

        if (state.archetype == SpellEidolonEntity.TriadArchetype.DECOY) {
            submitPhantomDecoyEffigy(state, poseStack, collector, pr, pg, pb, ar, ag, ab);
            super.submit(state, poseStack, collector, cameraState);
            return;
        }

        // AUTONOMOUS AGENT: Mobile Biped Construct + Orbiting Elemental Accents & Mirrored Equipment
        poseStack.pushPose();

        // Ground aura ring beneath Agent feet + majestic Air Zephyr Wing-Pinions & ankle cyclone!
        collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(EIDOLON_TEXTURE), (pose, vc) -> {
            Matrix4f m = pose.pose();
            float spin = state.ageInTicks * 0.08F;
            Vec3 ux = new Vec3(Mth.cos(spin), 0.0D, Mth.sin(spin));
            Vec3 uz = new Vec3(-Mth.sin(spin), 0.0D, Mth.cos(spin));
            renderLocalRing(m, vc, new Vec3(0.0D, 0.05D, 0.0D), ux, uz, 0.36F, 0.48F, 20, pr, pg, pb, 0.72F);
            if (state.mutated) {
                // Orbiting elemental blade crescents around the Agent's waist/shoulders
                int bladeCount = state.materia == EssenceType.AIR ? 4 : 2;
                for (int b = 0; b < bladeCount; b++) {
                    float a = spin * 2.4F + (float) (Math.PI * 2.0D * b / bladeCount);
                    Vec3 bladeCenter = new Vec3(Mth.cos(a) * 0.58D, 1.05D + 0.12D * Mth.sin(spin * 3.0F + b), Mth.sin(a) * 0.58D);
                    renderOctahedron(m, vc, bladeCenter, 0.10F, 0.28F, a, ar, ag, ab, 0.88F);
                }
            }
            if (state.materia == EssenceType.AIR) {
                renderZephyrAgentOrnaments(m, vc, state.ageInTicks, state.yRot, pr, pg, pb, ar, ag, ab);
            } else if (state.materia == EssenceType.WATER) {
                renderAqueousAgentOrnaments(m, vc, state.ageInTicks, state.yRot, pr, pg, pb, ar, ag, ab);
            } else if (state.materia == EssenceType.NATURE) {
                renderSylvanAgentOrnaments(m, vc, state.ageInTicks, state.yRot, pr, pg, pb, ar, ag, ab);
            } else if (state.materia == EssenceType.EARTH) {
                renderTectonicAgentOrnaments(m, vc, state.ageInTicks, state.yRot, pr, pg, pb, ar, ag, ab);
            } else if (state.materia == EssenceType.FROZEN) {
                renderGlacialAgentOrnaments(m, vc, state.ageInTicks, state.yRot, pr, pg, pb, ar, ag, ab);
            } else if (state.materia == EssenceType.NETHER) {
                renderInfernalAgentOrnaments(m, vc, state.ageInTicks, state.yRot, pr, pg, pb, ar, ag, ab);
            } else if (state.materia == EssenceType.RADIANT) {
                renderSolarSeraphAgentOrnaments(m, vc, state.ageInTicks, state.yRot, pr, pg, pb, ar, ag, ab);
            } else if (state.materia == EssenceType.UMBRAL) {
                renderShadowStalkerAgentOrnaments(m, vc, state.ageInTicks, state.yRot);
            } else if (state.materia == EssenceType.UNDEAD) {
                renderCryptguardAgentOrnaments(m, vc, state.ageInTicks, state.yRot);
            } else if (state.materia == EssenceType.VOID) {
                renderVoidStalkerAgentOrnaments(m, vc, state.ageInTicks, state.yRot);
            } else if (state.materia == EssenceType.ARID) {
                renderDuneDervishAgentOrnaments(m, vc, state.ageInTicks, state.yRot);
            } else if (state.materia == EssenceType.LIGHTNING) {
                renderVoltSparkAgentOrnaments(m, vc, state.ageInTicks, state.yRot);
            } else if (state.materia == EssenceType.MAGMA) {
                renderMagmaBruteAgentOrnaments(m, vc, state.ageInTicks, state.yRot);
            } else if (state.materia == EssenceType.STORM) {
                renderTempestZephyrAgentOrnaments(m, vc, state.ageInTicks, state.yRot);
            } else if (state.materia == EssenceType.GLACIAL) {
                renderGlacialBruiserAgentOrnaments(m, vc, state.ageInTicks, state.yRot);
            } else if (state.materia == EssenceType.OVERGROWTH) {
                renderSylvanGoliathAgentOrnaments(m, vc, state.ageInTicks, state.yRot);
            } else if (state.materia == EssenceType.DUST) {
                renderDustStalkerAgentOrnaments(m, vc, state.ageInTicks, state.yRot);
            } else if (state.materia == EssenceType.SPORE) {
                renderSporeTenderAgentOrnaments(m, vc, state.ageInTicks, state.yRot);
            } else if (state.materia == EssenceType.TAIGA) {
                renderBorealStalkerAgentOrnaments(m, vc, state.ageInTicks, state.yRot);
            } else if (state.materia == EssenceType.OASIS) {
                renderOasisWardenAgentOrnaments(m, vc, state.ageInTicks, state.yRot);
            } else if (state.materia == EssenceType.VITAE) {
                renderVitalityKnightAgentOrnaments(m, vc, state.ageInTicks, state.yRot);
            } else if (state.materia == EssenceType.BLOOD) {
                renderBloodThrallKnightAgentOrnaments(m, vc, state.ageInTicks, state.yRot);
            } else if (state.materia == EssenceType.AURA) {
                renderZephyrMonkAgentOrnaments(m, vc, state.ageInTicks, state.yRot);
            } else if (state.materia == EssenceType.AMBER) {
                renderAmberSentinelAgentOrnaments(m, vc, state.ageInTicks, state.yRot);
            } else if (state.materia == EssenceType.ECLIPSE) {
                renderEclipseKnightAgentOrnaments(m, vc, state.ageInTicks, state.yRot);
            } else if (state.materia == EssenceType.BLIGHT) {
                renderBlightRevenantAgentOrnaments(m, vc, state.ageInTicks, state.yRot);
            } else if (state.materia == EssenceType.ASTRAL) {
                renderAstralPhaseStalkerAgentOrnaments(m, vc, state.ageInTicks, state.yRot);
            } else if (state.materia == EssenceType.SOULFIRE) {
                renderSoulHarvesterAgentOrnaments(m, vc, state.ageInTicks, state.yRot);
            } else if (state.materia == EssenceType.VAPOR) {
                renderBoilerKnightAgentOrnaments(m, vc, state.ageInTicks, state.yRot);
            } else if (state.materia == EssenceType.NULL_R) {
                renderWhiteHolePaladinAgentOrnaments(m, vc, state.ageInTicks, state.yRot);
            } else if (state.materia == EssenceType.NULL_U) {
                renderVoidShadowStalkerAgentOrnaments(m, vc, state.ageInTicks, state.yRot);
            } else if (state.materia == EssenceType.DAWN) {
                renderDawnStriderAgentOrnaments(m, vc, state.ageInTicks, state.yRot);
            } else if (state.materia == EssenceType.ABYSS) {
                renderTentacledKrakenHuskAgentOrnaments(m, vc, state.ageInTicks, state.yRot);
            } else if (state.materia == EssenceType.AEGIS) {
                renderBulwarkPaladinAgentOrnaments(m, vc, state.ageInTicks, state.yRot);
            } else if (state.materia == EssenceType.AURORA) {
                renderBorealisArchonAgentOrnaments(m, vc, state.ageInTicks, state.yRot);
            } else if (state.materia == EssenceType.MIRAGE) {
                renderMirageDuelistAgentOrnaments(m, vc, state.ageInTicks, state.yRot);
            } else if (state.materia == EssenceType.WRAITH) {
                renderWraithStalkerAgentOrnaments(m, vc, state.ageInTicks, state.yRot);
            } else if (state.materia == EssenceType.BARROW) {
                renderBarrowSkeletonAgentOrnaments(m, vc, state.ageInTicks, state.yRot);
            } else if (state.materia == EssenceType.PYRE) {
                renderPyreJuggernautAgentOrnaments(m, vc, state.ageInTicks, state.yRot);
            } else if (state.materia == EssenceType.PENUMBRA) {
                renderPenumbraBladewardenAgentOrnaments(m, vc, state.ageInTicks, state.yRot);
            } else if (state.materia == EssenceType.RIME) {
                renderGlacialRimeVanguardAgentOrnaments(m, vc, state.ageInTicks, state.yRot);
            } else if (state.materia == EssenceType.SPRING) {
                renderVernalBloomBladewardenAgentOrnaments(m, vc, state.ageInTicks, state.yRot);
            } else if (state.materia == EssenceType.STATIC) {
                renderGalvanicArcVanguardAgentOrnaments(m, vc, state.ageInTicks, state.yRot);
            } else if (state.materia == EssenceType.SYLVAN) {
                renderIronwoodJuggernautAgentOrnaments(m, vc, state.ageInTicks, state.yRot);
            } else if (state.materia == EssenceType.MIASMA) {
                renderSepticBogStalkerAgentOrnaments(m, vc, state.ageInTicks, state.yRot);
            } else if (state.materia == EssenceType.GENESIS) {
                renderSolarHydroTemplarAgentOrnaments(m, vc, state.ageInTicks, state.yRot);
            } else if (state.materia == EssenceType.OBLIVION) {
                renderStygianDreadJuggernautAgentOrnaments(m, vc, state.ageInTicks, state.yRot);
            } else if (state.materia == EssenceType.AETHER) {
                renderFirmamentArchonColossusAgentOrnaments(m, vc, state.ageInTicks, state.yRot);
            } else if (state.materia == EssenceType.ENTROPIC) {
                renderEntropicDreadTitanAgentOrnaments(m, vc, state.ageInTicks, state.yRot);
            } else if (state.materia == EssenceType.CELESTIAL) {
                renderCelestialSeraphColossusAgentOrnaments(m, vc, state.ageInTicks, state.yRot);
            } else if (state.materia == EssenceType.PRISMATIC) {
                renderPrismaticValkyrieAgentOrnaments(m, vc, state.ageInTicks, state.yRot);
            } else if (state.materia == EssenceType.EMPYREAN) {
                renderYggdrasilArborealJuggernautAgentOrnaments(m, vc, state.ageInTicks, state.yRot);
            } else if (state.materia == EssenceType.CATACLYSM) {
                renderStygianMagmaVoidSerpentAgentOrnaments(m, vc, state.ageInTicks, state.yRot);
            } else if (state.materia == EssenceType.TERMINUS) {
                renderTitanicGlacialElementalAgentOrnaments(m, vc, state.ageInTicks, state.yRot);
            } else if (state.materia == EssenceType.ESCHATON) {
                renderEventHorizonLeviathanAgentOrnaments(m, vc, state.ageInTicks, state.yRot);
            } else if (state.materia == EssenceType.APOTHEOSIS) {
                renderCelestialSeraphTitanAgentOrnaments(m, vc, state.ageInTicks, state.yRot);
            } else if (state.materia == EssenceType.ENTROPICA) {
                renderAvatarOfEntropicaAgentOrnaments(m, vc, state.ageInTicks, state.yRot);
            }
        });

        poseStack.mulPose(Axis.YP.rotationDegrees(180f - state.yRot));
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -1.5F, 0.0F);

        this.model.setupAnim(state);
        final int tint = state.argbTint;
        collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(EIDOLON_TEXTURE), (pose, vc) -> {
            PoseStack localStack = new PoseStack();
            localStack.last().pose().set(pose.pose());
            localStack.last().normal().set(pose.normal());
            this.model.renderToBuffer(localStack, vc, FULL_LIGHT, NO_OVERLAY, tint);
        });

        if (state.hasHeadEquip) {
            poseStack.pushPose();
            this.model.body.translateAndRotate(poseStack);
            this.model.head.translateAndRotate(poseStack);
            poseStack.translate(0.0f, -0.15f, -0.12f);
            poseStack.scale(0.65f, -0.65f, -0.65f);
            state.headEquipRenderState.submit(poseStack, collector, FULL_LIGHT, NO_OVERLAY, 0);
            poseStack.popPose();
        }

        if (state.hasChestEquip) {
            poseStack.pushPose();
            this.model.body.translateAndRotate(poseStack);
            poseStack.translate(0.0f, -0.15f, -0.40f);
            poseStack.scale(0.65f, -0.65f, -0.65f);
            state.chestEquipRenderState.submit(poseStack, collector, FULL_LIGHT, NO_OVERLAY, 0);
            poseStack.popPose();
        }
        if (state.hasLegsEquip) {
            poseStack.pushPose();
            this.model.body.translateAndRotate(poseStack);
            poseStack.translate(0.0f, 0.42f, -0.26f);
            poseStack.scale(0.62f, -0.62f, -0.62f);
            state.legsEquipRenderState.submit(poseStack, collector, FULL_LIGHT, NO_OVERLAY, 0);
            poseStack.popPose();
        }
        if (state.hasFeetEquip) {
            poseStack.pushPose();
            this.model.rightLeg.translateAndRotate(poseStack);
            poseStack.translate(-0.08f, 0.62f, -0.18f);
            poseStack.scale(0.55f, -0.55f, -0.55f);
            state.feetEquipRenderState.submit(poseStack, collector, FULL_LIGHT, NO_OVERLAY, 0);
            poseStack.popPose();
        }

        if (state.hasItem) {
            poseStack.pushPose();
            this.model.body.translateAndRotate(poseStack);
            this.model.rightArm.translateAndRotate(poseStack);
            poseStack.translate(-0.1f, 0.8f, -0.2f);
            poseStack.mulPose(Axis.XP.rotationDegrees(90f));
            poseStack.scale(0.8f, 0.8f, 0.8f);
            state.itemRenderState.submit(poseStack, collector, FULL_LIGHT, NO_OVERLAY, 0);
            poseStack.popPose();
        }

        if (state.hasOffhandItem) {
            poseStack.pushPose();
            this.model.body.translateAndRotate(poseStack);
            this.model.leftArm.translateAndRotate(poseStack);
            poseStack.translate(0.1f, 0.8f, -0.2f);
            poseStack.mulPose(Axis.XP.rotationDegrees(90f));
            poseStack.scale(0.8f, 0.8f, 0.8f);
            state.offhandRenderState.submit(poseStack, collector, FULL_LIGHT, NO_OVERLAY, 0);
            poseStack.popPose();
        }

        poseStack.popPose();
        super.submit(state, poseStack, collector, cameraState);
    }

    /**
     * Triad Archetype 2 — ANCHORED SENTRY: Ground-anchored rotating crystalline turret pylon.
     */
    private static void submitAnchoredSentryPylon(
            EidolonRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            float pr, float pg, float pb,
            float ar, float ag, float ab
    ) {
        poseStack.pushPose();
        collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(EIDOLON_TEXTURE), (pose, vc) -> {
            Matrix4f m = pose.pose();
            float age = state.ageInTicks;
            float bob = 0.07F * Mth.sin(age * 0.10F);
            float spin = age * 0.09F;

            // 1. Ground-anchored octagonal plinth + outer runic ward circle
            Vec3 ux = new Vec3(Mth.cos(spin * 0.4F), 0.0D, Mth.sin(spin * 0.4F));
            Vec3 uz = new Vec3(-Mth.sin(spin * 0.4F), 0.0D, Mth.cos(spin * 0.4F));
            renderLocalRing(m, vc, new Vec3(0.0D, 0.03D, 0.0D), ux, uz, 0.58F, 0.72F, 24, pr, pg, pb, 0.82F);
            renderCylinderFrustum(m, vc, 0.02F, 0.28F, 0.52F, 0.36F, 8, 0.0F, pr * 0.65F, pg * 0.65F, pb * 0.65F, 0.92F);
            renderCylinderFrustum(m, vc, 0.28F, 0.44F, 0.36F, 0.24F, 8, (float) (Math.PI / 8.0D), ar, ag, ab, 0.85F);

            // 2. Levitating faceted obelisk crystal turret core + inner white-hot heart
            Vec3 coreCenter = new Vec3(0.0D, 1.18D + bob, 0.0D);
            renderOctahedron(m, vc, coreCenter, 0.16F, 0.48F, spin, 1.0F, 1.0F, 1.0F, 0.96F);
            renderOctahedron(m, vc, coreCenter, 0.28F, 0.68F, -spin * 0.6F, pr, pg, pb, 0.78F);

            // 3. Counter-rotating tilted gyroscopic crystal rings
            Vec3 r1x = new Vec3(Mth.cos(spin), 0.32D, Mth.sin(spin)).normalize();
            Vec3 r1z = new Vec3(-Mth.sin(spin), 0.32D, Mth.cos(spin)).normalize();
            renderLocalRing(m, vc, coreCenter, r1x, r1z, 0.44F, 0.53F, 20, ar, ag, ab, 0.88F);

            Vec3 r2x = new Vec3(Mth.cos(-spin * 1.3F), -0.36D, Mth.sin(-spin * 1.3F)).normalize();
            Vec3 r2z = new Vec3(-Mth.sin(-spin * 1.3F), -0.36D, Mth.cos(-spin * 1.3F)).normalize();
            renderLocalRing(m, vc, coreCenter, r2x, r2z, 0.38F, 0.45F, 20, pr, pg, pb, 0.80F);

            // 4. Forward-aiming focusing lens aligned with turret yaw
            float yawRad = (180.0F - state.yRot) * Mth.DEG_TO_RAD;
            Vec3 aimFwd = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
            Vec3 aimRight = new Vec3(aimFwd.z, 0.0D, -aimFwd.x);
            Vec3 aimUp = new Vec3(0.0D, 1.0D, 0.0D);
            renderLocalRing(m, vc, coreCenter.add(aimFwd.scale(0.46D)), aimRight, aimUp, 0.10F, 0.18F, 16, 1.0F, 1.0F, 1.0F, 0.92F);

            // 5. Air (#1) Zephyr Wind-Chime Turret: 6 swaying resonant crystal wind-chimes & rising updraft spirals!
            if (state.materia == EssenceType.AIR) {
                for (int c = 0; c < 6; c++) {
                    float cAng = spin * 0.65F + (float) (Math.PI * 2.0D * c / 6.0D);
                    float chimeLen = (c % 2 == 0) ? 0.44F : 0.34F;
                    double sway = 0.055D * Mth.sin(age * 0.18F + c * 1.1F);
                    Vec3 topAnchor = coreCenter.add(Mth.cos(cAng) * 0.48D, -0.04D, Mth.sin(cAng) * 0.48D);
                    Vec3 chimeMid = topAnchor.add(sway, -chimeLen * 0.55D, -sway);
                    renderOctahedron(m, vc, chimeMid, 0.045F, chimeLen * 0.52F, cAng,
                            (c % 2 == 0) ? ar : pr, (c % 2 == 0) ? ag : pg, (c % 2 == 0) ? ab : pb, 0.90F);
                }
                for (int s = 0; s < 3; s++) {
                    float phase = age * 0.24F + (float) (Math.PI * 2.0D * s / 3.0D);
                    for (int step = 0; step < 10; step++) {
                        float t0 = step / 10.0F;
                        float t1 = (step + 1) / 10.0F;
                        float r0 = 0.34F + 0.38F * t0;
                        float r1 = 0.34F + 0.38F * t1;
                        float a0 = phase + t0 * 5.2F;
                        float a1 = phase + t1 * 5.2F;
                        Vec3 p0 = new Vec3(Mth.cos(a0) * r0, 0.08D + t0 * 1.75D, Mth.sin(a0) * r0);
                        Vec3 p1 = new Vec3(Mth.cos(a1) * r1, 0.08D + t1 * 1.75D, Mth.sin(a1) * r1);
                        Vec3 perp = new Vec3(-Mth.sin(a0) * 0.032D, 0.025D, Mth.cos(a0) * 0.032D);
                        emitDoubleSidedQuad(m, vc, p0.subtract(perp), p0.add(perp), p1.add(perp), p1.subtract(perp),
                                ar, ag, ab, 0.72F * (1.0F - t0 * 0.55F));
                    }
                }
            } else if (state.materia == EssenceType.WATER) {
                // Section 6.3 Aqueous Sentry (#225C7C): foaming fountain basin, 4 orbiting pressurized water orbs & hydro-streams!
                renderLocalRing(m, vc, new Vec3(0.0D, 0.06D, 0.0D), ux, uz, 0.72F, 0.86F, 24, 0.88F, 0.98F, 1.00F, 0.78F);
                for (int o = 0; o < 4; o++) {
                    float oAng = -spin * 1.35F + (float) (Math.PI * 0.5D * o);
                    double oy = coreCenter.y + 0.14D * Mth.sin(age * 0.18F + o * 1.57F);
                    Vec3 orbPos = new Vec3(Mth.cos(oAng) * 0.62D, oy, Mth.sin(oAng) * 0.62D);
                    renderOctahedron(m, vc, orbPos, 0.085F, 0.095F, age * 0.25F, 0.90F, 0.99F, 1.00F, 0.94F);
                    renderOctahedron(m, vc, orbPos, 0.135F, 0.145F, -age * 0.18F, 0.133F, 0.361F, 0.486F, 0.74F);
                }
                for (int s = 0; s < 3; s++) {
                    float phase = -age * 0.20F + (float) (Math.PI * 2.0D * s / 3.0D);
                    for (int step = 0; step < 10; step++) {
                        float t0 = step / 10.0F;
                        float t1 = (step + 1) / 10.0F;
                        float r0 = 0.62F * (1.0F - 0.42F * t0) + 0.05F * Mth.sin(t0 * 6.28F + age * 0.2F);
                        float r1 = 0.62F * (1.0F - 0.42F * t1) + 0.05F * Mth.sin(t1 * 6.28F + age * 0.2F);
                        float a0 = phase + t0 * 4.4F;
                        float a1 = phase + t1 * 4.4F;
                        Vec3 p0 = new Vec3(Mth.cos(a0) * r0, 0.08D + t0 * 1.45D, Mth.sin(a0) * r0);
                        Vec3 p1 = new Vec3(Mth.cos(a1) * r1, 0.08D + t1 * 1.45D, Mth.sin(a1) * r1);
                        Vec3 perp = new Vec3(-Mth.sin(a0) * 0.042D, 0.028D, Mth.cos(a0) * 0.042D);
                        boolean foam = (step >= 7);
                        emitDoubleSidedQuad(m, vc, p0.subtract(perp), p0.add(perp), p1.add(perp), p1.subtract(perp),
                                foam ? 0.88F : 0.141F, foam ? 0.98F : 0.714F, 1.00F, 0.76F * (1.0F - t0 * 0.30F));
                    }
                }
            } else if (state.materia == EssenceType.NATURE) {
                // Section 6.4 Sylvan Thorn Sentry (#1A5923): 6 gnarled 3D wooden anchor roots, blooming turret flower & orbiting Thorn Darts!
                for (int rIdx = 0; rIdx < 6; rIdx++) {
                    float rootAng = (float) (Math.PI * 2.0D * rIdx / 6.0D);
                    Vec3 rDir = new Vec3(Mth.cos(rootAng), 0.0D, Mth.sin(rootAng));
                    Vec3 tang = new Vec3(-Mth.sin(rootAng), 0.0D, Mth.cos(rootAng));
                    Vec3 prevRoot = rDir.scale(0.26D).add(0.0D, 0.42D, 0.0D);
                    for (int s = 1; s <= 6; s++) {
                        float u = s / 6.0F;
                        double dist = 0.26D + u * 0.78D;
                        double y = 0.42D * (1.0D - u) * (1.0D - u * 0.35D) + 0.04D * Mth.sin(u * (float) Math.PI);
                        double twist = 0.08D * Mth.sin(u * 4.5F + rIdx);
                        Vec3 curRoot = rDir.scale(dist).add(tang.scale(twist)).add(0.0D, y, 0.0D);
                        Vec3 perp = tang.scale(0.065D * (1.0F - 0.55F * u)).add(0.0D, 0.045D * (1.0F - 0.55F * u), 0.0D);
                        emitDoubleSidedQuad(m, vc, prevRoot.subtract(perp), prevRoot.add(perp), curRoot.add(perp), curRoot.subtract(perp),
                                0.36F, 0.23F, 0.12F, 0.95F);
                        prevRoot = curRoot;
                    }
                }

                // 6-petal woodland turret blossom crowning the core
                for (int p = 0; p < 6; p++) {
                    float pAng = spin * 0.85F + (float) (Math.PI * 2.0D * p / 6.0D);
                    Vec3 pDir = new Vec3(Mth.cos(pAng), 0.18D, Mth.sin(pAng)).normalize();
                    Vec3 pTang = new Vec3(-Mth.sin(pAng), 0.0D, Mth.cos(pAng));
                    Vec3 pBase = coreCenter.add(pDir.scale(0.16D));
                    Vec3 pLeft = coreCenter.add(pDir.scale(0.36D)).add(pTang.scale(0.13D));
                    Vec3 pTip = coreCenter.add(pDir.scale(0.54D));
                    Vec3 pRight = coreCenter.add(pDir.scale(0.36D)).subtract(pTang.scale(0.13D));
                    emitDoubleSidedQuad(m, vc, pBase, pLeft, pTip, pRight, 1.00F, 0.58F, 0.76F, 0.86F);
                }

                // 4 orbiting razor Thorn Darts poised around the turret
                for (int d = 0; d < 4; d++) {
                    float dAng = -spin * 1.55F + (float) (Math.PI * 0.5D * d);
                    double dy = coreCenter.y + 0.12D * Mth.sin(age * 0.20F + d * 1.57F);
                    Vec3 dartPos = new Vec3(Mth.cos(dAng) * 0.64D, dy, Mth.sin(dAng) * 0.64D);
                    renderOctahedron(m, vc, dartPos, 0.055F, 0.22F, dAng, 0.71F, 1.00F, 0.47F, 0.95F);
                    renderOctahedron(m, vc, dartPos, 0.085F, 0.16F, -dAng, 0.102F, 0.349F, 0.137F, 0.84F);
                }
            } else if (state.materia == EssenceType.EARTH) {
                // Section 6.5 Tectonic Plinth Sentry (#773214): 3-tier carved stone bastion plinth, 4 granite buttresses & 4 stone spire bolts!
                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), ux, uz, 0.72F, 0.88F, 8, 1.00F, 0.76F, 0.47F, 0.88F);
                renderCylinderFrustum(m, vc, 0.02F, 0.24F, 0.66F, 0.54F, 8, 0.0F, 0.353F, 0.322F, 0.298F, 0.96F);
                renderCylinderFrustum(m, vc, 0.24F, 0.48F, 0.52F, 0.40F, 8, (float) (Math.PI / 8.0D), 0.467F, 0.196F, 0.078F, 0.95F);
                renderCylinderFrustum(m, vc, 0.48F, 0.66F, 0.38F, 0.28F, 8, 0.0F, 0.353F, 0.322F, 0.298F, 0.94F);

                // 4 angled carved granite corner buttress pillars anchoring the turret into the bedrock
                for (int b = 0; b < 4; b++) {
                    float bAng = (float) (Math.PI * 0.5D * b + Math.PI * 0.25D);
                    Vec3 bRad = new Vec3(Mth.cos(bAng), 0.0D, Mth.sin(bAng));
                    Vec3 bTang = new Vec3(-Mth.sin(bAng), 0.0D, Mth.cos(bAng));
                    Vec3 topIn = bRad.scale(0.32D).add(0.0D, 0.64D, 0.0D);
                    Vec3 botOut = bRad.scale(0.84D).add(0.0D, 0.03D, 0.0D);
                    Vec3 w = bTang.scale(0.085D);
                    emitDoubleSidedQuad(m, vc, topIn.subtract(w), topIn.add(w), botOut.add(w.scale(1.3D)), botOut.subtract(w.scale(1.3D)),
                            0.353F, 0.322F, 0.298F, 0.96F);
                    emitDoubleSidedQuad(m, vc, topIn.add(0.0D, 0.02D, 0.0D), botOut.add(0.0D, 0.02D, 0.0D),
                            botOut.add(bRad.scale(0.04D)), topIn.add(bRad.scale(0.04D)),
                            1.00F, 0.76F, 0.47F, 0.92F);
                }

                // 4 orbiting chiseled Quake Spire stone bolts poised around the turret core
                for (int s = 0; s < 4; s++) {
                    float sAng = -spin * 1.25F + (float) (Math.PI * 0.5D * s);
                    double sy = coreCenter.y + 0.10D * Mth.sin(age * 0.16F + s * 1.57F);
                    Vec3 boltPos = new Vec3(Mth.cos(sAng) * 0.66D, sy, Mth.sin(sAng) * 0.66D);
                    renderOctahedron(m, vc, boltPos, 0.095F, 0.26F, sAng, 0.467F, 0.196F, 0.078F, 0.95F);
                    renderOctahedron(m, vc, boltPos, 0.055F, 0.30F, -sAng, 1.00F, 0.76F, 0.47F, 0.96F);
                }
            } else if (state.materia == EssenceType.FROZEN) {
                // Section 6.6 Glacial Icicle Sentry (#98DCF2): 6-pointed snowflake seal, anchored frost-crystal plinth & 6 rapid icicle darts!
                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), ux, uz, 0.74F, 0.92F, 6, 0.92F, 0.98F, 1.00F, 0.90F);
                for (int arm = 0; arm < 6; arm++) {
                    float fAng = spin * 0.35F + (float) (Math.PI * 2.0D * arm / 6.0D);
                    Vec3 aDir = new Vec3(Mth.cos(fAng), 0.0D, Mth.sin(fAng));
                    Vec3 aTang = new Vec3(-Mth.sin(fAng), 0.0D, Mth.cos(fAng));
                    Vec3 armBase = aDir.scale(0.18D).add(0.0D, 0.048D, 0.0D);
                    Vec3 armTip = aDir.scale(0.96D).add(0.0D, 0.048D, 0.0D);
                    Vec3 aw = aTang.scale(0.045D);
                    emitDoubleSidedQuad(m, vc, armBase.subtract(aw), armBase.add(aw), armTip.add(aw.scale(0.35D)), armTip.subtract(aw.scale(0.35D)),
                            0.60F, 0.86F, 0.95F, 0.88F);
                }
                renderCylinderFrustum(m, vc, 0.02F, 0.28F, 0.62F, 0.46F, 6, 0.0F, 0.17F, 0.51F, 0.79F, 0.86F);
                renderCylinderFrustum(m, vc, 0.28F, 0.62F, 0.44F, 0.28F, 6, (float) (Math.PI / 6.0D), 0.60F, 0.86F, 0.95F, 0.82F);

                // 4 angled crystalline blue-ice buttress shards anchoring the frost turret
                for (int b = 0; b < 4; b++) {
                    float bAng = (float) (Math.PI * 0.5D * b + Math.PI * 0.25D);
                    Vec3 shardPos = new Vec3(Mth.cos(bAng) * 0.62D, 0.34D, Mth.sin(bAng) * 0.62D);
                    renderOctahedron(m, vc, shardPos, 0.12F, 0.34F, bAng, 0.17F, 0.51F, 0.79F, 0.88F);
                    renderOctahedron(m, vc, shardPos, 0.065F, 0.38F, -bAng, 0.92F, 0.98F, 1.00F, 0.94F);
                }

                // 6 orbiting razor icicle darts spinning rapidly around the anchored frost crystal
                for (int d = 0; d < 6; d++) {
                    float dAng = -spin * 1.85F + (float) (Math.PI * 2.0D * d / 6.0D);
                    double dy = coreCenter.y + 0.12D * Mth.sin(age * 0.22F + d * 1.047F);
                    Vec3 dartPos = new Vec3(Mth.cos(dAng) * 0.68D, dy, Mth.sin(dAng) * 0.68D);
                    renderOctahedron(m, vc, dartPos, 0.065F, 0.28F, dAng, 0.60F, 0.86F, 0.95F, 0.92F);
                    renderOctahedron(m, vc, dartPos, 0.032F, 0.32F, -dAng, 0.94F, 0.99F, 1.00F, 0.98F);
                }
            } else if (state.materia == EssenceType.NETHER) {
                // Section 6.7 Infernal Magma Mortar (#B83C08): anchored basalt-slag mortar cauldron, glowing lava basin & 4 orbiting explosive fireballs!
                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), ux, uz, 0.72F, 0.92F, 8, 1.00F, 0.82F, 0.36F, 0.92F);
                renderCylinderFrustum(m, vc, 0.02F, 0.26F, 0.70F, 0.56F, 8, 0.0F, 0.17F, 0.09F, 0.07F, 0.96F);
                renderCylinderFrustum(m, vc, 0.26F, 0.58F, 0.54F, 0.66F, 8, (float) (Math.PI / 8.0D), 0.72F, 0.24F, 0.03F, 0.92F);
                // Boiling golden-magma cauldron pool rim & tilted mortar barrel collar
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.59D, 0.0D), flatX, flatZ, 0.0F, 0.58F, 16, 1.00F, 0.82F, 0.36F, 0.95F);
                Vec3 mortarMouth = coreCenter.add(aimFwd.scale(0.32D)).add(0.0D, 0.22D, 0.0D);
                renderLocalRing(m, vc, mortarMouth, aimRight, aimUp.add(aimFwd.scale(-0.45D)).normalize(),
                        0.18F, 0.30F, 16, 1.00F, 0.82F, 0.36F, 0.94F);

                // 4 angled dark-basalt magma vent buttresses
                for (int b = 0; b < 4; b++) {
                    float bAng = (float) (Math.PI * 0.5D * b + Math.PI * 0.25D);
                    Vec3 ventPos = new Vec3(Mth.cos(bAng) * 0.66D, 0.32D, Mth.sin(bAng) * 0.66D);
                    renderOctahedron(m, vc, ventPos, 0.13F, 0.30F, bAng, 0.17F, 0.09F, 0.07F, 0.95F);
                    renderOctahedron(m, vc, ventPos.add(0.0D, 0.10D, 0.0D), 0.065F, 0.24F, -bAng, 1.00F, 0.82F, 0.36F, 0.96F);
                }

                // 4 orbiting explosive magma fireballs poised around the mortar core
                for (int f = 0; f < 4; f++) {
                    float fAng = -spin * 1.45F + (float) (Math.PI * 0.5D * f);
                    double fy = coreCenter.y + 0.12D * Mth.sin(age * 0.20F + f * 1.57F);
                    Vec3 fbPos = new Vec3(Mth.cos(fAng) * 0.66D, fy, Mth.sin(fAng) * 0.66D);
                    renderOctahedron(m, vc, fbPos, 0.11F, 0.12F, age * 0.25F, 1.00F, 0.86F, 0.42F, 0.98F);
                    renderOctahedron(m, vc, fbPos, 0.155F, 0.165F, -age * 0.18F, 0.72F, 0.24F, 0.03F, 0.86F);
                }
            } else if (state.materia == EssenceType.RADIANT) {
                // Section 6.8 Solar Seraph Sentry (#FFE278): white-quartz & solar-gold plinth, levitating Sun-Prism & 4 orbiting Mirror-Shields!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ, 0.76F, 0.94F, 24, 1.00F, 0.886F, 0.471F, 0.92F);
                renderCylinderFrustum(m, vc, 0.02F, 0.26F, 0.66F, 0.50F, 8, 0.0F, 0.98F, 0.96F, 0.90F, 0.95F);
                renderCylinderFrustum(m, vc, 0.26F, 0.56F, 0.48F, 0.34F, 8, (float) (Math.PI / 8.0D), 1.00F, 0.886F, 0.471F, 0.90F);

                // Levitating radiant Sun-Prism core + overhead Seraphic Halo
                renderOctahedron(m, vc, coreCenter, 0.22F, 0.36F, spin * 0.85F, 1.00F, 0.995F, 0.95F, 0.98F);
                renderLocalRing(m, vc, coreCenter.add(0.0D, 0.46D, 0.0D), flatX, flatZ, 0.30F, 0.40F, 22, 1.00F, 0.886F, 0.471F, 0.94F);

                // 4 orbiting 3D hexagonal golden Mirror-Shield facets reflecting hostile projectiles!
                for (int s = 0; s < 4; s++) {
                    float sAng = spin * 1.35F + (float) (Math.PI * 0.5D * s);
                    double sy = coreCenter.y + 0.08D * Mth.sin(age * 0.18F + s * 1.57F);
                    Vec3 rad = new Vec3(Mth.cos(sAng), 0.0D, Mth.sin(sAng));
                    Vec3 tang = new Vec3(-Mth.sin(sAng), 0.0D, Mth.cos(sAng));
                    Vec3 mirrorPos = new Vec3(rad.x * 0.74D, sy, rad.z * 0.74D);
                    Vec3 m0 = mirrorPos.subtract(tang.scale(0.18D)).add(0.0D, -0.24D, 0.0D);
                    Vec3 m1 = mirrorPos.add(tang.scale(0.18D)).add(0.0D, -0.24D, 0.0D);
                    Vec3 m2 = mirrorPos.add(tang.scale(0.18D)).add(0.0D, 0.24D, 0.0D);
                    Vec3 m3 = mirrorPos.subtract(tang.scale(0.18D)).add(0.0D, 0.24D, 0.0D);
                    emitDoubleSidedQuad(m, vc, m0, m1, m2, m3, 1.00F, 0.886F, 0.471F, 0.68F);
                    Vec3 c0 = mirrorPos.subtract(tang.scale(0.11D)).add(0.0D, -0.15D, 0.0D);
                    Vec3 c1 = mirrorPos.add(tang.scale(0.11D)).add(0.0D, -0.15D, 0.0D);
                    Vec3 c2 = mirrorPos.add(tang.scale(0.11D)).add(0.0D, 0.15D, 0.0D);
                    Vec3 c3 = mirrorPos.subtract(tang.scale(0.11D)).add(0.0D, 0.15D, 0.0D);
                    emitDoubleSidedQuad(m, vc, c0, c1, c2, c3, 1.00F, 0.995F, 0.96F, 0.92F);
                }
            } else if (state.materia == EssenceType.UMBRAL) {
                // Section 6.9 Shadow Stalker Sentry (#211A21): obsidian Eclipse Monolith suppressing mob spawns in a 16-block radius!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                renderCylinderFrustum(m, vc, 0.01F, 0.34F, 0.68F, 0.48F, 8, 0.0F, 0.085F, 0.058F, 0.095F, 0.96F);
                renderOctahedron(m, vc, coreCenter, 0.32F, 0.76F, -spin * 0.55F, 0.129F, 0.102F, 0.129F, 0.96F);
                renderLocalRing(m, vc, coreCenter, flatX, flatZ, 0.46F, 0.56F, 22, 0.56F, 0.36F, 1.00F, 0.88F);

                // Pulsing dark horizon ward rings signifying 16m hostile spawn suppression
                for (int w = 0; w < 2; w++) {
                    float wp = ((age * 0.032F + w * 0.5F) % 1.0F);
                    float wRad = 0.62F + wp * 2.15F;
                    renderLocalRing(m, vc, new Vec3(0.0D, 0.05D, 0.0D), flatX, flatZ,
                            wRad * 0.88F, wRad, 28, 0.56F, 0.36F, 1.00F, 0.72F * (1.0F - wp));
                }
            } else if (state.materia == EssenceType.UNDEAD) {
                // Section 6.10 Cryptguard Ossuary Totem Sentry (#4D6453): anchored tombstone ossuary totem,
                // 6 bleached bone-rib buttresses, crowning ossuary skull & 4 orbiting seeking cursed skulls!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ, 0.74F, 0.92F, 22,
                        0.302F, 0.392F, 0.325F, 0.92F);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.055D, 0.0D), flatX, flatZ, 0.80F, 0.86F, 22,
                        0.369F, 0.878F, 0.533F, 0.90F);
                renderCylinderFrustum(m, vc, 0.02F, 0.34F, 0.68F, 0.50F, 8, 0.0F, 0.302F, 0.392F, 0.325F, 0.96F);
                renderCylinderFrustum(m, vc, 0.34F, 0.72F, 0.46F, 0.32F, 8, (float) (Math.PI / 8.0D), 0.22F, 0.29F, 0.24F, 0.95F);

                // 6 curved bleached-bone rib buttresses gripping the ossuary totem
                for (int rIdx = 0; rIdx < 6; rIdx++) {
                    float rAng = (float) (Math.PI * 2.0D * rIdx / 6.0D);
                    Vec3 rDir = new Vec3(Mth.cos(rAng), 0.0D, Mth.sin(rAng));
                    Vec3 tang = new Vec3(-Mth.sin(rAng), 0.0D, Mth.cos(rAng));
                    Vec3 bot = rDir.scale(0.76D).add(0.0D, 0.04D, 0.0D);
                    Vec3 mid = rDir.scale(0.58D).add(0.0D, 0.46D, 0.0D);
                    Vec3 top = rDir.scale(0.32D).add(0.0D, 0.82D, 0.0D);
                    Vec3 rw = tang.scale(0.052D);
                    emitDoubleSidedQuad(m, vc, bot.subtract(rw), bot.add(rw), mid.add(rw), mid.subtract(rw),
                            0.902F, 0.875F, 0.812F, 0.95F);
                    emitDoubleSidedQuad(m, vc, mid.subtract(rw), mid.add(rw), top.add(rw.scale(0.6D)), top.subtract(rw.scale(0.6D)),
                            0.84F, 0.81F, 0.74F, 0.94F);
                }

                // Central ossuary totem skull crown + blazing #5EE088 necrotic soul eye sockets
                Vec3 totemSkull = coreCenter.add(0.0D, 0.14D, 0.0D);
                renderOctahedron(m, vc, totemSkull, 0.24F, 0.28F, 0.0F, 0.902F, 0.875F, 0.812F, 0.96F);
                for (int side = -1; side <= 1; side += 2) {
                    Vec3 eye = totemSkull.add(aimFwd.scale(0.22D)).add(aimRight.scale(side * 0.09D)).add(0.0D, 0.03D, 0.0D);
                    renderLocalRing(m, vc, eye, aimRight, aimUp, 0.0F, 0.055F, 8, 0.369F, 0.878F, 0.533F, 0.98F);
                }

                // 4 orbiting seeking cursed skulls poised to launch at living targets!
                for (int k = 0; k < 4; k++) {
                    float kAng = -spin * 1.35F + (float) (Math.PI * 0.5D * k);
                    double ky = coreCenter.y + 0.12D * Mth.sin(age * 0.20F + k * 1.57F);
                    Vec3 skullPos = new Vec3(Mth.cos(kAng) * 0.68D, ky, Mth.sin(kAng) * 0.68D);
                    renderOctahedron(m, vc, skullPos, 0.115F, 0.135F, kAng, 0.902F, 0.875F, 0.812F, 0.95F);
                    renderOctahedron(m, vc, skullPos.add(0.0D, 0.02D, 0.0D), 0.065F, 0.075F, -kAng,
                            0.369F, 0.878F, 0.533F, 0.98F);
                }
            } else if (state.materia == EssenceType.VOID) {
                // Section 6.11 Void Gravity Pylon Sentry (#41304E): anchored deep-violet cosmic pylon,
                // suspended pitch-black singularity core, 3 gyroscopic #C77DFF accretion rings & inward gravity waves!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ, 0.72F, 0.92F, 24,
                        0.255F, 0.188F, 0.306F, 0.94F);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.055D, 0.0D), flatX, flatZ, 0.78F, 0.85F, 24,
                        0.780F, 0.490F, 1.000F, 0.92F);
                renderCylinderFrustum(m, vc, 0.02F, 0.42F, 0.66F, 0.40F, 8, 0.0F, 0.255F, 0.188F, 0.306F, 0.96F);

                // 2 INWARD-collapsing ground gravity pulse rings signifying 10m enemy suction!
                for (int g = 0; g < 2; g++) {
                    float gp = 1.0F - (((age * 0.045F) + g * 0.5F) % 1.0F);
                    float gRad = 0.52F + gp * 1.95F;
                    renderLocalRing(m, vc, new Vec3(0.0D, 0.05D, 0.0D), flatX, flatZ,
                            gRad * 0.88F, gRad, 28, 0.780F, 0.490F, 1.000F, 0.78F * (1.0F - gp));
                }

                // Suspended pitch-black singularity core + 3 tilted #C77DFF accretion hoops
                Vec3 bhCenter = coreCenter.add(0.0D, 0.16D, 0.0D);
                renderOctahedron(m, vc, bhCenter, 0.28F, 0.32F, -spin * 0.8F, 0.045F, 0.025F, 0.070F, 0.98F);
                renderOctahedron(m, vc, bhCenter, 0.35F, 0.40F, spin * 0.6F, 0.255F, 0.188F, 0.306F, 0.58F);

                Vec3 tX1 = new Vec3(Mth.cos(spin), 0.30D, Mth.sin(spin)).normalize();
                Vec3 tZ1 = new Vec3(-Mth.sin(spin), 0.30D, Mth.cos(spin)).normalize();
                renderLocalRing(m, vc, bhCenter, tX1, tZ1, 0.46F, 0.58F, 24, 0.780F, 0.490F, 1.000F, 0.90F);
                Vec3 tX2 = new Vec3(Mth.cos(-spin * 0.8F), -0.42D, Mth.sin(-spin * 0.8F)).normalize();
                Vec3 tZ2 = tX2.cross(new Vec3(0.0D, 1.0D, 0.0D)).normalize();
                renderLocalRing(m, vc, bhCenter, tX2, tZ2, 0.52F, 0.60F, 22, 0.255F, 0.188F, 0.306F, 0.88F);

                // 4 orbiting wall-phasing Void Darts poised around the pylon!
                for (int d = 0; d < 4; d++) {
                    float dAng = spin * 1.45F + (float) (Math.PI * 0.5D * d);
                    double dy = bhCenter.y + 0.10D * Mth.sin(age * 0.22F + d * 1.57F);
                    Vec3 dartPos = new Vec3(Mth.cos(dAng) * 0.72D, dy, Mth.sin(dAng) * 0.72D);
                    renderOctahedron(m, vc, dartPos, 0.075F, 0.20F, dAng, 0.780F, 0.490F, 1.000F, 0.95F);
                }
            } else if (state.materia == EssenceType.ARID) {
                // Section 6.12 Sandstone Obelisk Sentry (#C2B28F): anchored 4-sided desert sandstone obelisk,
                // sunlit #F5E4BE pyramidion capstone, whirling base dust rings & 4 orbiting high-velocity sand slugs!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float obRot = (float) (Math.PI * 0.25D);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ, 0.68F, 0.94F, 24,
                        0.761F, 0.698F, 0.561F, 0.92F);
                renderCylinderFrustum(m, vc, 0.02F, 0.34F, 0.64F, 0.48F, 4, obRot, 0.612F, 0.478F, 0.298F, 0.96F);
                renderCylinderFrustum(m, vc, 0.34F, 1.56F, 0.44F, 0.24F, 4, obRot, 0.761F, 0.698F, 0.561F, 0.98F);
                renderOctahedron(m, vc, new Vec3(0.0D, 1.78D, 0.0D), 0.24F, 0.26F, obRot,
                        0.961F, 0.894F, 0.745F, 0.98F);

                // Whirling sandstorm crescents sweeping around the Sandstone Obelisk
                for (int c = 0; c < 3; c++) {
                    float cAng = spin * 1.45F + (float) (Math.PI * 2.0D * c / 3.0D);
                    renderSweptArcBlade(m, vc, new Vec3(0.0D, 0.24D + c * 0.38D, 0.0D),
                            0.74F - c * 0.08F, cAng, 1.15F, 0.14F, 0.961F, 0.894F, 0.745F, 0.86F);
                }

                // 4 orbiting high-velocity sandstone slugs poised to fire with heavy knockback!
                for (int s = 0; s < 4; s++) {
                    float sAng = -spin * 1.55F + (float) (Math.PI * 0.5D * s);
                    double sy = coreCenter.y + 0.12D * Mth.sin(age * 0.22F + s * 1.57F);
                    Vec3 slugPos = new Vec3(Mth.cos(sAng) * 0.72D, sy, Mth.sin(sAng) * 0.72D);
                    renderOctahedron(m, vc, slugPos, 0.095F, 0.18F, sAng, 0.761F, 0.698F, 0.561F, 0.96F);
                    renderOctahedron(m, vc, slugPos, 0.065F, 0.12F, -sAng, 0.961F, 0.894F, 0.745F, 0.98F);
                }
            } else if (state.materia == EssenceType.LIGHTNING) {
                // Section 6.13 Volt-Spark Sentry (#FFE600): anchored copper-and-gold Tesla Coil Pylon,
                // high-voltage toroidal discharge crown, crackling coil rings & 4 orbiting Ball-Lightning orbs!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ, 0.68F, 0.94F, 24,
                        1.00F, 0.902F, 0.00F, 0.94F);
                renderCylinderFrustum(m, vc, 0.02F, 0.36F, 0.62F, 0.44F, 8, 0.0F, 0.85F, 0.48F, 0.22F, 0.96F);
                renderCylinderFrustum(m, vc, 0.36F, 1.48F, 0.30F, 0.22F, 8, 0.0F, 0.78F, 0.44F, 0.20F, 0.96F);

                // Copper secondary winding rings & white-hot toroidal Tesla discharge terminal
                for (int w = 0; w < 4; w++) {
                    double wy = 0.50D + w * 0.28D;
                    renderLocalRing(m, vc, new Vec3(0.0D, wy, 0.0D), flatX, flatZ, 0.24F, 0.36F, 18,
                            1.00F, 0.902F, 0.00F, 0.94F);
                }
                Vec3 toroid = new Vec3(0.0D, 1.62D, 0.0D);
                renderLocalRing(m, vc, toroid, flatX, flatZ, 0.28F, 0.48F, 24, 1.00F, 0.902F, 0.00F, 0.96F);
                renderOctahedron(m, vc, toroid, 0.22F, 0.22F, spin * 1.6F, 1.00F, 1.00F, 0.95F, 0.99F);

                // 4 orbiting homing Ball-Lightning Orbs poised to launch!
                for (int b = 0; b < 4; b++) {
                    float bAng = spin * 1.85F + (float) (Math.PI * 0.5D * b);
                    double by = coreCenter.y + 0.14D * Mth.sin(age * 0.28F + b * 1.57F);
                    Vec3 orbPos = new Vec3(Mth.cos(bAng) * 0.74D, by, Mth.sin(bAng) * 0.74D);
                    renderOctahedron(m, vc, orbPos, 0.12F, 0.12F, bAng, 1.00F, 0.902F, 0.00F, 0.96F);
                    renderOctahedron(m, vc, orbPos, 0.075F, 0.075F, -bAng, 1.00F, 1.00F, 0.96F, 0.99F);
                }
            } else if (state.materia == EssenceType.MAGMA) {
                // Section 6.14 Volcanic Magma Mortar (#B83C08): heavy terraced obsidian/pumice caldera mortar cone,
                // boiling molten lava crater pool, 6 basalt buttresses & 4 orbiting Molten Magma Boulders!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ, 0.72F, 1.02F, 24,
                        0.722F, 0.235F, 0.031F, 0.94F);
                renderCylinderFrustum(m, vc, 0.02F, 0.52F, 0.86F, 0.62F, 8, 0.0F, 0.165F, 0.090F, 0.071F, 0.96F);
                renderCylinderFrustum(m, vc, 0.52F, 1.18F, 0.58F, 0.44F, 8, 0.39F, 0.200F, 0.110F, 0.085F, 0.96F);

                // Boiling #B83C08 / #FFD15C lava crater rim & glowing magma fissures
                renderLocalRing(m, vc, new Vec3(0.0D, 1.20D, 0.0D), flatX, flatZ, 0.0F, 0.42F, 18,
                        0.722F, 0.235F, 0.031F, 0.96F);
                renderLocalRing(m, vc, new Vec3(0.0D, 1.22D, 0.0D), flatX, flatZ, 0.12F, 0.34F, 18,
                        1.00F, 0.820F, 0.361F, 0.99F);

                for (int b = 0; b < 6; b++) {
                    float bAng = (float) (Math.PI * 2.0D * b / 6.0D);
                    Vec3 buttress = new Vec3(Mth.cos(bAng) * 0.64D, 0.44D, Mth.sin(bAng) * 0.64D);
                    renderOctahedron(m, vc, buttress, 0.16F, 0.42F, bAng, 0.165F, 0.090F, 0.071F, 0.94F);
                }

                // 4 orbiting fractured Molten Magma Boulders poised to arc from the volcanic mortar!
                for (int s = 0; s < 4; s++) {
                    float sAng = -spin * 1.15F + (float) (Math.PI * 0.5D * s);
                    double sy = 1.42D + 0.12D * Mth.sin(age * 0.22F + s * 1.57F);
                    Vec3 boulderPos = new Vec3(Mth.cos(sAng) * 0.72D, sy, Mth.sin(sAng) * 0.72D);
                    renderOctahedron(m, vc, boulderPos, 0.15F, 0.15F, sAng, 0.165F, 0.090F, 0.071F, 0.92F);
                    renderOctahedron(m, vc, boulderPos, 0.12F, 0.12F, -sAng, 0.722F, 0.235F, 0.031F, 0.96F);
                    renderOctahedron(m, vc, boulderPos, 0.075F, 0.075F, sAng * 1.5F, 1.00F, 0.820F, 0.361F, 0.99F);
                }
            } else if (state.materia == EssenceType.STORM) {
                // Section 6.15 Tempest-Squall Spire (#429BA3): anchored hurricane water-wind spire,
                // 4 sweeping seafoam gale-crescents & 4 orbiting pressurized Squall Tempest Slugs!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ, 0.66F, 0.94F, 24,
                        0.259F, 0.608F, 0.639F, 0.92F);
                renderOpenFrustumWall(m, vc, 0.05F, 1.58F, 0.24F, 0.62F, 10, spin * 0.9F,
                        0.259F, 0.608F, 0.639F, 0.56F);

                for (int c = 0; c < 4; c++) {
                    float cAng = spin * 1.95F + (float) (Math.PI * 0.5D * c);
                    renderSweptArcBlade(m, vc, new Vec3(0.0D, 0.28D + c * 0.34D, 0.0D),
                            0.44F + c * 0.08F, cAng, 1.25F, 0.14F, 0.847F, 0.980F, 1.000F, 0.90F);
                }

                for (int s = 0; s < 4; s++) {
                    float sAng = -spin * 1.65F + (float) (Math.PI * 0.5D * s);
                    double sy = coreCenter.y + 0.14D * Mth.sin(age * 0.26F + s * 1.57F);
                    Vec3 slugPos = new Vec3(Mth.cos(sAng) * 0.74D, sy, Mth.sin(sAng) * 0.74D);
                    renderOctahedron(m, vc, slugPos, 0.11F, 0.18F, sAng, 0.259F, 0.608F, 0.639F, 0.95F);
                    renderOctahedron(m, vc, slugPos, 0.070F, 0.12F, -sAng, 0.847F, 0.980F, 1.000F, 0.99F);
                }
            } else if (state.materia == EssenceType.GLACIAL) {
                // Section 6.16 Cryo-Pylon Sentry (#98DCF2): hexagonal Blue-Ice plinth, rotating permafrost seal & 6 orbiting icicle darts!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ, 0.66F, 0.94F, 6,
                        0.173F, 0.510F, 0.788F, 0.92F);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.060D, 0.0D), flatX, flatZ, 0.50F, 0.64F, 18,
                        0.596F, 0.863F, 0.949F, 0.96F);
                renderOpenFrustumWall(m, vc, 0.05F, 1.48F, 0.42F, 0.26F, 6, spin * 0.25F,
                        0.596F, 0.863F, 0.949F, 0.64F);

                for (int ic = 0; ic < 6; ic++) {
                    float icAng = -spin * 1.45F + (float) (Math.PI * 2.0D * ic / 6.0D);
                    double iy = coreCenter.y + 0.10D * Mth.sin(age * 0.24F + ic * 1.05F);
                    Vec3 dartPos = new Vec3(Mth.cos(icAng) * 0.76D, iy, Mth.sin(icAng) * 0.76D);
                    boolean pale = (ic % 2 == 0);
                    renderOctahedron(m, vc, dartPos, 0.085F, 0.26F, icAng,
                            pale ? 0.596F : 0.173F, pale ? 0.863F : 0.510F, pale ? 0.949F : 0.788F, 0.96F);
                    renderOctahedron(m, vc, dartPos, 0.045F, 0.20F, -icAng, 0.90F, 1.00F, 1.00F, 0.99F);
                }
            } else if (state.materia == EssenceType.OVERGROWTH) {
                // Section 6.17 Living Root Totem (#1A5923): gnarled wooden root & mossy-stone pillar, blooming crown & 4 thorn darts!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ, 0.68F, 0.96F, 18,
                        0.467F, 0.196F, 0.078F, 0.92F);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.060D, 0.0D), flatX, flatZ, 0.52F, 0.66F, 18,
                        0.173F, 0.902F, 0.408F, 0.96F);
                renderOpenFrustumWall(m, vc, 0.05F, 1.52F, 0.46F, 0.28F, 8, spin * 0.20F,
                        0.102F, 0.349F, 0.137F, 0.72F);

                for (int td = 0; td < 4; td++) {
                    float tdAng = spin * 1.35F + (float) (Math.PI * 0.5D * td);
                    double ty = coreCenter.y + 0.12D * Mth.sin(age * 0.22F + td * 1.57F);
                    Vec3 dartPos = new Vec3(Mth.cos(tdAng) * 0.76D, ty, Mth.sin(tdAng) * 0.76D);
                    renderOctahedron(m, vc, dartPos, 0.10F, 0.24F, tdAng, 0.467F, 0.196F, 0.078F, 0.95F);
                    renderOctahedron(m, vc, dartPos, 0.065F, 0.18F, -tdAng, 0.706F, 1.000F, 0.471F, 0.99F);
                }
            } else if (state.materia == EssenceType.DUST) {
                // Section 6.18 Abrasive Dust Pylon (#C2B28F): octagonal chiseled sandstone & chalk pneumatic pylon,
                // pressurized whiteout vent rings & 4 orbiting high-pressure mineral dust slugs!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ, 0.68F, 0.96F, 24,
                        0.761F, 0.698F, 0.561F, 0.94F);
                renderCylinderFrustum(m, vc, 0.02F, 0.36F, 0.64F, 0.46F, 8, 0.0F, 0.549F, 0.490F, 0.396F, 0.96F);
                renderCylinderFrustum(m, vc, 0.36F, 1.54F, 0.40F, 0.24F, 8, 0.0F, 0.761F, 0.698F, 0.561F, 0.96F);

                for (int v = 0; v < 3; v++) {
                    double vy = 0.52D + v * 0.36D;
                    renderLocalRing(m, vc, new Vec3(0.0D, vy, 0.0D), flatX, flatZ, 0.34F, 0.48F, 20,
                            0.949F, 0.925F, 0.882F, 0.92F);
                }
                for (int s = 0; s < 4; s++) {
                    float sAng = -spin * 1.75F + (float) (Math.PI * 0.5D * s);
                    double sy = coreCenter.y + 0.12D * Mth.sin(age * 0.26F + s * 1.57F);
                    Vec3 slugPos = new Vec3(Mth.cos(sAng) * 0.76D, sy, Mth.sin(sAng) * 0.76D);
                    renderOctahedron(m, vc, slugPos, 0.11F, 0.15F, sAng, 0.761F, 0.698F, 0.561F, 0.96F);
                    renderOctahedron(m, vc, slugPos, 0.075F, 0.10F, -sAng, 0.949F, 0.925F, 0.882F, 0.99F);
                }
            } else if (state.materia == EssenceType.SPORE) {
                // Section 6.19 Bio-Pollen Spire (#1A5923 / #A88654): living mycelial mushroom stalk,
                // glowing golden-gilled canopy umbrella & 4 orbiting buoyant bio-pollen puffballs!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ, 0.66F, 0.96F, 24,
                        0.102F, 0.349F, 0.137F, 0.92F);
                renderCylinderFrustum(m, vc, 0.02F, 1.32F, 0.34F, 0.22F, 10, 0.0F, 0.910F, 0.784F, 0.510F, 0.95F);
                // Tiered mushroom canopy cap (#1A5923 top with #A88654 golden gill underside)
                renderCylinderFrustum(m, vc, 1.24F, 1.46F, 0.92F, 0.56F, 14, spin * 0.25F, 0.102F, 0.349F, 0.137F, 0.96F);
                renderCylinderFrustum(m, vc, 1.46F, 1.66F, 0.56F, 0.10F, 14, spin * 0.25F, 0.173F, 0.902F, 0.408F, 0.94F);
                renderLocalRing(m, vc, new Vec3(0.0D, 1.23D, 0.0D), flatX, flatZ, 0.24F, 0.90F, 20,
                        0.910F, 0.784F, 0.510F, 0.92F);

                for (int p = 0; p < 4; p++) {
                    float pAng = spin * 1.35F + (float) (Math.PI * 0.5D * p);
                    double py = coreCenter.y + 0.14D * Mth.sin(age * 0.22F + p * 1.57F);
                    Vec3 puffPos = new Vec3(Mth.cos(pAng) * 0.80D, py, Mth.sin(pAng) * 0.80D);
                    renderOctahedron(m, vc, puffPos, 0.13F, 0.14F, pAng, 0.102F, 0.349F, 0.137F, 0.94F);
                    renderOctahedron(m, vc, puffPos, 0.085F, 0.09F, -pAng, 0.910F, 0.784F, 0.510F, 0.98F);
                }
            } else if (state.materia == EssenceType.TAIGA) {
                // Section 6.20 Frost-Howler Spire (#1A5923 / #98DCF2): petrified spruce-bark totem spire,
                // howling frost-cyan crystal horns, +25% attack-speed / cold-resist aura rings & 4 orbiting frost-resin spikes!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float pulseWave = (age * 0.055F) % 1.0F;
                float auraR = 0.65F + pulseWave * 1.35F;
                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ, 0.68F, 0.98F, 24,
                        0.102F, 0.349F, 0.137F, 0.92F);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.060D, 0.0D), flatX, flatZ, auraR * 0.88F, auraR, 26,
                        0.596F, 0.863F, 0.949F, 0.86F * (1.0F - pulseWave));
                // Tiered evergreen conifer trunk & frosted crown
                renderCylinderFrustum(m, vc, 0.02F, 1.28F, 0.36F, 0.24F, 8, 0.0F, 0.102F, 0.349F, 0.137F, 0.96F);
                renderCylinderFrustum(m, vc, 0.72F, 1.18F, 0.76F, 0.26F, 8, 0.39F, 0.102F, 0.349F, 0.137F, 0.88F);
                renderCylinderFrustum(m, vc, 1.16F, 1.68F, 0.58F, 0.10F, 8, 0.0F, 0.596F, 0.863F, 0.949F, 0.90F);
                renderOctahedron(m, vc, new Vec3(0.0D, 1.84D, 0.0D), 0.18F, 0.34F, spin * 0.8F, 0.596F, 0.863F, 0.949F, 0.98F);

                for (int s = 0; s < 4; s++) {
                    float sAng = spin * 1.55F + (float) (Math.PI * 0.5D * s);
                    double sy = coreCenter.y + 0.12D * Mth.sin(age * 0.24F + s * 1.57F);
                    Vec3 spikePos = new Vec3(Mth.cos(sAng) * 0.82D, sy, Mth.sin(sAng) * 0.82D);
                    renderOctahedron(m, vc, spikePos, 0.10F, 0.24F, sAng, 0.102F, 0.349F, 0.137F, 0.95F);
                    renderOctahedron(m, vc, spikePos, 0.13F, 0.18F, -sAng, 0.596F, 0.863F, 0.949F, 0.78F);
                }
            } else if (state.materia == EssenceType.OASIS) {
                // Section 6.21 Aquifer Spring Spire (#1A5923 / #C2B28F): carved sunlit sandstone fountain basin,
                // central sparkling springwater column, lush succulent palm crown & 4 orbiting water/pollen orbs!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float springWave = (age * 0.050F) % 1.0F;
                float springR = 0.65F + springWave * 1.40F;
                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ, 0.70F, 1.00F, 24,
                        0.761F, 0.698F, 0.561F, 0.94F);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.060D, 0.0D), flatX, flatZ, springR * 0.88F, springR, 26,
                        0.220F, 0.780F, 0.960F, 0.86F * (1.0F - springWave));
                // Sandstone basin + glowing azure springwater fountain jet + lush green succulent crown
                renderCylinderFrustum(m, vc, 0.02F, 0.54F, 0.58F, 0.72F, 10, 0.0F, 0.761F, 0.698F, 0.561F, 0.95F);
                renderCylinderFrustum(m, vc, 0.48F, 1.62F, 0.24F, 0.16F, 10, spin * 0.4F, 0.220F, 0.780F, 0.960F, 0.88F);
                renderCylinderFrustum(m, vc, 1.38F, 1.72F, 0.76F, 0.22F, 8, spin * 0.2F, 0.102F, 0.349F, 0.137F, 0.94F);
                renderOctahedron(m, vc, new Vec3(0.0D, 1.86D, 0.0D), 0.18F, 0.26F, spin, 0.761F, 0.698F, 0.561F, 0.98F);

                for (int o = 0; o < 4; o++) {
                    float oAng = spin * 1.35F + (float) (Math.PI * 0.5D * o);
                    double oy = coreCenter.y + 0.14D * Mth.sin(age * 0.22F + o * 1.57F);
                    Vec3 orbPos = new Vec3(Mth.cos(oAng) * 0.80D, oy, Mth.sin(oAng) * 0.80D);
                    boolean waterDrop = (o % 2 == 0);
                    renderOctahedron(m, vc, orbPos, 0.12F, 0.14F, oAng,
                            waterDrop ? 0.220F : 0.102F,
                            waterDrop ? 0.780F : 0.349F,
                            waterDrop ? 0.960F : 0.137F,
                            0.94F);
                }
            } else if (state.materia == EssenceType.VITAE) {
                // Section 6.22 Cardiovascular Spire (#FF6B9D): pulsing rose-crystal heartbeat chamber,
                // double-helix vascular rings & 4 orbiting defensive vitality darts!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float beat = 0.88F + 0.18F * Mth.abs(Mth.sin(age * 0.22F));
                float pulseWave = (age * 0.055F) % 1.0F;
                float pulseR = 0.65F + pulseWave * 1.45F;
                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ, 0.68F, 0.98F, 24,
                        1.000F, 0.420F, 0.616F, 0.92F);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.060D, 0.0D), flatX, flatZ, pulseR * 0.88F, pulseR, 26,
                        1.000F, 0.820F, 0.890F, 0.88F * (1.0F - pulseWave));

                renderCylinderFrustum(m, vc, 0.02F, 0.46F, 0.62F, 0.44F, 10, 0.0F, 0.788F, 0.180F, 0.400F, 0.94F);
                renderOpenFrustumWall(m, vc, 0.38F, 1.62F, 0.42F, 0.32F, 10, spin * 0.35F, 1.000F, 0.420F, 0.616F, 0.56F);
                Vec3 heartPos = new Vec3(0.0D, 1.15D, 0.0D);
                renderOctahedron(m, vc, heartPos, 0.24F * beat, 0.34F * beat, spin * 0.5F, 1.000F, 0.420F, 0.616F, 0.96F);
                renderOctahedron(m, vc, heartPos, 0.15F * beat, 0.22F * beat, -spin * 0.7F, 1.000F, 0.820F, 0.890F, 0.99F);

                for (int d = 0; d < 4; d++) {
                    float dAng = -spin * 1.55F + (float) (Math.PI * 0.5D * d);
                    double dy = 1.15D + 0.14D * Mth.sin(age * 0.24F + d * 1.57F);
                    Vec3 dartPos = new Vec3(Mth.cos(dAng) * 0.76D, dy, Mth.sin(dAng) * 0.76D);
                    renderOctahedron(m, vc, dartPos, 0.085F, 0.24F, dAng, 1.000F, 0.420F, 0.616F, 0.95F);
                    renderOctahedron(m, vc, dartPos, 0.050F, 0.28F, -dAng, 1.000F, 0.820F, 0.890F, 0.99F);
                }
            } else if (state.materia == EssenceType.BLOOD) {
                // Section 6.23 Exsanguination Pylon (#8A0303): anchored crimson reservoir obelisk,
                // boiling venous-red blood chalice core, 8m siphon sigil & twin orbiting exsanguination tether nodes!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float pulse = 0.88F + 0.16F * Mth.abs(Mth.sin(age * 0.20F));
                float siphonWave = 1.0F - ((age * 0.048F) % 1.0F);
                float siphonR = 0.58F + siphonWave * 1.55F;
                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ, 0.68F, 0.98F, 24,
                        0.541F, 0.012F, 0.012F, 0.94F);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.060D, 0.0D), flatX, flatZ, siphonR * 0.88F, siphonR, 26,
                        1.000F, 0.220F, 0.298F, 0.86F * (1.0F - siphonWave));

                renderCylinderFrustum(m, vc, 0.02F, 0.48F, 0.64F, 0.44F, 8, 0.0F, 0.220F, 0.015F, 0.025F, 0.96F);
                renderCylinderFrustum(m, vc, 0.48F, 1.48F, 0.40F, 0.26F, 8, (float) (Math.PI / 8.0D),
                        0.541F, 0.012F, 0.012F, 0.94F);
                // 4 hardened coagulated blood spikes buttressing the pylon base
                for (int s = 0; s < 4; s++) {
                    float sAng = (float) (Math.PI * 0.5D * s + Math.PI * 0.25D);
                    Vec3 spikePos = new Vec3(Mth.cos(sAng) * 0.58D, 0.42D, Mth.sin(sAng) * 0.58D);
                    renderOctahedron(m, vc, spikePos, 0.12F, 0.36F, sAng, 0.541F, 0.012F, 0.012F, 0.95F);
                }

                Vec3 reservoir = new Vec3(0.0D, 1.66D, 0.0D);
                renderOctahedron(m, vc, reservoir, 0.25F * pulse, 0.32F * pulse, spin * 0.55F,
                        0.541F, 0.012F, 0.012F, 0.96F);
                renderOctahedron(m, vc, reservoir, 0.16F * pulse, 0.22F * pulse, -spin * 0.75F,
                        1.000F, 0.220F, 0.298F, 0.99F);

                // Twin orbiting exsanguination tether nodes (matching its 2-hostile siphon tethers!)
                for (int t = 0; t < 2; t++) {
                    float tAng = spin * 1.35F + t * (float) Math.PI;
                    double ty = 1.24D + 0.14D * Mth.sin(age * 0.22F + t * 3.14F);
                    Vec3 tetherOrb = new Vec3(Mth.cos(tAng) * 0.78D, ty, Mth.sin(tAng) * 0.78D);
                    renderOctahedron(m, vc, tetherOrb, 0.13F, 0.18F, tAng, 0.541F, 0.012F, 0.012F, 0.96F);
                    renderOctahedron(m, vc, tetherOrb, 0.08F, 0.12F, -tAng, 1.000F, 0.220F, 0.298F, 0.99F);
                }
            } else if (state.materia == EssenceType.AURA) {
                // Section 6.24 Chakra Respiration Spire (#FF6B9D / #A88654): meditative golden-pink lotus pagoda spire,
                // levitating pranic breath crystal & 8m fuel-discount / Slow-Falling respiration rings!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float breath = 0.88F + 0.14F * Mth.sin(age * 0.12F);
                float breathWave = (age * 0.045F) % 1.0F;
                float breathR = 0.65F + breathWave * 1.85F;
                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ, 0.68F, 0.98F, 24,
                        0.839F, 0.706F, 0.471F, 0.92F);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.060D, 0.0D), flatX, flatZ, breathR * 0.88F, breathR, 28,
                        1.000F, 0.420F, 0.616F, 0.86F * (1.0F - breathWave));

                renderCylinderFrustum(m, vc, 0.02F, 0.44F, 0.60F, 0.42F, 8, 0.0F, 0.659F, 0.525F, 0.329F, 0.94F);
                renderOpenFrustumWall(m, vc, 0.42F, 1.56F, 0.40F, 0.28F, 8, spin * 0.30F,
                        1.000F, 0.420F, 0.616F, 0.52F);

                Vec3 chakraCore = new Vec3(0.0D, 1.24D, 0.0D);
                renderOctahedron(m, vc, chakraCore, 0.24F * breath, 0.34F * breath, spin * 0.65F,
                        1.000F, 0.420F, 0.616F, 0.96F);
                renderOctahedron(m, vc, chakraCore, 0.15F * breath, 0.22F * breath, -spin * 0.85F,
                        0.839F, 0.706F, 0.471F, 0.99F);

                // 6 floating golden-pink pranic prayer beads orbiting in serenity
                for (int b = 0; b < 6; b++) {
                    float bAng = spin * 1.15F + (float) (Math.PI * 2.0D * b / 6.0D);
                    double by = 1.24D + 0.14D * Mth.sin(age * 0.18F + b * 1.047F);
                    Vec3 beadPos = new Vec3(Mth.cos(bAng) * 0.76D, by, Mth.sin(bAng) * 0.76D);
                    boolean pink = (b % 2 == 0);
                    renderOctahedron(m, vc, beadPos, 0.095F, 0.12F, bAng,
                            pink ? 1.000F : 0.839F,
                            pink ? 0.420F : 0.706F,
                            pink ? 0.616F : 0.471F,
                            0.96F);
                }
            } else if (state.materia == EssenceType.AMBER) {
                // Section 6.25 Resinite Stasis Spire Sentry (#FF6B9D / #773214 / #F29B2E): petrified deep-amber plinth,
                // translucent golden resin vault dome, #FF6B9D fossilized rose heart & 8m +4-Armor preservation ring!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float pulse = 0.90F + 0.10F * Mth.sin(age * 0.14F);
                float vaultWave = (age * 0.038F) % 1.0F;
                float vaultR = 0.70F + vaultWave * 1.90F;
                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ, 0.68F, 1.00F, 6,
                        0.467F, 0.196F, 0.078F, 0.94F);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.060D, 0.0D), flatX, flatZ, vaultR * 0.88F, vaultR, 24,
                        0.949F, 0.608F, 0.180F, 0.84F * (1.0F - vaultWave));

                renderCylinderFrustum(m, vc, 0.02F, 0.48F, 0.62F, 0.46F, 6, 0.0F, 0.467F, 0.196F, 0.078F, 0.96F);
                renderOpenFrustumWall(m, vc, 0.46F, 1.62F, 0.44F, 0.30F, 6, spin * 0.16F,
                        0.949F, 0.608F, 0.180F, 0.56F);

                Vec3 fossilCore = new Vec3(0.0D, 1.24D, 0.0D);
                renderOctahedron(m, vc, fossilCore, 0.25F * pulse, 0.38F * pulse, spin * 0.40F,
                        0.949F, 0.608F, 0.180F, 0.66F);
                renderOctahedron(m, vc, fossilCore, 0.16F * pulse, 0.24F * pulse, -spin * 0.55F,
                        1.000F, 0.420F, 0.616F, 0.98F);

                // 4 orbiting slowing golden-resin & living-pink crystal slugs
                for (int s = 0; s < 4; s++) {
                    float sAng = spin * 0.85F + (float) (Math.PI * 0.5D * s);
                    double sy = 1.20D + 0.10D * Mth.sin(age * 0.16F + s * 1.57F);
                    Vec3 slugPos = new Vec3(Mth.cos(sAng) * 0.78D, sy, Mth.sin(sAng) * 0.78D);
                    boolean rose = (s % 2 == 0);
                    renderOctahedron(m, vc, slugPos, 0.10F, 0.14F, sAng,
                            rose ? 1.000F : 0.949F,
                            rose ? 0.420F : 0.608F,
                            rose ? 0.616F : 0.180F,
                            0.95F);
                }
            } else if (state.materia == EssenceType.ECLIPSE) {
                // Section 6.26 Black Sun Corona Spire Sentry (#FFEA78 / #211A21): obsidian-shadow plinth,
                // levitating black sun sphere encircled by blazing golden coronal flares & blinding twilight ring!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float coronaWave = (age * 0.045F) % 1.0F;
                float coronaR = 0.72F + coronaWave * 1.95F;
                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ, 0.66F, 1.02F, 24,
                        1.000F, 0.918F, 0.471F, 0.95F);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.060D, 0.0D), flatX, flatZ, coronaR * 0.88F, coronaR, 28,
                        0.129F, 0.102F, 0.129F, 0.88F * (1.0F - coronaWave));

                renderCylinderFrustum(m, vc, 0.02F, 0.50F, 0.58F, 0.40F, 8, 0.0F, 0.129F, 0.102F, 0.129F, 0.96F);
                renderOpenFrustumWall(m, vc, 0.48F, 1.54F, 0.38F, 0.24F, 8, -spin * 0.28F,
                        0.129F, 0.102F, 0.129F, 0.72F);

                Vec3 blackSun = new Vec3(0.0D, 1.30D, 0.0D);
                renderOctahedron(m, vc, blackSun, 0.28F, 0.28F, spin * 0.35F,
                        0.080F, 0.060F, 0.085F, 0.99F);
                Vec3 coronaU = new Vec3(Mth.cos(spin * 0.5F), 0.25D, Mth.sin(spin * 0.5F)).normalize();
                Vec3 coronaV = new Vec3(-Mth.sin(spin * 0.5F), 0.85D, Mth.cos(spin * 0.5F)).normalize();
                renderLocalRing(m, vc, blackSun, coronaU, coronaV, 0.32F, 0.46F, 24,
                        1.000F, 0.918F, 0.471F, 0.96F);

                // 6 orbiting golden-and-black coronal flare bolts
                for (int b = 0; b < 6; b++) {
                    float bAng = -spin * 1.25F + (float) (Math.PI * 2.0D * b / 6.0D);
                    double by = 1.30D + 0.12D * Mth.sin(age * 0.20F + b);
                    Vec3 boltPos = new Vec3(Mth.cos(bAng) * 0.80D, by, Mth.sin(bAng) * 0.80D);
                    boolean gold = (b % 2 == 0);
                    renderOctahedron(m, vc, boltPos, 0.095F, 0.13F, bAng,
                            gold ? 1.000F : 0.129F,
                            gold ? 0.918F : 0.102F,
                            gold ? 0.471F : 0.129F,
                            0.96F);
                }
            } else if (state.materia == EssenceType.BLIGHT) {
                // Section 6.27 Necro-Briar Spire Sentry (#4D6453 / #34CC48): tombstone bone-bark plinth,
                // coiled necrotic briar cage, festering #34CC48 spore heart & 8m 30% slow ring!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float pulse = 0.88F + 0.14F * Mth.sin(age * 0.18F);
                float rotWave = (age * 0.040F) % 1.0F;
                float rotR = 0.68F + rotWave * 1.85F;
                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ, 0.66F, 1.00F, 16,
                        0.302F, 0.392F, 0.325F, 0.94F);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.060D, 0.0D), flatX, flatZ, rotR * 0.88F, rotR, 24,
                        0.204F, 0.800F, 0.282F, 0.86F * (1.0F - rotWave));

                renderCylinderFrustum(m, vc, 0.02F, 0.50F, 0.60F, 0.42F, 7, 0.0F, 0.302F, 0.392F, 0.325F, 0.96F);
                renderOpenFrustumWall(m, vc, 0.48F, 1.56F, 0.42F, 0.28F, 7, spin * 0.22F,
                        0.302F, 0.392F, 0.325F, 0.68F);

                Vec3 blightHeart = new Vec3(0.0D, 1.26D, 0.0D);
                renderOctahedron(m, vc, blightHeart, 0.24F * pulse, 0.34F * pulse, spin * 0.50F,
                        0.204F, 0.800F, 0.282F, 0.96F);
                renderOctahedron(m, vc, blightHeart, 0.14F * pulse, 0.22F * pulse, -spin * 0.70F,
                        0.902F, 0.875F, 0.812F, 0.98F);

                // 4 orbiting piercing skeletal thorn darts
                for (int d = 0; d < 4; d++) {
                    float dAng = spin * 1.05F + (float) (Math.PI * 0.5D * d);
                    double dy = 1.24D + 0.12D * Mth.sin(age * 0.20F + d * 1.57F);
                    Vec3 dartPos = new Vec3(Mth.cos(dAng) * 0.78D, dy, Mth.sin(dAng) * 0.78D);
                    boolean bone = (d % 2 == 0);
                    renderOctahedron(m, vc, dartPos, 0.085F, 0.18F, dAng,
                            bone ? 0.902F : 0.204F,
                            bone ? 0.875F : 0.800F,
                            bone ? 0.812F : 0.282F,
                            0.96F);
                }
            } else if (state.materia == EssenceType.ASTRAL) {
                // Section 6.28 Pulsar Singularity Spire Sentry (#41304E / #FFE600): cosmic impactite plinth,
                // levitating pulsar singularity core, gyroscopic #FFE600 electric rings & 4 homing ball-lightning orbs!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float pulse = 0.90F + 0.12F * Mth.sin(age * 0.24F);
                float pullWave = 1.0F - ((age * 0.045F) % 1.0F);
                float pullR = 0.65F + pullWave * 2.10F;
                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ, 0.68F, 1.02F, 24,
                        0.255F, 0.188F, 0.306F, 0.94F);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.060D, 0.0D), flatX, flatZ, pullR * 0.88F, pullR, 28,
                        1.000F, 0.902F, 0.000F, 0.88F * (1.0F - pullWave));

                renderCylinderFrustum(m, vc, 0.02F, 0.48F, 0.58F, 0.38F, 8, 0.0F, 0.255F, 0.188F, 0.306F, 0.96F);
                renderOpenFrustumWall(m, vc, 0.46F, 1.56F, 0.38F, 0.24F, 8, spin * 0.30F,
                        0.255F, 0.188F, 0.306F, 0.72F);

                Vec3 pulsarCore = new Vec3(0.0D, 1.32D, 0.0D);
                renderOctahedron(m, vc, pulsarCore, 0.24F * pulse, 0.30F * pulse, spin * 0.65F,
                        0.255F, 0.188F, 0.306F, 0.98F);
                renderOctahedron(m, vc, pulsarCore, 0.15F * pulse, 0.20F * pulse, -spin * 0.95F,
                        1.000F, 0.902F, 0.000F, 0.99F);

                Vec3 gyroU = new Vec3(Mth.cos(spin * 0.7F), 0.32D, Mth.sin(spin * 0.7F)).normalize();
                Vec3 gyroV = new Vec3(-Mth.sin(spin * 0.7F), 0.84D, Mth.cos(spin * 0.7F)).normalize();
                renderLocalRing(m, vc, pulsarCore, gyroU, gyroV, 0.34F, 0.46F, 24,
                        1.000F, 0.902F, 0.000F, 0.95F);

                // 4 orbiting homing ball-lightning orbs
                for (int o = 0; o < 4; o++) {
                    float oAng = spin * 1.15F + (float) (Math.PI * 0.5D * o);
                    double oy = 1.32D + 0.14D * Mth.sin(age * 0.24F + o * 1.57F);
                    Vec3 orbPos = new Vec3(Mth.cos(oAng) * 0.80D, oy, Mth.sin(oAng) * 0.80D);
                    renderOctahedron(m, vc, orbPos, 0.11F, 0.11F, oAng, 1.000F, 0.902F, 0.000F, 0.98F);
                    renderOctahedron(m, vc, orbPos, 0.15F, 0.15F, -oAng, 0.255F, 0.188F, 0.306F, 0.68F);
                }
            } else if (state.materia == EssenceType.SOULFIRE) {
                // Section 6.29 Phantasmal Pyre Spire Sentry (#1DF0E5 / #0D6469): soul-ember brazier plinth,
                // roaring cyan soulfire beacon core, 8m projectile-burning ward ring & 4 homing screaming soul skulls!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float pulse = 0.90F + 0.14F * Mth.sin(age * 0.26F);
                float burnWave = (age * 0.042F) % 1.0F;
                float burnR = 0.68F + burnWave * 1.95F;
                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ, 0.68F, 1.02F, 22,
                        0.051F, 0.392F, 0.412F, 0.94F);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.060D, 0.0D), flatX, flatZ, burnR * 0.88F, burnR, 26,
                        0.114F, 0.941F, 0.898F, 0.88F * (1.0F - burnWave));

                renderCylinderFrustum(m, vc, 0.02F, 0.50F, 0.60F, 0.42F, 8, 0.0F, 0.051F, 0.392F, 0.412F, 0.96F);
                renderOpenFrustumWall(m, vc, 0.48F, 1.56F, 0.42F, 0.28F, 8, -spin * 0.26F,
                        0.114F, 0.941F, 0.898F, 0.66F);

                Vec3 pyreCore = new Vec3(0.0D, 1.30D, 0.0D);
                renderOctahedron(m, vc, pyreCore, 0.26F * pulse, 0.38F * pulse, spin * 0.55F,
                        0.114F, 0.941F, 0.898F, 0.96F);
                renderOctahedron(m, vc, pyreCore, 0.15F * pulse, 0.24F * pulse, -spin * 0.80F,
                        0.88F, 1.00F, 0.99F, 0.99F);

                // 4 orbiting screaming soul skulls
                for (int s = 0; s < 4; s++) {
                    float sAng = spin * 1.05F + (float) (Math.PI * 0.5D * s);
                    double sy = 1.28D + 0.12D * Mth.sin(age * 0.22F + s * 1.57F);
                    Vec3 skullPos = new Vec3(Mth.cos(sAng) * 0.80D, sy, Mth.sin(sAng) * 0.80D);
                    renderOctahedron(m, vc, skullPos, 0.11F, 0.14F, sAng, 0.114F, 0.941F, 0.898F, 0.96F);
                    renderOctahedron(m, vc, skullPos, 0.07F, 0.09F, -sAng, 0.051F, 0.392F, 0.412F, 0.94F);
                }
            } else if (state.materia == EssenceType.VAPOR) {
                // Section 6.30 Steam-Vent Obelisk Sentry (#E0F7FA / #30A9B0): aquatic boiler plinth,
                // pressurized steam-vent column, 8m expanding blinding smoke ring & 4 orbiting steam spheres!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float pulse = 0.90F + 0.12F * Mth.sin(age * 0.28F);
                float steamWave = (age * 0.045F) % 1.0F;
                float steamR = 0.70F + steamWave * 2.15F;
                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ, 0.68F, 1.00F, 24,
                        0.188F, 0.663F, 0.690F, 0.94F);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.065D, 0.0D), flatX, flatZ, steamR * 0.86F, steamR, 28,
                        0.878F, 0.969F, 0.980F, 0.86F * (1.0F - steamWave));

                renderCylinderFrustum(m, vc, 0.02F, 0.52F, 0.60F, 0.44F, 8, 0.0F, 0.188F, 0.663F, 0.690F, 0.96F);
                renderOpenFrustumWall(m, vc, 0.50F, 1.62F, 0.42F, 0.30F, 8, spin * 0.24F,
                        0.878F, 0.969F, 0.980F, 0.70F);

                Vec3 boilerCore = new Vec3(0.0D, 1.30D, 0.0D);
                renderOctahedron(m, vc, boilerCore, 0.24F * pulse, 0.30F * pulse, spin * 0.50F,
                        0.188F, 0.663F, 0.690F, 0.96F);
                renderLocalRing(m, vc, new Vec3(0.0D, 1.64D, 0.0D), flatX, flatZ, 0.22F, 0.40F, 20,
                        0.878F, 0.969F, 0.980F, 0.94F);

                for (int s = 0; s < 4; s++) {
                    float sAng = spin * 1.10F + (float) (Math.PI * 0.5D * s);
                    double sy = 1.28D + 0.12D * Mth.sin(age * 0.24F + s * 1.57F);
                    Vec3 orbPos = new Vec3(Mth.cos(sAng) * 0.78D, sy, Mth.sin(sAng) * 0.78D);
                    renderOctahedron(m, vc, orbPos, 0.11F, 0.11F, sAng, 0.878F, 0.969F, 0.980F, 0.95F);
                    renderOctahedron(m, vc, orbPos, 0.07F, 0.07F, -sAng, 0.188F, 0.663F, 0.690F, 0.96F);
                }
            } else if (state.materia == EssenceType.NULL_R) {
                // Section 6.31 Event Horizon Spire (#FFFFFF / #41304E): anchored antimatter obelisk firing piercing lances & 8m pulling blinding aura!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float pulse = 0.90F + 0.12F * Mth.sin(age * 0.28F);
                float gravWave = 1.0F - ((age * 0.045F) % 1.0F);
                float pullR = 0.65F + gravWave * 1.85F;

                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ, 0.66F, 1.02F, 26,
                        0.255F, 0.188F, 0.306F, 0.96F);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.065D, 0.0D), flatX, flatZ, pullR * 0.88F, pullR, 28,
                        1.000F, 1.000F, 1.000F, 0.90F * (1.0F - gravWave * 0.65F));

                renderCylinderFrustum(m, vc, 0.02F, 0.56F, 0.58F, 0.40F, 8, 0.0F, 0.255F, 0.188F, 0.306F, 0.96F);
                renderOpenFrustumWall(m, vc, 0.54F, 1.72F, 0.38F, 0.24F, 8, spin * 0.28F,
                        1.000F, 1.000F, 1.000F, 0.78F);

                Vec3 whiteHoleCore = new Vec3(0.0D, 1.38D, 0.0D);
                renderOctahedron(m, vc, whiteHoleCore, 0.22F * pulse, 0.32F * pulse, spin * 0.55F,
                        1.000F, 1.000F, 1.000F, 0.99F);
                renderOctahedron(m, vc, whiteHoleCore, 0.30F, 0.40F, -spin * 0.38F,
                        0.255F, 0.188F, 0.306F, 0.72F);
                renderLocalRing(m, vc, new Vec3(0.0D, 1.38D, 0.0D), flatX, flatZ, 0.38F, 0.52F, 24,
                        1.000F, 1.000F, 1.000F, 0.95F);

                for (int l = 0; l < 4; l++) {
                    float lAng = -spin * 1.15F + (float) (Math.PI * 0.5D * l);
                    double ly = 1.36D + 0.14D * Mth.sin(age * 0.26F + l * 1.57F);
                    Vec3 lancePos = new Vec3(Mth.cos(lAng) * 0.80D, ly, Mth.sin(lAng) * 0.80D);
                    renderOctahedron(m, vc, lancePos, 0.08F, 0.22F, lAng, 1.000F, 1.000F, 1.000F, 0.98F);
                    renderOctahedron(m, vc, lancePos, 0.12F, 0.12F, -lAng, 0.255F, 0.188F, 0.306F, 0.92F);
                }
            } else if (state.materia == EssenceType.NULL_U) {
                // Section 6.32 Oblivion Singularity Spire (#110B17 / #41304E): silent abyssal obelisk firing pulling gravity darts & 8m silencing aura!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float pulse = 0.92F + 0.10F * Mth.sin(age * 0.26F);
                float sinkWave = 1.0F - ((age * 0.042F) % 1.0F);
                float pullR = 0.68F + sinkWave * 1.88F;

                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ, 0.66F, 1.04F, 26,
                        0.067F, 0.043F, 0.090F, 0.98F);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.065D, 0.0D), flatX, flatZ, pullR * 0.86F, pullR, 28,
                        0.255F, 0.188F, 0.306F, 0.92F * (1.0F - sinkWave * 0.60F));

                renderCylinderFrustum(m, vc, 0.02F, 0.56F, 0.58F, 0.40F, 8, 0.0F, 0.067F, 0.043F, 0.090F, 0.98F);
                renderOpenFrustumWall(m, vc, 0.54F, 1.72F, 0.38F, 0.24F, 8, -spin * 0.26F,
                        0.255F, 0.188F, 0.306F, 0.84F);

                Vec3 blackHoleCore = new Vec3(0.0D, 1.38D, 0.0D);
                renderOctahedron(m, vc, blackHoleCore, 0.24F * pulse, 0.34F * pulse, -spin * 0.50F,
                        0.067F, 0.043F, 0.090F, 0.99F);
                renderOctahedron(m, vc, blackHoleCore, 0.31F, 0.42F, spin * 0.36F,
                        0.255F, 0.188F, 0.306F, 0.78F);
                renderLocalRing(m, vc, new Vec3(0.0D, 1.38D, 0.0D), flatX, flatZ, 0.36F, 0.54F, 24,
                        0.255F, 0.188F, 0.306F, 0.95F);

                for (int d = 0; d < 4; d++) {
                    float dAng = -spin * 1.12F + (float) (Math.PI * 0.5D * d);
                    double dy = 1.36D + 0.14D * Mth.sin(age * 0.24F + d * 1.57F);
                    Vec3 dartPos = new Vec3(Mth.cos(dAng) * 0.80D, dy, Mth.sin(dAng) * 0.80D);
                    renderOctahedron(m, vc, dartPos, 0.085F, 0.22F, dAng, 0.067F, 0.043F, 0.090F, 0.98F);
                    renderOctahedron(m, vc, dartPos, 0.12F, 0.12F, -dAng, 0.255F, 0.188F, 0.306F, 0.94F);
                }
            } else if (state.materia == EssenceType.DAWN) {
                // Section 6.33 Sol-Zephyr Spire (#FFEA78 / #A88654): anchored daybreak spire firing 3-pierce javelins & 8m Slow-Falling/Speed aura!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float pulse = 0.92F + 0.10F * Mth.sin(age * 0.28F);
                float windWave = (age * 0.045F) % 1.0F;
                float gustR = 0.68F + windWave * 1.95F;

                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ, 0.66F, 1.02F, 26,
                        0.659F, 0.525F, 0.329F, 0.94F);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.065D, 0.0D), flatX, flatZ, gustR * 0.88F, gustR, 28,
                        1.000F, 0.918F, 0.471F, 0.90F * (1.0F - windWave * 0.65F));

                renderCylinderFrustum(m, vc, 0.02F, 0.56F, 0.58F, 0.40F, 8, 0.0F, 0.659F, 0.525F, 0.329F, 0.96F);
                renderOpenFrustumWall(m, vc, 0.54F, 1.72F, 0.38F, 0.24F, 8, spin * 0.28F,
                        1.000F, 0.918F, 0.471F, 0.78F);

                Vec3 sunCore = new Vec3(0.0D, 1.40D, 0.0D);
                renderOctahedron(m, vc, sunCore, 0.24F * pulse, 0.34F * pulse, spin * 0.55F,
                        1.000F, 0.918F, 0.471F, 0.99F);
                renderLocalRing(m, vc, new Vec3(0.0D, 1.40D, 0.0D), flatX, flatZ, 0.38F, 0.54F, 24,
                        0.659F, 0.525F, 0.329F, 0.94F);

                for (int j = 0; j < 4; j++) {
                    float jAng = spin * 1.15F + (float) (Math.PI * 0.5D * j);
                    double jy = 1.38D + 0.14D * Mth.sin(age * 0.26F + j * 1.57F);
                    Vec3 javPos = new Vec3(Mth.cos(jAng) * 0.82D, jy, Mth.sin(jAng) * 0.82D);
                    renderOctahedron(m, vc, javPos, 0.08F, 0.24F, jAng, 1.000F, 0.918F, 0.471F, 0.98F);
                    renderOctahedron(m, vc, javPos, 0.12F, 0.11F, -jAng, 0.659F, 0.525F, 0.329F, 0.92F);
                }
            } else if (state.materia == EssenceType.ABYSS) {
                // Section 6.34 Trench Spire (#211A21 / #225C7C): anchored abyssal spire creating an 8m whirlpool vortex!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float pulse = 0.92F + 0.10F * Mth.sin(age * 0.26F);
                float swirlWave = 1.0F - ((age * 0.045F) % 1.0F);
                float whirlR = 0.64F + swirlWave * 2.15F;

                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ, 0.68F, 1.08F, 26,
                        0.129F, 0.102F, 0.129F, 0.96F);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.065D, 0.0D), flatX, flatZ, whirlR * 0.86F, whirlR, 28,
                        0.133F, 0.361F, 0.486F, 0.92F * (1.0F - swirlWave * 0.55F));

                renderCylinderFrustum(m, vc, 0.02F, 0.60F, 0.60F, 0.42F, 8, 0.0F, 0.129F, 0.102F, 0.129F, 0.98F);
                renderOpenFrustumWall(m, vc, 0.56F, 1.72F, 0.40F, 0.26F, 8, -spin * 0.32F,
                        0.133F, 0.361F, 0.486F, 0.80F);

                Vec3 trenchEye = new Vec3(0.0D, 1.38D, 0.0D);
                renderOctahedron(m, vc, trenchEye, 0.24F * pulse, 0.30F * pulse, -spin * 0.50F,
                        0.129F, 0.102F, 0.129F, 0.99F);
                renderLocalRing(m, vc, trenchEye, flatX, flatZ, 0.36F, 0.54F, 24,
                        0.133F, 0.361F, 0.486F, 0.95F);

                for (int o = 0; o < 4; o++) {
                    float oAng = -spin * 1.10F + (float) (Math.PI * 0.5D * o);
                    double oy = 1.34D + 0.14D * Mth.sin(age * 0.24F + o * 1.57F);
                    Vec3 orbPos = new Vec3(Mth.cos(oAng) * 0.80D, oy, Mth.sin(oAng) * 0.80D);
                    renderOctahedron(m, vc, orbPos, 0.12F, 0.14F, oAng, 0.129F, 0.102F, 0.129F, 0.99F);
                    renderOctahedron(m, vc, orbPos, 0.16F, 0.18F, -oAng, 0.133F, 0.361F, 0.486F, 0.82F);
                }
            } else if (state.materia == EssenceType.AEGIS) {
                // Section 6.35 Citadel Fortress Spire (#FFEA78 / #773214): octagonal granite tower & +6 Armor holy aura!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float pulse = 0.93F + 0.08F * Mth.sin(age * 0.24F);
                float wardWave = (age * 0.040F) % 1.0F;
                float wardR = 0.72F + wardWave * 1.95F;

                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ, 0.70F, 1.12F, 8,
                        0.467F, 0.196F, 0.078F, 0.97F);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.065D, 0.0D), flatX, flatZ, wardR * 0.88F, wardR, 24,
                        1.000F, 0.918F, 0.471F, 0.92F * (1.0F - wardWave * 0.60F));

                renderCylinderFrustum(m, vc, 0.02F, 0.72F, 0.64F, 0.50F, 8, 0.0F, 0.467F, 0.196F, 0.078F, 0.98F);
                renderCylinderFrustum(m, vc, 0.72F, 1.66F, 0.46F, 0.38F, 8, 0.39F, 1.000F, 0.918F, 0.471F, 0.86F);

                Vec3 citadelCrown = new Vec3(0.0D, 1.88D, 0.0D);
                renderOctahedron(m, vc, citadelCrown, 0.24F * pulse, 0.30F * pulse, spin * 0.35F,
                        1.000F, 0.918F, 0.471F, 0.99F);
                renderLocalRing(m, vc, citadelCrown, flatX, flatZ, 0.38F, 0.56F, 24,
                        1.000F, 0.918F, 0.471F, 0.96F);

                for (int p = 0; p < 4; p++) {
                    float pAng = spin * 0.85F + (float) (Math.PI * 0.5D * p);
                    Vec3 shCenter = new Vec3(Mth.cos(pAng) * 0.84D, 1.24D, Mth.sin(pAng) * 0.84D);
                    Vec3 tang = new Vec3(-Mth.sin(pAng) * 0.18D, 0.0D, Mth.cos(pAng) * 0.18D);
                    Vec3 up = new Vec3(0.0D, 0.24D, 0.0D);
                    emitDoubleSidedQuad(m, vc, shCenter.subtract(tang).add(up), shCenter.add(tang).add(up),
                            shCenter.add(tang).subtract(up), shCenter.subtract(tang).subtract(up),
                            0.467F, 0.196F, 0.078F, 0.98F);
                    renderOctahedron(m, vc, shCenter, 0.08F, 0.10F, pAng, 1.000F, 0.918F, 0.471F, 0.99F);
                }
            } else if (state.materia == EssenceType.AURORA) {
                // Section 6.36 Prismatic Aurora Spire (#FFEA78 / #98DCF2): crystalline polar ice spire & projectile-deflecting aurora curtains!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float pulse = 0.93F + 0.09F * Mth.sin(age * 0.26F);
                float frostWave = (age * 0.044F) % 1.0F;
                float frostR = 0.70F + frostWave * 2.05F;

                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ, 0.68F, 1.10F, 6,
                        0.596F, 0.863F, 0.949F, 0.96F);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.065D, 0.0D), flatX, flatZ, frostR * 0.88F, frostR, 26,
                        1.000F, 0.918F, 0.471F, 0.92F * (1.0F - frostWave * 0.60F));

                renderCylinderFrustum(m, vc, 0.02F, 0.78F, 0.58F, 0.42F, 6, 0.0F, 0.596F, 0.863F, 0.949F, 0.94F);
                renderOpenFrustumWall(m, vc, 0.74F, 1.72F, 0.44F, 0.28F, 6, spin * 0.30F,
                        1.000F, 0.918F, 0.471F, 0.78F);

                Vec3 borealisCrown = new Vec3(0.0D, 1.88D, 0.0D);
                renderOctahedron(m, vc, borealisCrown, 0.22F * pulse, 0.34F * pulse, spin * 0.45F,
                        0.596F, 0.863F, 0.949F, 0.98F);
                renderLocalRing(m, vc, borealisCrown, flatX, flatZ, 0.36F, 0.54F, 24,
                        1.000F, 0.918F, 0.471F, 0.97F);

                for (int c = 0; c < 4; c++) {
                    float cAng = spin * 0.95F + (float) (Math.PI * 0.5D * c);
                    double cy = 1.24D + 0.14D * Mth.sin(age * 0.24F + c * 1.57F);
                    Vec3 cCenter = new Vec3(Mth.cos(cAng) * 0.82D, cy, Mth.sin(cAng) * 0.82D);
                    Vec3 tang = new Vec3(-Mth.sin(cAng) * 0.16D, 0.0D, Mth.cos(cAng) * 0.16D);
                    Vec3 up = new Vec3(0.0D, 0.32D, 0.0D);
                    boolean gold = (c % 2 == 0);
                    emitDoubleSidedQuad(m, vc, cCenter.subtract(tang).add(up), cCenter.add(tang).add(up),
                            cCenter.add(tang).subtract(up), cCenter.subtract(tang).subtract(up),
                            gold ? 1.000F : 0.596F,
                            gold ? 0.918F : 0.863F,
                            gold ? 0.471F : 0.949F,
                            0.88F);
                }
            } else if (state.materia == EssenceType.MIRAGE) {
                // Section 6.37 Fata Morgana Obelisk (#FFEA78 / #C2B28F): 4-sided desert sandstone obelisk,
                // shimmering sunlit mirage capstone, 12m illusion waves & 4 orbiting refractive mirror plates!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float obRot = (float) (Math.PI * 0.25D);
                float hazeWave = (age * 0.045F) % 1.0F;
                float hazeR = 0.72F + hazeWave * 2.25F;

                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ, 0.68F, 1.08F, 4,
                        0.761F, 0.698F, 0.561F, 0.96F);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.065D, 0.0D), flatX, flatZ, hazeR * 0.88F, hazeR, 26,
                        1.000F, 0.918F, 0.471F, 0.90F * (1.0F - hazeWave * 0.62F));

                renderCylinderFrustum(m, vc, 0.02F, 0.42F, 0.64F, 0.48F, 4, obRot, 0.761F, 0.698F, 0.561F, 0.97F);
                renderCylinderFrustum(m, vc, 0.42F, 1.62F, 0.46F, 0.26F, 4, obRot, 0.761F, 0.698F, 0.561F, 0.94F);

                Vec3 mirageApex = new Vec3(0.0D, 1.86D, 0.0D);
                renderOctahedron(m, vc, mirageApex, 0.24F, 0.30F, spin * 0.45F, 1.000F, 0.918F, 0.471F, 0.99F);
                renderLocalRing(m, vc, mirageApex, flatX, flatZ, 0.36F, 0.54F, 24,
                        1.000F, 0.918F, 0.471F, 0.95F);

                for (int p = 0; p < 4; p++) {
                    float pAng = -spin * 1.05F + (float) (Math.PI * 0.5D * p);
                    double py = 1.22D + 0.12D * Mth.sin(age * 0.24F + p * 1.57F);
                    Vec3 mirrorPos = new Vec3(Mth.cos(pAng) * 0.82D, py, Mth.sin(pAng) * 0.82D);
                    Vec3 tang = new Vec3(-Mth.sin(pAng) * 0.16D, 0.0D, Mth.cos(pAng) * 0.16D);
                    Vec3 up = new Vec3(0.0D, 0.28D, 0.0D);
                    boolean gold = (p % 2 == 0);
                    emitDoubleSidedQuad(m, vc, mirrorPos.subtract(tang).add(up), mirrorPos.add(tang).add(up),
                            mirrorPos.add(tang).subtract(up), mirrorPos.subtract(tang).subtract(up),
                            gold ? 1.000F : 0.761F,
                            gold ? 0.918F : 0.698F,
                            gold ? 0.471F : 0.561F,
                            0.86F);
                }
            } else if (state.materia == EssenceType.WRAITH) {
                // Section 6.38 Banshee Wail Obelisk (#41304E / #4D6453): abyssal indigo & necrotic moss obelisk,
                // screaming spectral skull apex, & expanding 8m ultrasonic banshee shriek rings!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float shriekWave = (age * 0.065F) % 1.0F;
                float shriekR = 0.65F + shriekWave * 2.15F;

                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ, 0.64F, 1.04F, 8,
                        0.255F, 0.188F, 0.306F, 0.96F);
                renderLocalRing(m, vc, new Vec3(0.0D, 1.24D, 0.0D), flatX, flatZ, shriekR * 0.88F, shriekR, 24,
                        0.302F, 0.392F, 0.325F, 0.90F * (1.0F - shriekWave * 0.68F));

                renderCylinderFrustum(m, vc, 0.02F, 0.44F, 0.60F, 0.44F, 6, 0.0F, 0.255F, 0.188F, 0.306F, 0.96F);
                renderCylinderFrustum(m, vc, 0.44F, 1.58F, 0.42F, 0.24F, 6, 0.52F, 0.302F, 0.392F, 0.325F, 0.90F);

                Vec3 skullApex = new Vec3(0.0D, 1.84D + 0.06D * Mth.sin(age * 0.18F), 0.0D);
                renderOctahedron(m, vc, skullApex, 0.26F, 0.32F, spin * 0.40F, 0.302F, 0.392F, 0.325F, 0.96F);
                renderOctahedron(m, vc, skullApex, 0.18F, 0.24F, -spin * 0.55F, 0.255F, 0.188F, 0.306F, 0.98F);
            } else if (state.materia == EssenceType.BARROW) {
                // Section 6.39 Cairn Beacon (#773214 / #4D6453): stacked megalithic cairn stones with mossy carvings,
                // petrified bone capstone, & 8m necrotic burial-dust pulse ring!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float dustWave = (age * 0.045F) % 1.0F;
                float dustR = 0.72F + dustWave * 2.15F;

                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ, 0.70F, 1.12F, 8,
                        0.467F, 0.196F, 0.078F, 0.97F);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.070D, 0.0D), flatX, flatZ, dustR * 0.86F, dustR, 24,
                        0.302F, 0.392F, 0.325F, 0.90F * (1.0F - dustWave * 0.65F));

                // 3 stacked megalithic cairn tiers (#773214 granite + #4D6453 necrotic moss)
                renderCylinderFrustum(m, vc, 0.02F, 0.56F, 0.72F, 0.58F, 6, 0.0F, 0.467F, 0.196F, 0.078F, 0.98F);
                renderCylinderFrustum(m, vc, 0.56F, 1.16F, 0.56F, 0.42F, 6, 0.52F, 0.302F, 0.392F, 0.325F, 0.95F);
                renderCylinderFrustum(m, vc, 1.16F, 1.66F, 0.40F, 0.26F, 6, 0.0F, 0.467F, 0.196F, 0.078F, 0.96F);

                Vec3 boneCrown = new Vec3(0.0D, 1.90D + 0.04D * Mth.sin(age * 0.14F), 0.0D);
                renderOctahedron(m, vc, boneCrown, 0.22F, 0.30F, spin * 0.35F, 0.902F, 0.875F, 0.812F, 0.98F);
                renderLocalRing(m, vc, boneCrown, flatX, flatZ, 0.34F, 0.48F, 16, 0.302F, 0.392F, 0.325F, 0.92F);
            } else if (state.materia == EssenceType.PYRE) {
                // Section 6.40 Cremation Pyre Spire (#773214 / #FF4500): stacked burning cedar & volcanic coal hearth spire,
                // roaring wildfire brazier core, & 8m wildfire ember pulse ring!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float fireWave = (age * 0.055F) % 1.0F;
                float fireR = 0.72F + fireWave * 2.35F;
                float pulse = 0.92F + 0.10F * Mth.sin(age * 0.32F);

                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ, 0.70F, 1.10F, 8,
                        0.467F, 0.196F, 0.078F, 0.98F);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.070D, 0.0D), flatX, flatZ, fireR * 0.86F, fireR, 24,
                        1.000F, 0.271F, 0.000F, 0.92F * (1.0F - fireWave * 0.65F));

                renderCylinderFrustum(m, vc, 0.02F, 0.58F, 0.68F, 0.50F, 6, 0.0F, 0.467F, 0.196F, 0.078F, 0.98F);
                renderOpenFrustumWall(m, vc, 0.54F, 1.66F, 0.48F, 0.30F, 6, spin * 0.36F,
                        1.000F, 0.271F, 0.000F, 0.86F);

                Vec3 pyreCore = new Vec3(0.0D, 1.48D, 0.0D);
                renderOctahedron(m, vc, pyreCore, 0.25F * pulse, 0.36F * pulse, spin * 0.60F,
                        1.000F, 0.271F, 0.000F, 0.99F);
                renderOctahedron(m, vc, pyreCore, 0.16F * pulse, 0.24F * pulse, -spin * 0.85F,
                        1.000F, 0.820F, 0.361F, 0.99F);

                for (int j = 0; j < 4; j++) {
                    float jAng = spin * 1.18F + (float) (Math.PI * 0.5D * j);
                    double jy = 1.42D + 0.12D * Mth.sin(age * 0.28F + j * 1.57F);
                    Vec3 jetPos = new Vec3(Mth.cos(jAng) * 0.78D, jy, Mth.sin(jAng) * 0.78D);
                    renderOctahedron(m, vc, jetPos, 0.09F, 0.22F, jAng, 1.000F, 0.271F, 0.000F, 0.97F);
                    renderOctahedron(m, vc, jetPos, 0.13F, 0.11F, -jAng, 0.467F, 0.196F, 0.078F, 0.94F);
                }
            } else if (state.materia == EssenceType.PENUMBRA) {
                // Section 6.41 Twilight Corona Citadel (#FFEA78 / #281E3C): anchored floating twilight prism,
                // intersecting coronal ellipses, 8m bastion ring, & twin light/shadow suppression lances!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float pulse = 0.92F + 0.10F * Mth.sin(age * 0.28F);
                float pulseWave = (age * 0.05F) % 1.0F;
                float bastionR = 0.72F + pulseWave * 2.30F;

                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ, 0.70F, 1.12F, 24,
                        0.157F, 0.118F, 0.235F, 0.96F);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.070D, 0.0D), flatX, flatZ, bastionR * 0.86F, bastionR, 28,
                        1.000F, 0.918F, 0.471F, 0.92F * (1.0F - pulseWave * 0.65F));

                renderCylinderFrustum(m, vc, 0.02F, 0.56F, 0.60F, 0.40F, 8, 0.0F, 0.157F, 0.118F, 0.235F, 0.98F);
                renderOpenFrustumWall(m, vc, 0.54F, 1.72F, 0.38F, 0.24F, 8, spin * 0.34F,
                        1.000F, 0.918F, 0.471F, 0.82F);

                Vec3 prismCore = new Vec3(0.0D, 1.44D, 0.0D);
                renderOctahedron(m, vc, prismCore, 0.26F * pulse, 0.42F * pulse, -spin * 0.55F,
                        0.157F, 0.118F, 0.235F, 0.99F);
                renderLocalRing(m, vc, prismCore, flatX, flatZ, 0.36F, 0.54F, 24,
                        1.000F, 0.918F, 0.471F, 0.97F);

                // Twin counter-orbiting light (#FFEA78) & shadow (#281E3C) suppression lances
                for (int l = 0; l < 4; l++) {
                    float lAng = spin * 1.25F + (float) (Math.PI * 0.5D * l);
                    boolean corona = (l % 2 == 0);
                    Vec3 lPos = new Vec3(Mth.cos(lAng) * 0.82D, 1.42D + 0.12D * Mth.sin(age * 0.26F + l), Mth.sin(lAng) * 0.82D);
                    renderOctahedron(m, vc, lPos, 0.085F, 0.26F, lAng,
                            corona ? 1.000F : 0.157F,
                            corona ? 0.918F : 0.118F,
                            corona ? 0.471F : 0.235F,
                            0.98F);
                }
            } else if (state.materia == EssenceType.RIME) {
                // Section 6.42 Hoarfrost Glacial Citadel Spire (#6395B1 / #DCF0FF): monolithic jagged permafrost tower,
                // 8m cryogenic frost-burst ring, & 4 orbiting 3-pierce needle-ice bolts!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float pulse = 0.92F + 0.10F * Mth.sin(age * 0.26F);
                float frostWave = (age * 0.05F) % 1.0F;
                float frostR = 0.72F + frostWave * 2.30F;

                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ, 0.72F, 1.14F, 6,
                        0.388F, 0.584F, 0.694F, 0.97F);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.070D, 0.0D), flatX, flatZ, frostR * 0.86F, frostR, 24,
                        0.863F, 0.941F, 1.000F, 0.92F * (1.0F - frostWave * 0.65F));

                renderCylinderFrustum(m, vc, 0.02F, 0.62F, 0.66F, 0.48F, 6, 0.0F, 0.388F, 0.584F, 0.694F, 0.98F);
                renderOpenFrustumWall(m, vc, 0.58F, 1.78F, 0.46F, 0.26F, 6, spin * 0.28F,
                        0.863F, 0.941F, 1.000F, 0.86F);

                Vec3 rimeApex = new Vec3(0.0D, 1.54D, 0.0D);
                renderOctahedron(m, vc, rimeApex, 0.24F * pulse, 0.44F * pulse, spin * 0.45F,
                        0.863F, 0.941F, 1.000F, 0.99F);
                renderLocalRing(m, vc, rimeApex, flatX, flatZ, 0.36F, 0.52F, 18,
                        0.388F, 0.584F, 0.694F, 0.95F);

                for (int n = 0; n < 4; n++) {
                    float nAng = -spin * 1.25F + (float) (Math.PI * 0.5D * n);
                    Vec3 nPos = new Vec3(Mth.cos(nAng) * 0.84D, 1.44D + 0.12D * Mth.sin(age * 0.24F + n), Mth.sin(nAng) * 0.84D);
                    renderOctahedron(m, vc, nPos, 0.075F, 0.28F, nAng, 0.863F, 0.941F, 1.000F, 0.98F);
                    renderOctahedron(m, vc, nPos, 0.11F, 0.14F, -nAng, 0.388F, 0.584F, 0.694F, 0.92F);
                }
            } else if (state.materia == EssenceType.SPRING) {
                // Section 6.43 Aquifer Springwater Font Spire (#FF6B9D / #64C8FF): monolithic living marble &
                // bubbling aquifer fountain, 8m restorative surge ring, & 5 orbiting lotus blossom water lances!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float pulse = 0.92F + 0.10F * Mth.sin(age * 0.26F);
                float surgeWave = (age * 0.05F) % 1.0F;
                float surgeR = 0.72F + surgeWave * 2.30F;

                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ, 0.72F, 1.14F, 24,
                        0.392F, 0.784F, 1.000F, 0.95F);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.070D, 0.0D), flatX, flatZ, surgeR * 0.86F, surgeR, 28,
                        1.000F, 0.420F, 0.616F, 0.92F * (1.0F - surgeWave * 0.65F));

                renderCylinderFrustum(m, vc, 0.02F, 0.58F, 0.64F, 0.48F, 12, 0.0F, 0.950F, 0.960F, 0.980F, 0.96F);
                renderOpenFrustumWall(m, vc, 0.54F, 1.72F, 0.46F, 0.28F, 12, spin * 0.32F,
                        0.392F, 0.784F, 1.000F, 0.84F);

                Vec3 lotusApex = new Vec3(0.0D, 1.50D, 0.0D);
                renderOctahedron(m, vc, lotusApex, 0.24F * pulse, 0.38F * pulse, spin * 0.45F,
                        1.000F, 0.420F, 0.616F, 0.98F);
                renderLocalRing(m, vc, lotusApex, flatX, flatZ, 0.36F, 0.54F, 20,
                        0.392F, 0.784F, 1.000F, 0.94F);

                for (int l = 0; l < 5; l++) {
                    float lAng = spin * 1.18F + (float) (Math.PI * 2.0D * l / 5.0D);
                    boolean pink = (l % 2 == 0);
                    Vec3 lPos = new Vec3(Mth.cos(lAng) * 0.84D, 1.42D + 0.12D * Mth.sin(age * 0.24F + l), Mth.sin(lAng) * 0.84D);
                    renderOctahedron(m, vc, lPos, 0.085F, 0.26F, lAng,
                            pink ? 1.000F : 0.392F,
                            pink ? 0.420F : 0.784F,
                            pink ? 0.616F : 1.000F,
                            0.97F);
                }
            } else if (state.materia == EssenceType.STATIC) {
                // Section 6.44 Dielectric Tesla Citadel Spire (#FFE600 / #878DA2): anchored monolithic galvanized slate
                // pylon, toroidal high-voltage discharge crown, 8m EMP pulse ring & 4 orbiting ball-lightning capacitors!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float pulse = 0.92F + 0.12F * Mth.sin(age * 0.40F);
                float empWave = (age * 0.05F) % 1.0F;
                float empR = 0.76F + empWave * 2.40F;

                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ, 0.72F, 1.14F, 8,
                        0.529F, 0.553F, 0.635F, 0.96F);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.068D, 0.0D), flatX, flatZ, empR * 0.88F, empR, 24,
                        1.000F, 0.902F, 0.000F, 0.95F * (1.0F - empWave * 0.65F));

                renderCylinderFrustum(m, vc, 0.02F, 0.54F, 0.66F, 0.48F, 8, 0.0F, 0.529F, 0.553F, 0.635F, 0.97F);
                renderCylinderFrustum(m, vc, 0.54F, 1.66F, 0.42F, 0.26F, 8, 0.0F, 0.420F, 0.445F, 0.520F, 0.96F);

                for (int w = 0; w < 4; w++) {
                    double wy = 0.62D + w * 0.26D;
                    renderLocalRing(m, vc, new Vec3(0.0D, wy, 0.0D), flatX, flatZ, 0.30F, 0.44F, 18,
                            1.000F, 0.902F, 0.000F, 0.96F);
                }

                Vec3 teslaApex = new Vec3(0.0D, 1.78D, 0.0D);
                renderOctahedron(m, vc, teslaApex, 0.26F * pulse, 0.30F * pulse, spin * 0.65F,
                        1.000F, 0.902F, 0.000F, 0.99F);
                renderLocalRing(m, vc, teslaApex, flatX, flatZ, 0.38F, 0.58F, 22,
                        0.529F, 0.553F, 0.635F, 0.95F);

                for (int c = 0; c < 4; c++) {
                    float cAng = -spin * 1.45F + (float) (Math.PI * 0.5D * c);
                    Vec3 cPos = new Vec3(Mth.cos(cAng) * 0.86D, 1.46D + 0.12D * Mth.sin(age * 0.30F + c), Mth.sin(cAng) * 0.86D);
                    renderOctahedron(m, vc, cPos, 0.13F, 0.16F, cAng, 1.000F, 0.902F, 0.000F, 0.99F);
                    renderOctahedron(m, vc, cPos, 0.17F, 0.12F, -cAng, 0.529F, 0.553F, 0.635F, 0.88F);
                }
            } else if (state.materia == EssenceType.SYLVAN) {
                // Section 6.45 Petrified Monolith Sentry (#1A5923 / #773214 / #FFEA78): anchored monolithic stone-timber
                // totem, golden solar canopy crest, 8m root-wave ring & 4 orbiting petrified ironwood boulders!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float pulse = 0.94F + 0.08F * Mth.sin(age * 0.24F);
                float rootWave = (age * 0.045F) % 1.0F;
                float rootR = 0.74F + rootWave * 2.35F;

                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ, 0.72F, 1.16F, 12,
                        0.467F, 0.196F, 0.078F, 0.96F);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.068D, 0.0D), flatX, flatZ, rootR * 0.86F, rootR, 20,
                        0.102F, 0.349F, 0.137F, 0.94F * (1.0F - rootWave * 0.65F));

                renderCylinderFrustum(m, vc, 0.02F, 0.62F, 0.68F, 0.52F, 6, 0.0F, 0.467F, 0.196F, 0.078F, 0.98F);
                renderCylinderFrustum(m, vc, 0.62F, 1.68F, 0.48F, 0.34F, 6, 0.52F, 0.102F, 0.349F, 0.137F, 0.96F);

                Vec3 sunCrest = new Vec3(0.0D, 1.82D, 0.0D);
                renderOctahedron(m, vc, sunCrest, 0.26F * pulse, 0.34F * pulse, spin * 0.40F,
                        1.000F, 0.918F, 0.471F, 0.99F);
                renderLocalRing(m, vc, sunCrest, flatX, flatZ, 0.38F, 0.56F, 18,
                        0.102F, 0.349F, 0.137F, 0.94F);

                for (int b = 0; b < 4; b++) {
                    float bAng = -spin * 1.15F + (float) (Math.PI * 0.5D * b);
                    Vec3 bPos = new Vec3(Mth.cos(bAng) * 0.86D, 1.44D + 0.12D * Mth.sin(age * 0.22F + b), Mth.sin(bAng) * 0.86D);
                    renderOctahedron(m, vc, bPos, 0.16F, 0.16F, bAng, 0.467F, 0.196F, 0.078F, 0.97F);
                    renderOctahedron(m, vc, bPos, 0.11F, 0.22F, -bAng,
                            (b % 2 == 0) ? 1.000F : 0.102F,
                            (b % 2 == 0) ? 0.918F : 0.349F,
                            (b % 2 == 0) ? 0.471F : 0.137F,
                            0.98F);
                }
            } else if (state.materia == EssenceType.MIASMA) {
                // Section 6.46 Cadaverous Geyser Pylon (#1A5923 / #4D6453 / #225C7C): anchored fungal geyser pylon
                // erupting boiling swamp acid rings & 4 orbiting pressurized septic gas bladders!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float pulse = 0.94F + 0.10F * Mth.sin(age * 0.28F);
                float acidWave = (age * 0.05F) % 1.0F;
                float acidR = 0.68F + acidWave * 2.10F;

                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ, 0.68F, 1.12F, 12,
                        0.302F, 0.392F, 0.325F, 0.96F);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.068D, 0.0D), flatX, flatZ, acidR * 0.85F, acidR, 20,
                        0.133F, 0.361F, 0.486F, 0.94F * (1.0F - acidWave * 0.65F));

                renderCylinderFrustum(m, vc, 0.02F, 0.72F, 0.64F, 0.46F, 8, 0.0F, 0.302F, 0.392F, 0.325F, 0.97F);
                renderCylinderFrustum(m, vc, 0.72F, 1.62F, 0.44F, 0.56F, 8, 0.38F, 0.102F, 0.349F, 0.137F, 0.95F);

                Vec3 geyserVent = new Vec3(0.0D, 1.78D, 0.0D);
                renderOctahedron(m, vc, geyserVent, 0.28F * pulse, 0.32F * pulse, spin * 0.45F,
                        0.133F, 0.361F, 0.486F, 0.98F);
                renderLocalRing(m, vc, geyserVent, flatX, flatZ, 0.36F, 0.58F, 18,
                        0.102F, 0.349F, 0.137F, 0.94F);

                for (int g = 0; g < 4; g++) {
                    float gAng = -spin * 1.20F + (float) (Math.PI * 0.5D * g);
                    Vec3 gPos = new Vec3(Mth.cos(gAng) * 0.84D, 1.38D + 0.16D * Mth.sin(age * 0.26F + g), Mth.sin(gAng) * 0.84D);
                    renderOctahedron(m, vc, gPos, 0.16F, 0.18F, gAng,
                            (g % 2 == 0) ? 0.133F : 0.102F,
                            (g % 2 == 0) ? 0.361F : 0.349F,
                            (g % 2 == 0) ? 0.486F : 0.137F,
                            0.97F);
                }
            } else if (state.materia == EssenceType.GENESIS) {
                // Section 6.47 Dawn-Glory Spire (#FF6B9D / #FFEA78 / #225C7C): stationary sun-prism spire
                // with 3 golden water-beam lenses, sacred oceanic base & vitae lotus halo!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float pulse = 0.95F + 0.08F * Mth.sin(age * 0.26F);

                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ, 0.72F, 1.18F, 18,
                        0.133F, 0.361F, 0.486F, 0.95F);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.068D, 0.0D), flatX, flatZ, 0.88F, 1.04F, 18,
                        1.000F, 0.420F, 0.616F, 0.94F);

                renderCylinderFrustum(m, vc, 0.02F, 0.68F, 0.62F, 0.44F, 8, 0.0F, 0.133F, 0.361F, 0.486F, 0.96F);
                renderCylinderFrustum(m, vc, 0.68F, 1.66F, 0.42F, 0.30F, 8, 0.39F, 1.000F, 0.420F, 0.616F, 0.95F);

                Vec3 sunPrism = new Vec3(0.0D, 1.82D, 0.0D);
                renderOctahedron(m, vc, sunPrism, 0.28F * pulse, 0.36F * pulse, spin * 0.48F,
                        1.000F, 0.918F, 0.471F, 0.99F);
                renderLocalRing(m, vc, sunPrism, flatX, flatZ, 0.38F, 0.58F, 20,
                        1.000F, 0.420F, 0.616F, 0.95F);

                for (int l = 0; l < 3; l++) {
                    float lAng = -spin * 1.25F + (float) (Math.PI * 2.0D * l / 3.0D);
                    Vec3 lPos = new Vec3(Mth.cos(lAng) * 0.86D, 1.56D + 0.12D * Mth.sin(age * 0.25F + l), Mth.sin(lAng) * 0.86D);
                    renderOctahedron(m, vc, lPos, 0.15F, 0.22F, lAng, 1.000F, 0.918F, 0.471F, 0.99F);
                    renderOctahedron(m, vc, lPos, 0.19F, 0.14F, -lAng, 0.133F, 0.361F, 0.486F, 0.88F);
                }
            } else if (state.materia == EssenceType.OBLIVION) {
                // Section 6.48 Pyre of Nonexistence Obelisk (#B83C08 / #211A21 / #4D6453): stationary stygian obsidian obelisk
                // radiating a blazing extinction fire ring with 4 orbiting necrotic skull mortars!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float pulse = 0.94F + 0.09F * Mth.sin(age * 0.28F);

                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ, 0.76F, 1.24F, 16,
                        0.129F, 0.102F, 0.129F, 0.97F);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.070D, 0.0D), flatX, flatZ, 0.90F, 1.08F, 18,
                        0.722F, 0.235F, 0.031F, 0.96F);

                renderCylinderFrustum(m, vc, 0.02F, 0.72F, 0.66F, 0.48F, 6, 0.0F, 0.129F, 0.102F, 0.129F, 0.98F);
                renderCylinderFrustum(m, vc, 0.72F, 1.72F, 0.46F, 0.28F, 4, 0.785F, 0.302F, 0.392F, 0.325F, 0.95F);

                Vec3 pyreCore = new Vec3(0.0D, 1.88D, 0.0D);
                renderOctahedron(m, vc, pyreCore, 0.30F * pulse, 0.40F * pulse, spin * 0.52F,
                        0.722F, 0.235F, 0.031F, 0.99F);
                renderLocalRing(m, vc, pyreCore, flatX, flatZ, 0.40F, 0.60F, 18,
                        0.129F, 0.102F, 0.129F, 0.96F);

                for (int s = 0; s < 4; s++) {
                    float sAng = -spin * 1.18F + (float) (Math.PI * 0.5D * s);
                    Vec3 sPos = new Vec3(Mth.cos(sAng) * 0.88D, 1.54D + 0.12D * Mth.sin(age * 0.26F + s), Mth.sin(sAng) * 0.88D);
                    renderOctahedron(m, vc, sPos, 0.16F, 0.22F, sAng, 0.302F, 0.392F, 0.325F, 0.97F);
                    renderOctahedron(m, vc, sPos, 0.11F, 0.14F, -sAng, 0.722F, 0.235F, 0.031F, 0.99F);
                }
            } else if (state.materia == EssenceType.AETHER) {
                // Section 6.50 Apex Firmament Spire of Equilibrium (#A88654 / #773214 / #225C7C / #B83C08):
                // Anchored monolithic obelisk pulsing atmospheric halo rings & 4 orbiting primordial equilibrium cores!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float pulse = 0.95F + 0.09F * Mth.sin(age * 0.28F);

                renderLocalRing(m, vc, new Vec3(0.0D, 0.048D, 0.0D), flatX, flatZ, 0.80F, 1.32F, 22,
                        0.659F, 0.525F, 0.329F, 0.96F);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.072D, 0.0D), flatX, flatZ, 0.96F, 1.14F, 22,
                        0.133F, 0.361F, 0.486F, 0.94F);

                renderCylinderFrustum(m, vc, 0.02F, 0.76F, 0.68F, 0.50F, 8, 0.0F, 0.467F, 0.196F, 0.078F, 0.96F);
                renderCylinderFrustum(m, vc, 0.76F, 1.82F, 0.48F, 0.30F, 8, 0.39F, 0.659F, 0.525F, 0.329F, 0.96F);

                Vec3 firmamentApex = new Vec3(0.0D, 2.02D, 0.0D);
                renderOctahedron(m, vc, firmamentApex, 0.32F * pulse, 0.44F * pulse, spin * 0.55F,
                        0.659F, 0.525F, 0.329F, 0.99F);
                renderLocalRing(m, vc, firmamentApex, flatX, flatZ, 0.44F, 0.66F, 22,
                        0.133F, 0.361F, 0.486F, 0.96F);

                float[][] quadRgb = {
                        {0.659F, 0.525F, 0.329F},
                        {0.467F, 0.196F, 0.078F},
                        {0.133F, 0.361F, 0.486F},
                        {0.722F, 0.235F, 0.031F}
                };
                for (int q = 0; q < 4; q++) {
                    float qAng = spin * 1.20F + (float) (Math.PI * 0.5D * q);
                    float[] c = quadRgb[q];
                    Vec3 qPos = new Vec3(Mth.cos(qAng) * 0.94D, 1.58D + 0.14D * Mth.sin(age * 0.25F + q), Mth.sin(qAng) * 0.94D);
                    renderOctahedron(m, vc, qPos, 0.18F, 0.25F, qAng, c[0], c[1], c[2], 0.99F);
                }
            } else if (state.materia == EssenceType.ENTROPIC) {
                // Section 6.51 Apex Event Horizon Monolith (#41304E / #211A21 / #4D6453 / #8A0303):
                // Anchored abyssal obelisk crowned by a collapsed event-horizon singularity & 4 orbiting sanguine/necrotic mortars!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float pulse = 0.94F + 0.09F * Mth.sin(age * 0.28F);

                renderLocalRing(m, vc, new Vec3(0.0D, 0.048D, 0.0D), flatX, flatZ, 0.80F, 1.32F, 20,
                        0.129F, 0.102F, 0.129F, 0.98F);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.072D, 0.0D), flatX, flatZ, 0.96F, 1.14F, 20,
                        0.541F, 0.012F, 0.012F, 0.96F);

                renderCylinderFrustum(m, vc, 0.02F, 0.78F, 0.68F, 0.50F, 6, 0.0F, 0.129F, 0.102F, 0.129F, 0.98F);
                renderCylinderFrustum(m, vc, 0.78F, 1.84F, 0.48F, 0.28F, 6, 0.52F, 0.255F, 0.188F, 0.306F, 0.96F);

                Vec3 horizonApex = new Vec3(0.0D, 2.04D, 0.0D);
                renderOctahedron(m, vc, horizonApex, 0.32F * pulse, 0.44F * pulse, -spin * 0.55F,
                        0.129F, 0.102F, 0.129F, 0.99F);
                renderLocalRing(m, vc, horizonApex, flatX, flatZ, 0.44F, 0.66F, 22,
                        0.541F, 0.012F, 0.012F, 0.96F);

                for (int mIdx = 0; mIdx < 4; mIdx++) {
                    float mAng = -spin * 1.22F + (float) (Math.PI * 0.5D * mIdx);
                    boolean blood = (mIdx % 2 == 0);
                    Vec3 mPos = new Vec3(Mth.cos(mAng) * 0.94D, 1.58D + 0.14D * Mth.sin(age * 0.26F + mIdx), Mth.sin(mAng) * 0.94D);
                    renderOctahedron(m, vc, mPos, 0.18F, 0.24F, mAng,
                            blood ? 0.541F : 0.302F,
                            blood ? 0.012F : 0.392F,
                            blood ? 0.012F : 0.325F,
                            0.99F);
                }
            } else if (state.materia == EssenceType.CELESTIAL) {
                // Section 6.52 Apex White-Hole Dawn Spire (#FFEA78 / #FF6B9D / #1A5923 / #A88654):
                // Consecrated white-gold & emerald spire crowned by a blazing white-hole dawn singularity & 4 orbiting pranic starcores!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.048D, 0.0D), flatX, flatZ, 0.72F, 1.26F, 24,
                        1.000F, 0.918F, 0.471F, 0.97F);
                renderCylinderFrustum(m, vc, 0.02F, 0.44F, 0.68F, 0.48F, 8, 0.0F,
                        0.659F, 0.525F, 0.329F, 0.98F);
                renderCylinderFrustum(m, vc, 0.44F, 1.78F, 0.44F, 0.26F, 8, 0.39F,
                        1.000F, 0.918F, 0.471F, 0.96F);

                Vec3 dawnApex = new Vec3(0.0D, 2.08D + 0.08D * Mth.sin(age * 0.22F), 0.0D);
                renderOctahedron(m, vc, dawnApex, 0.28F, 0.42F, spin * 1.15F,
                        1.000F, 1.000F, 1.000F, 0.99F);
                renderLocalRing(m, vc, dawnApex, flatX, flatZ, 0.44F, 0.68F, 24,
                        1.000F, 0.420F, 0.616F, 0.96F);

                float[][] celRgb = {
                        {1.000F, 0.918F, 0.471F},
                        {1.000F, 0.420F, 0.616F},
                        {0.102F, 0.349F, 0.137F},
                        {0.659F, 0.525F, 0.329F}
                };
                for (int cIdx = 0; cIdx < 4; cIdx++) {
                    float cAng = spin * 1.24F + (float) (Math.PI * 0.5D * cIdx);
                    float[] c = celRgb[cIdx];
                    Vec3 cPos = new Vec3(Mth.cos(cAng) * 0.94D, 1.58D + 0.14D * Mth.sin(age * 0.26F + cIdx), Mth.sin(cAng) * 0.94D);
                    renderOctahedron(m, vc, cPos, 0.18F, 0.25F, cAng, c[0], c[1], c[2], 0.99F);
                }
            } else if (state.materia == EssenceType.PRISMATIC) {
                // Section 6.53 Refractive Mirror Monolith (#FFFFFF / #72F6FF / Full HSB Spectrum):
                // Multi-tiered diamond crystal obelisk crowned by a white-hot prism apex & 6 orbiting rainbow mirror facets!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                int baseRgb = Mth.hsvToRgb((age * 0.025F) % 1.0F, 0.72F, 1.0F);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.048D, 0.0D), flatX, flatZ, 0.74F, 1.32F, 24,
                        ((baseRgb >> 16) & 0xFF) / 255.0F,
                        ((baseRgb >> 8) & 0xFF) / 255.0F,
                        (baseRgb & 0xFF) / 255.0F,
                        0.96F);
                renderCylinderFrustum(m, vc, 0.02F, 0.46F, 0.68F, 0.48F, 8, 0.0F,
                        0.820F, 0.960F, 1.000F, 0.96F);
                renderCylinderFrustum(m, vc, 0.46F, 1.84F, 0.44F, 0.24F, 6, spin * 0.45F,
                        1.000F, 1.000F, 1.000F, 0.92F);

                Vec3 prismApex = new Vec3(0.0D, 2.14D + 0.08D * Mth.sin(age * 0.24F), 0.0D);
                renderOctahedron(m, vc, prismApex, 0.30F, 0.46F, spin * 1.35F,
                        1.000F, 1.000F, 1.000F, 0.99F);
                renderLocalRing(m, vc, prismApex, flatX, flatZ, 0.46F, 0.72F, 24,
                        0.447F, 0.965F, 1.000F, 0.95F);

                for (int pIdx = 0; pIdx < 6; pIdx++) {
                    float pAng = -spin * 1.30F + (float) (Math.PI * 2.0D * pIdx / 6.0D);
                    int mRgb = Mth.hsvToRgb(((pIdx / 6.0F) + age * 0.032F) % 1.0F, 0.78F, 1.0F);
                    float mr = ((mRgb >> 16) & 0xFF) / 255.0F;
                    float mg = ((mRgb >> 8) & 0xFF) / 255.0F;
                    float mb = (mRgb & 0xFF) / 255.0F;
                    Vec3 mPos = new Vec3(Mth.cos(pAng) * 0.96D, 1.48D + 0.16D * Mth.sin(age * 0.28F + pIdx), Mth.sin(pAng) * 0.96D);
                    renderOctahedron(m, vc, mPos, 0.17F, 0.28F, pAng, mr, mg, mb, 0.98F);
                }
            } else if (state.materia == EssenceType.EMPYREAN) {
                // Section 6.54 Heartwood Canopy Obelisk (#FFEA78 / #FF6B9D / #1A5923):
                // Monolithic World-Tree heartwood trunk, emerald foliage crown, rose-vitae seed heart & 6 orbiting golden seed-pods!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.050D, 0.0D), flatX, flatZ, 0.80F, 1.44F, 24,
                        1.000F, 0.918F, 0.471F, 0.97F);
                renderCylinderFrustum(m, vc, 0.02F, 0.56F, 0.78F, 0.54F, 8, 0.0F,
                        0.102F, 0.349F, 0.137F, 0.98F);
                renderCylinderFrustum(m, vc, 0.56F, 1.96F, 0.50F, 0.30F, 8, 0.39F,
                        1.000F, 0.918F, 0.471F, 0.95F);

                Vec3 canopyApex = new Vec3(0.0D, 2.24D + 0.08D * Mth.sin(age * 0.20F), 0.0D);
                renderOctahedron(m, vc, canopyApex, 0.34F, 0.48F, spin * 1.10F,
                        1.000F, 0.420F, 0.616F, 0.99F);
                renderLocalRing(m, vc, canopyApex, flatX, flatZ, 0.54F, 0.92F, 24,
                        0.102F, 0.349F, 0.137F, 0.96F);

                float[][] empRgb = {
                        {1.000F, 0.918F, 0.471F},
                        {1.000F, 0.420F, 0.616F},
                        {0.102F, 0.349F, 0.137F}
                };
                for (int sIdx = 0; sIdx < 6; sIdx++) {
                    float sAng = spin * 1.18F + (float) (Math.PI * 2.0D * sIdx / 6.0D);
                    float[] c = empRgb[sIdx % 3];
                    Vec3 sPos = new Vec3(Mth.cos(sAng) * 1.04D, 1.56D + 0.15D * Mth.sin(age * 0.24F + sIdx), Mth.sin(sAng) * 1.04D);
                    renderOctahedron(m, vc, sPos, 0.18F, 0.28F, sAng, c[0], c[1], c[2], 0.99F);
                }
            } else if (state.materia == EssenceType.CATACLYSM) {
                // Section 6.55 Volcanic Eruption Spire (#41304E / #211A21 / #B83C08):
                // Monolithic magma-void obsidian spire, inward-pulling 16m caldera gravity rings, roaring magma crater apex & 6 orbiting railgun magma spikes!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float pullWave = 1.0F - ((age * 0.052F) % 1.0F);
                float pullR = 0.80F + pullWave * 1.85F;
                renderLocalRing(m, vc, new Vec3(0.0D, 0.050D, 0.0D), flatX, flatZ, 0.82F, 1.48F, 24,
                        0.722F, 0.235F, 0.031F, 0.97F);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.072D, 0.0D), flatX, flatZ, pullR * 0.86F, pullR, 24,
                        0.255F, 0.188F, 0.306F, 0.92F * (1.0F - pullWave * 0.55F));

                renderCylinderFrustum(m, vc, 0.02F, 0.60F, 0.80F, 0.56F, 6, 0.0F,
                        0.129F, 0.102F, 0.129F, 0.99F);
                renderCylinderFrustum(m, vc, 0.60F, 2.02F, 0.52F, 0.32F, 6, 0.52F,
                        0.255F, 0.188F, 0.306F, 0.96F);

                Vec3 calderaApex = new Vec3(0.0D, 2.28D + 0.07D * Mth.sin(age * 0.28F), 0.0D);
                renderOctahedron(m, vc, calderaApex, 0.35F, 0.50F, spin * 1.25F,
                        0.722F, 0.235F, 0.031F, 0.99F);
                renderLocalRing(m, vc, calderaApex, flatX, flatZ, 0.52F, 0.94F, 20,
                        1.000F, 0.369F, 0.102F, 0.96F);

                float[][] catRgb = {
                        {0.722F, 0.235F, 0.031F}, // #B83C08 Nether Blazing Rust Crimson
                        {0.255F, 0.188F, 0.306F}, // #41304E Void Deep Indigo
                        {0.129F, 0.102F, 0.129F}  // #211A21 Umbral Shadow Charcoal
                };
                for (int sIdx = 0; sIdx < 6; sIdx++) {
                    float sAng = -spin * 1.35F + (float) (Math.PI * 2.0D * sIdx / 6.0D);
                    float[] c = catRgb[sIdx % 3];
                    Vec3 sPos = new Vec3(Mth.cos(sAng) * 1.06D, 1.58D + 0.14D * Mth.sin(age * 0.30F + sIdx), Mth.sin(sAng) * 1.06D);
                    renderOctahedron(m, vc, sPos, 0.13F, 0.34F, sAng, c[0], c[1], c[2], 0.99F);
                }
            } else if (state.materia == EssenceType.TERMINUS) {
                // Section 6.56 Anchored Permafrost Elemental Spire (#A88654 / #98DCF2 / #225C7C):
                // Monolithic deep-azure & frost-cyan permafrost spire, 16m Polar Freeze Field wave rings,
                // golden-buff vacuum gale halo & 6 orbiting wall-penetrating cryogenic lances!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float freezeWave = (age * 0.046F) % 1.0F;
                float freezeR = 0.82F + freezeWave * 2.10F;
                renderLocalRing(m, vc, new Vec3(0.0D, 0.050D, 0.0D), flatX, flatZ, 0.80F, 1.44F, 24,
                        0.133F, 0.361F, 0.486F, 0.96F);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.072D, 0.0D), flatX, flatZ, freezeR * 0.88F, freezeR, 24,
                        0.596F, 0.863F, 0.949F, 0.92F * (1.0F - freezeWave * 0.58F));

                renderCylinderFrustum(m, vc, 0.02F, 0.64F, 0.78F, 0.54F, 6, 0.0F,
                        0.133F, 0.361F, 0.486F, 0.97F);
                renderCylinderFrustum(m, vc, 0.64F, 2.06F, 0.50F, 0.28F, 6, 0.52F,
                        0.596F, 0.863F, 0.949F, 0.93F);

                Vec3 permafrostApex = new Vec3(0.0D, 2.32D + 0.06D * Mth.sin(age * 0.24F), 0.0D);
                renderOctahedron(m, vc, permafrostApex, 0.32F, 0.52F, spin * 1.10F,
                        0.596F, 0.863F, 0.949F, 0.99F);
                renderLocalRing(m, vc, permafrostApex, flatX, flatZ, 0.50F, 0.90F, 22,
                        0.659F, 0.525F, 0.329F, 0.95F);

                float[][] termRgb = {
                        {0.596F, 0.863F, 0.949F}, // #98DCF2 Frozen Cyan/Frost
                        {0.659F, 0.525F, 0.329F}, // #A88654 Air Golden-Buff
                        {0.133F, 0.361F, 0.486F}  // #225C7C Water Deep Azure
                };
                for (int sIdx = 0; sIdx < 6; sIdx++) {
                    float sAng = spin * 1.28F + (float) (Math.PI * 2.0D * sIdx / 6.0D);
                    float[] c = termRgb[sIdx % 3];
                    Vec3 sPos = new Vec3(Mth.cos(sAng) * 1.06D, 1.58D + 0.14D * Mth.sin(age * 0.26F + sIdx), Mth.sin(sAng) * 1.06D);
                    renderOctahedron(m, vc, sPos, 0.12F, 0.36F, sAng, c[0], c[1], c[2], 0.98F);
                }
            } else if (state.materia == EssenceType.ESCHATON) {
                // Section 6.57 Accretion Disk Singularity Spire (#0B0813 / #7012A5 / #D4C2FC):
                // Monolithic dark-matter obelisk, inward-collapsing 20m Gravitational Accretion Field rings,
                // suspended event-horizon apex core with Chrono-Silver halo & 6 orbiting singularity lances!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float inwardWave = 1.0F - ((age * 0.052F) % 1.0F);
                float inwardR = 0.85F + inwardWave * 2.35F;
                renderLocalRing(m, vc, new Vec3(0.0D, 0.050D, 0.0D), flatX, flatZ, 0.84F, 1.54F, 26,
                        0.043F, 0.031F, 0.075F, 0.98F);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.072D, 0.0D), flatX, flatZ, inwardR * 0.88F, inwardR, 26,
                        0.439F, 0.071F, 0.647F, 0.94F * (1.0F - inwardWave * 0.55F));

                renderCylinderFrustum(m, vc, 0.02F, 0.68F, 0.82F, 0.56F, 8, 0.0F,
                        0.043F, 0.031F, 0.075F, 0.99F);
                renderCylinderFrustum(m, vc, 0.68F, 2.16F, 0.52F, 0.30F, 8, 0.39F,
                        0.439F, 0.071F, 0.647F, 0.94F);

                Vec3 singularityApex = new Vec3(0.0D, 2.42D + 0.06D * Mth.sin(age * 0.28F), 0.0D);
                renderOctahedron(m, vc, singularityApex, 0.34F, 0.54F, -spin * 1.25F,
                        0.043F, 0.031F, 0.075F, 0.99F);
                renderLocalRing(m, vc, singularityApex, flatX, flatZ, 0.54F, 0.98F, 24,
                        0.831F, 0.761F, 0.988F, 0.97F);

                float[][] eschRgb = {
                        {0.439F, 0.071F, 0.647F}, // #7012A5 Accretion Disk Searing Violet
                        {0.831F, 0.761F, 0.988F}, // #D4C2FC Event Edge Chrono-Silver
                        {0.043F, 0.031F, 0.075F}  // #0B0813 Event Horizon Void Black
                };
                for (int sIdx = 0; sIdx < 6; sIdx++) {
                    float sAng = -spin * 1.42F + (float) (Math.PI * 2.0D * sIdx / 6.0D);
                    float[] c = eschRgb[sIdx % 3];
                    Vec3 sPos = new Vec3(Mth.cos(sAng) * 1.12D, 1.64D + 0.14D * Mth.sin(age * 0.30F + sIdx), Mth.sin(sAng) * 1.12D);
                    renderOctahedron(m, vc, sPos, 0.13F, 0.40F, sAng, c[0], c[1], c[2], 0.99F);
                }
            } else if (state.materia == EssenceType.APOTHEOSIS) {
                // Section 6.58 White-Hole Dawn Obelisk (#FFFFFF / #FFE484 / #72F6FF):
                // Monolithic white-hole obelisk, expanding 20m Sanctuary Field rings, suspended supernova apex starcore
                // with Astral Gold & Prismatic Cyan halos & 6 orbiting holy supernova lances!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float outWave = (age * 0.052F) % 1.0F;
                float outR = 0.88F + outWave * 2.40F;
                renderLocalRing(m, vc, new Vec3(0.0D, 0.050D, 0.0D), flatX, flatZ, 0.86F, 1.56F, 28,
                        1.000F, 0.894F, 0.518F, 0.98F);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.072D, 0.0D), flatX, flatZ, outR * 0.88F, outR, 28,
                        0.447F, 0.965F, 1.000F, 0.94F * (1.0F - outWave * 0.55F));

                renderCylinderFrustum(m, vc, 0.02F, 0.70F, 0.82F, 0.56F, 8, 0.0F,
                        1.000F, 1.000F, 1.000F, 0.99F);
                renderCylinderFrustum(m, vc, 0.70F, 2.20F, 0.52F, 0.30F, 8, 0.39F,
                        1.000F, 0.894F, 0.518F, 0.96F);

                Vec3 dawnApex = new Vec3(0.0D, 2.46D + 0.06D * Mth.sin(age * 0.28F), 0.0D);
                renderOctahedron(m, vc, dawnApex, 0.36F, 0.56F, spin * 1.25F,
                        1.000F, 1.000F, 1.000F, 0.99F);
                renderLocalRing(m, vc, dawnApex, flatX, flatZ, 0.56F, 1.00F, 26,
                        0.447F, 0.965F, 1.000F, 0.97F);

                float[][] apoRgb = {
                        {1.000F, 1.000F, 1.000F}, // #FFFFFF Supernova Brilliant White
                        {1.000F, 0.894F, 0.518F}, // #FFE484 Celestial Astral Gold
                        {0.447F, 0.965F, 1.000F}  // #72F6FF Ascendant Prismatic Cyan
                };
                for (int sIdx = 0; sIdx < 6; sIdx++) {
                    float sAng = spin * 1.42F + (float) (Math.PI * 2.0D * sIdx / 6.0D);
                    float[] c = apoRgb[sIdx % 3];
                    Vec3 sPos = new Vec3(Mth.cos(sAng) * 1.14D, 1.66D + 0.14D * Mth.sin(age * 0.30F + sIdx), Mth.sin(sAng) * 1.14D);
                    renderOctahedron(m, vc, sPos, 0.14F, 0.42F, sAng, c[0], c[1], c[2], 0.99F);
                }
            } else if (state.materia == EssenceType.ENTROPICA) {
                // Section 6.59 Altar of the Horizon (#E0E5EC / #1A153B / #FAD02C):
                // Monolithic equilibrium obelisk, balanced inward/outward 20m Equilibrium Field rings,
                // suspended dual-singularity apex core & 6 orbiting balance-ray lances!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float outWave = (age * 0.050F) % 1.0F;
                float inWave = 1.0F - outWave;
                float outR = 0.88F + outWave * 2.45F;
                float inR = 0.88F + inWave * 2.45F;
                renderLocalRing(m, vc, new Vec3(0.0D, 0.050D, 0.0D), flatX, flatZ, 0.88F, 1.58F, 28,
                        0.878F, 0.898F, 0.925F, 0.98F);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.068D, 0.0D), flatX, flatZ, outR * 0.88F, outR, 28,
                        0.980F, 0.816F, 0.173F, 0.95F * (1.0F - outWave * 0.55F));
                renderLocalRing(m, vc, new Vec3(0.0D, 0.082D, 0.0D), flatX, flatZ, inR * 0.88F, inR, 28,
                        0.102F, 0.082F, 0.231F, 0.95F * (1.0F - inWave * 0.55F));

                renderCylinderFrustum(m, vc, 0.02F, 0.72F, 0.84F, 0.58F, 8, 0.0F,
                        0.102F, 0.082F, 0.231F, 0.99F);
                renderCylinderFrustum(m, vc, 0.72F, 2.24F, 0.54F, 0.30F, 8, 0.39F,
                        0.878F, 0.898F, 0.925F, 0.96F);

                Vec3 horizonApex = new Vec3(0.0D, 2.50D + 0.06D * Mth.sin(age * 0.28F), 0.0D);
                renderOctahedron(m, vc, horizonApex, 0.36F, 0.58F, spin * 1.28F,
                        0.980F, 0.816F, 0.173F, 0.99F);
                renderLocalRing(m, vc, horizonApex, flatX, flatZ, 0.58F, 1.04F, 26,
                        0.878F, 0.898F, 0.925F, 0.98F);

                float[][] entRgb = {
                        {0.878F, 0.898F, 0.925F}, // #E0E5EC Shifting Chrono-Silver
                        {0.980F, 0.816F, 0.173F}, // #FAD02C Transcendent Genesis Gold
                        {0.102F, 0.082F, 0.231F}  // #1A153B Deep Event-Horizon Indigo
                };
                for (int sIdx = 0; sIdx < 6; sIdx++) {
                    float sAng = spin * 1.44F + (float) (Math.PI * 2.0D * sIdx / 6.0D);
                    float[] c = entRgb[sIdx % 3];
                    Vec3 sPos = new Vec3(Mth.cos(sAng) * 1.16D, 1.68D + 0.14D * Mth.sin(age * 0.30F + sIdx), Mth.sin(sAng) * 1.16D);
                    renderOctahedron(m, vc, sPos, 0.14F, 0.44F, sAng, c[0], c[1], c[2], 0.99F);
                }
            }
        });
        poseStack.popPose();
    }

    /**
     * Triad Archetype 3 — PHANTOM DECOY: Shimmering spinning cyclone / elemental mirage effigy.
     */
    private void submitPhantomDecoyEffigy(
            EidolonRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            float pr, float pg, float pb,
            float ar, float ag, float ab
    ) {
        poseStack.pushPose();

        // 1. Multi-ringed spinning cyclone funnel + helical slipstream ribbons
        collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(EIDOLON_TEXTURE), (pose, vc) -> {
            Matrix4f m = pose.pose();
            float age = state.ageInTicks;
            float flicker = 0.78F + 0.22F * Mth.sin(age * 0.30F);

            int bands = 7;
            for (int i = 0; i < bands; i++) {
                float t = i / (float) (bands - 1);
                double y = 0.10D + t * 1.80D;
                float rIn = (0.20F + 0.48F * t) * (0.94F + 0.06F * Mth.sin(age * 0.25F + i));
                float rOut = rIn + 0.09F + 0.04F * t;
                float spin = age * (0.22F + 0.04F * i) * (i % 2 == 0 ? 1.0F : -1.0F);
                Vec3 ux = new Vec3(Mth.cos(spin), 0.08D * Mth.sin(spin), Mth.sin(spin)).normalize();
                Vec3 uz = new Vec3(-Mth.sin(spin), 0.08D * Mth.cos(spin), Mth.cos(spin)).normalize();
                float cr = (i % 2 == 0) ? pr : ar;
                float cg = (i % 2 == 0) ? pg : ag;
                float cb = (i % 2 == 0) ? pb : ab;
                renderLocalRing(m, vc, new Vec3(0.0D, y, 0.0D), ux, uz, rIn, rOut, 20, cr, cg, cb, 0.70F * flicker);
            }

            // Air (#1) Mirage Cyclone: continuous translucent funnel veil + orbiting wind-shear blades!
            if (state.materia == EssenceType.AIR) {
                int tiers = 6;
                for (int tIdx = 0; tIdx < tiers; tIdx++) {
                    float t0 = tIdx / (float) tiers;
                    float t1 = (tIdx + 1) / (float) tiers;
                    float y0 = 0.08F + t0 * 1.86F;
                    float y1 = 0.08F + t1 * 1.86F;
                    float r0 = 0.24F + 0.52F * t0;
                    float r1 = 0.24F + 0.52F * t1;
                    renderOpenFrustumWall(m, vc, y0, y1, r0, r1, 18, age * 0.18F + tIdx * 0.3F,
                            pr, pg, pb, (0.34F - 0.14F * t0) * flicker);
                }
                for (int c = 0; c < 4; c++) {
                    float cAng = age * 0.32F + (float) (Math.PI * 0.5D * c);
                    double cy = 0.45D + 0.35D * c;
                    float cr = 0.44F + 0.10F * c;
                    renderSweptArcBlade(m, vc, new Vec3(0.0D, cy, 0.0D), cr, cAng, 1.15F, 0.09F, ar, ag, ab, 0.88F * flicker);
                }
            } else if (state.materia == EssenceType.WATER) {
                // Section 6.3 Aqueous Mirage Decoy (#225C7C): shimmering water-refraction veil, foaming base ripple & rising 3D bubbles!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float rippleT = (age * 0.06F) % 1.0F;
                float rippleR = 0.35F + rippleT * 0.65F;
                renderLocalRing(m, vc, new Vec3(0.0D, 0.04D, 0.0D), flatX, flatZ,
                        rippleR * 0.84F, rippleR, 24, 0.88F, 0.98F, 1.00F, 0.78F * (1.0F - rippleT) * flicker);

                int tiers = 6;
                for (int tIdx = 0; tIdx < tiers; tIdx++) {
                    float t0 = tIdx / (float) tiers;
                    float t1 = (tIdx + 1) / (float) tiers;
                    float y0 = 0.08F + t0 * 1.84F;
                    float y1 = 0.08F + t1 * 1.84F;
                    float r0 = 0.38F + 0.10F * Mth.sin(age * 0.22F + t0 * 5.0F);
                    float r1 = 0.38F + 0.10F * Mth.sin(age * 0.22F + t1 * 5.0F);
                    renderOpenFrustumWall(m, vc, y0, y1, r0, r1, 18, -age * 0.12F + tIdx * 0.25F,
                            0.133F, 0.361F, 0.486F, (0.36F - 0.10F * t0) * flicker);
                }
                for (int b = 0; b < 6; b++) {
                    float bRise = ((age * 0.055F + b / 6.0F) % 1.0F);
                    float bAng = b * 1.0472F + age * 0.09F;
                    double br = 0.46D + 0.08D * Mth.sin(age * 0.2F + b);
                    Vec3 bPos = new Vec3(Mth.cos(bAng) * br, 0.16D + bRise * 1.72D, Mth.sin(bAng) * br);
                    float bFade = Mth.sin(bRise * (float) Math.PI) * flicker;
                    renderOctahedron(m, vc, bPos, 0.055F, 0.060F, age * 0.2F, 0.88F, 0.98F, 1.00F, 0.88F * bFade);
                }
            } else if (state.materia == EssenceType.NATURE) {
                // Section 6.4 Sylvan Pollen Effigy (#1A5923): woven flowering vine effigy, 8-petal blossom crown & sweet golden pollen!
                for (int v = 0; v < 4; v++) {
                    float vBase = age * 0.06F + (float) (Math.PI * 0.5D * v);
                    Vec3 prevV = null;
                    for (int s = 0; s <= 12; s++) {
                        float u = s / 12.0F;
                        double vy = 0.06D + u * 1.82D;
                        double vr = 0.34D + 0.16D * Mth.sin(u * (float) Math.PI);
                        float va = vBase + u * 3.8F * ((v % 2 == 0) ? 1.0F : -1.0F);
                        Vec3 vpt = new Vec3(Mth.cos(va) * vr, vy, Mth.sin(va) * vr);
                        if (prevV != null) {
                            Vec3 perp = new Vec3(-Mth.sin(va) * 0.036D, 0.024D, Mth.cos(va) * 0.036D);
                            emitDoubleSidedQuad(m, vc, prevV.subtract(perp), prevV.add(perp), vpt.add(perp), vpt.subtract(perp),
                                    (v % 2 == 0) ? 0.102F : 0.17F, (v % 2 == 0) ? 0.349F : 0.90F, (v % 2 == 0) ? 0.137F : 0.41F,
                                    0.86F * flicker);
                        }
                        prevV = vpt;
                    }
                }

                // Rotating 8-petal blossom crown atop the floral effigy
                Vec3 crownPos = new Vec3(0.0D, 1.92D, 0.0D);
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                renderLocalRing(m, vc, crownPos, flatX, flatZ, 0.0F, 0.18F, 14, 1.00F, 0.85F, 0.24F, 0.96F * flicker);
                for (int p = 0; p < 8; p++) {
                    float pAng = age * 0.08F + (float) (Math.PI * 2.0D * p / 8.0D);
                    Vec3 pDir = new Vec3(Mth.cos(pAng), 0.14D, Mth.sin(pAng)).normalize();
                    Vec3 pTang = new Vec3(-Mth.sin(pAng), 0.0D, Mth.cos(pAng));
                    emitDoubleSidedQuad(m, vc,
                            crownPos.add(pDir.scale(0.14D)),
                            crownPos.add(pDir.scale(0.34D)).add(pTang.scale(0.11D)),
                            crownPos.add(pDir.scale(0.50D)),
                            crownPos.add(pDir.scale(0.34D)).subtract(pTang.scale(0.11D)),
                            1.00F, 0.58F, 0.76F, 0.88F * flicker);
                }

                // Wafting golden pollen allure rings & drifting pollen motes
                float pollenCycle = (age * 0.045F) % 1.0F;
                float pollenR = 0.42F + pollenCycle * 0.85F;
                renderLocalRing(m, vc, new Vec3(0.0D, 1.05D + 0.15D * Mth.sin(age * 0.12F), 0.0D), flatX, flatZ,
                        pollenR * 0.86F, pollenR, 24, 1.00F, 0.85F, 0.22F, 0.76F * (1.0F - pollenCycle) * flicker);
                for (int sp = 0; sp < 6; sp++) {
                    float spRise = ((age * 0.04F + sp / 6.0F) % 1.0F);
                    float spAng = sp * 1.0472F + age * 0.11F;
                    double spr = 0.52D + 0.22D * spRise;
                    Vec3 spPos = new Vec3(Mth.cos(spAng) * spr, 0.25D + spRise * 1.65D, Mth.sin(spAng) * spr);
                    renderOctahedron(m, vc, spPos, 0.052F, 0.056F, age * 0.18F,
                            1.00F, 0.86F, 0.24F, 0.88F * Mth.sin(spRise * (float) Math.PI) * flicker);
                }
            } else if (state.materia == EssenceType.EARTH) {
                // Section 6.5 Granite Effigy Decoy (#773214): 4 interlocking carved granite/terracotta monolith slabs, amber keystone & taunt tremors!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float tremorT = (age * 0.045F) % 1.0F;
                float tremorR = 0.48F + tremorT * 1.15F;
                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ,
                        tremorR * 0.86F, tremorR, 8, 1.00F, 0.76F, 0.47F, 0.82F * (1.0F - tremorT) * flicker);
                renderCylinderFrustum(m, vc, 0.02F, 0.20F, 0.68F, 0.58F, 8, 0.0F, 0.353F, 0.322F, 0.298F, 0.95F);

                // 4 towering carved granite & burnt-terracotta bastion slabs encasing the effigy
                for (int p = 0; p < 4; p++) {
                    float pAng = age * 0.025F + (float) (Math.PI * 0.5D * p);
                    double bob = 0.035D * Mth.sin(age * 0.10F + p * 1.57F);
                    Vec3 rad = new Vec3(Mth.cos(pAng), 0.0D, Mth.sin(pAng));
                    Vec3 tang = new Vec3(-Mth.sin(pAng), 0.0D, Mth.cos(pAng));
                    Vec3 b0 = rad.scale(0.50D).subtract(tang.scale(0.24D)).add(0.0D, 0.18D + bob, 0.0D);
                    Vec3 b1 = rad.scale(0.50D).add(tang.scale(0.24D)).add(0.0D, 0.18D + bob, 0.0D);
                    Vec3 t1 = rad.scale(0.42D).add(tang.scale(0.18D)).add(0.0D, 1.78D + bob, 0.0D);
                    Vec3 t0 = rad.scale(0.42D).subtract(tang.scale(0.18D)).add(0.0D, 1.78D + bob, 0.0D);
                    boolean graniteSlab = (p % 2 == 0);
                    emitDoubleSidedQuad(m, vc, b0, b1, t1, t0,
                            graniteSlab ? 0.353F : 0.467F,
                            graniteSlab ? 0.322F : 0.196F,
                            graniteSlab ? 0.298F : 0.078F,
                            0.90F);
                    // Glowing amber fault seam running up the center of each granite slab!
                    Vec3 seamBot = rad.scale(0.515D).add(0.0D, 0.24D + bob, 0.0D);
                    Vec3 seamTop = rad.scale(0.435D).add(0.0D, 1.72D + bob, 0.0D);
                    Vec3 sw = tang.scale(0.026D);
                    emitDoubleSidedQuad(m, vc, seamBot.subtract(sw), seamBot.add(sw), seamTop.add(sw), seamTop.subtract(sw),
                            1.00F, 0.76F, 0.47F, 0.96F * flicker);
                }

                // Levitating amber-quartz keystone obelisk crowning the Granite Effigy
                Vec3 keystone = new Vec3(0.0D, 2.08D + 0.06D * Mth.sin(age * 0.12F), 0.0D);
                renderOctahedron(m, vc, keystone, 0.18F, 0.30F, -age * 0.08F, 1.00F, 0.76F, 0.47F, 0.96F * flicker);
                renderOctahedron(m, vc, keystone, 0.24F, 0.22F, age * 0.05F, 0.467F, 0.196F, 0.078F, 0.80F * flicker);
            } else if (state.materia == EssenceType.FROZEN) {
                // Section 6.6 Glacial Ice Effigy (#98DCF2): 4 translucent crystalline ice prism slabs, hoarfrost ring & 6-pointed snowflake crown!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float frostPulse = (age * 0.05F) % 1.0F;
                float frostR = 0.45F + frostPulse * 1.15F;
                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ,
                        frostR * 0.86F, frostR, 6, 0.92F, 0.98F, 1.00F, 0.82F * (1.0F - frostPulse) * flicker);
                renderOpenFrustumWall(m, vc, 0.06F, 1.88F, 0.56F, 0.46F, 6, age * 0.02F, 0.60F, 0.86F, 0.95F, 0.36F * flicker);

                // 4 towering faceted permafrost-blue & glacial-cyan prism facets encasing the ice effigy
                for (int p = 0; p < 4; p++) {
                    float pAng = -age * 0.03F + (float) (Math.PI * 0.5D * p);
                    double bob = 0.04D * Mth.sin(age * 0.11F + p * 1.57F);
                    Vec3 rad = new Vec3(Mth.cos(pAng), 0.0D, Mth.sin(pAng));
                    Vec3 tang = new Vec3(-Mth.sin(pAng), 0.0D, Mth.cos(pAng));
                    Vec3 b0 = rad.scale(0.52D).subtract(tang.scale(0.22D)).add(0.0D, 0.16D + bob, 0.0D);
                    Vec3 b1 = rad.scale(0.52D).add(tang.scale(0.22D)).add(0.0D, 0.16D + bob, 0.0D);
                    Vec3 t1 = rad.scale(0.42D).add(tang.scale(0.15D)).add(0.0D, 1.82D + bob, 0.0D);
                    Vec3 t0 = rad.scale(0.42D).subtract(tang.scale(0.15D)).add(0.0D, 1.82D + bob, 0.0D);
                    boolean cyanFacet = (p % 2 == 0);
                    emitDoubleSidedQuad(m, vc, b0, b1, t1, t0,
                            cyanFacet ? 0.60F : 0.17F,
                            cyanFacet ? 0.86F : 0.51F,
                            cyanFacet ? 0.95F : 0.79F,
                            0.68F * flicker);
                    // Diamond-white specular ice edge highlight
                    Vec3 seamBot = rad.scale(0.535D).add(0.0D, 0.22D + bob, 0.0D);
                    Vec3 seamTop = rad.scale(0.435D).add(0.0D, 1.76D + bob, 0.0D);
                    Vec3 sw = tang.scale(0.022D);
                    emitDoubleSidedQuad(m, vc, seamBot.subtract(sw), seamBot.add(sw), seamTop.add(sw), seamTop.subtract(sw),
                            0.94F, 0.99F, 1.00F, 0.94F * flicker);
                }

                // Levitating 6-pointed crystalline snowflake keystone crowning the Ice Effigy
                Vec3 crown = new Vec3(0.0D, 2.08D + 0.06D * Mth.sin(age * 0.12F), 0.0D);
                renderOctahedron(m, vc, crown, 0.16F, 0.30F, age * 0.09F, 0.94F, 0.99F, 1.00F, 0.96F * flicker);
                for (int arm = 0; arm < 6; arm++) {
                    float sAng = age * 0.09F + (float) (Math.PI * 2.0D * arm / 6.0D);
                    Vec3 armTip = crown.add(Mth.cos(sAng) * 0.36D, 0.0D, Mth.sin(sAng) * 0.36D);
                    Vec3 aw = new Vec3(-Mth.sin(sAng) * 0.032D, 0.032D, Mth.cos(sAng) * 0.032D);
                    emitDoubleSidedQuad(m, vc, crown.subtract(aw), crown.add(aw), armTip.add(aw.scale(0.3D)), armTip.subtract(aw.scale(0.3D)),
                            0.60F, 0.86F, 0.95F, 0.90F * flicker);
                }
            } else if (state.materia == EssenceType.NETHER) {
                // Section 6.7 Blazing Wicker Effigy (#B83C08): criss-crossed 3D charred wicker lattice engulfed in rising hellfire ribbons & flame crown!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float blazePulse = (age * 0.06F) % 1.0F;
                float blazeR = 0.45F + blazePulse * 1.20F;
                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ,
                        blazeR * 0.84F, blazeR, 20, 1.00F, 0.82F, 0.36F, 0.85F * (1.0F - blazePulse) * flicker);

                // 8 diagonal interwoven charred-wicker timber struts (#4A2814 with glowing #FFD15C ember seams)
                for (int w = 0; w < 8; w++) {
                    float wBase = (float) (Math.PI * 2.0D * w / 8.0D) + age * 0.02F;
                    float twistDir = (w % 2 == 0) ? 1.35F : -1.35F;
                    Vec3 prevW = null;
                    for (int s = 0; s <= 10; s++) {
                        float u = s / 10.0F;
                        double wy = 0.06D + u * 1.84D;
                        double wr = 0.46D - 0.08D * Mth.sin(u * (float) Math.PI) + 0.06D * u;
                        float wa = wBase + u * twistDir;
                        Vec3 wpt = new Vec3(Mth.cos(wa) * wr, wy, Mth.sin(wa) * wr);
                        if (prevW != null) {
                            Vec3 perp = new Vec3(-Mth.sin(wa) * 0.038D, 0.022D, Mth.cos(wa) * 0.038D);
                            emitDoubleSidedQuad(m, vc, prevW.subtract(perp), prevW.add(perp), wpt.add(perp), wpt.subtract(perp),
                                    0.29F, 0.15F, 0.08F, 0.94F);
                            Vec3 emberPerp = perp.scale(0.38D);
                            emitDoubleSidedQuad(m, vc, prevW.subtract(emberPerp), prevW.add(emberPerp), wpt.add(emberPerp), wpt.subtract(emberPerp),
                                    1.00F, 0.82F, 0.36F, 0.92F * flicker);
                        }
                        prevW = wpt;
                    }
                }

                // 6 roaring 3D hellfire flame ribbons curling upward around the wicker cage
                for (int f = 0; f < 6; f++) {
                    float fBase = -age * 0.16F + (float) (Math.PI * 2.0D * f / 6.0D);
                    Vec3 prevF = null;
                    for (int s = 0; s <= 10; s++) {
                        float u = s / 10.0F;
                        double fy = 0.08D + u * 2.05D;
                        double fr = 0.50D * (1.0D - 0.35D * u) + 0.06D * Mth.sin(age * 0.32F + u * 6.0F + f);
                        float fa = fBase + u * 3.6F;
                        Vec3 fpt = new Vec3(Mth.cos(fa) * fr, fy, Mth.sin(fa) * fr);
                        if (prevF != null) {
                            Vec3 perp = new Vec3(-Mth.sin(fa) * 0.055D * (1.0F - 0.5F * u), 0.03D, Mth.cos(fa) * 0.055D * (1.0F - 0.5F * u));
                            boolean gold = (f % 2 == 0);
                            emitDoubleSidedQuad(m, vc, prevF.subtract(perp), prevF.add(perp), fpt.add(perp), fpt.subtract(perp),
                                    gold ? 1.00F : 0.72F, gold ? 0.82F : 0.24F, gold ? 0.36F : 0.03F,
                                    0.86F * (1.0F - 0.35F * u) * flicker);
                        }
                        prevF = fpt;
                    }
                }

                // Blazing hellfire crown atop the Wicker Effigy
                Vec3 crown = new Vec3(0.0D, 2.06D + 0.05D * Mth.sin(age * 0.18F), 0.0D);
                renderOctahedron(m, vc, crown, 0.18F, 0.34F, -age * 0.14F, 1.00F, 0.85F, 0.38F, 0.96F * flicker);
                renderOctahedron(m, vc, crown, 0.26F, 0.26F, age * 0.10F, 0.72F, 0.24F, 0.03F, 0.84F * flicker);
            } else if (state.materia == EssenceType.RADIANT) {
                // Section 6.8 Solar Seraph Decoy (#FFE278): folded luminous 6-wing Seraph sanctuary, overhead Halo & pulsing restoration rings!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float healPulse = (age * 0.048F) % 1.0F;
                float healR = 0.42F + healPulse * 1.45F;
                renderLocalRing(m, vc, new Vec3(0.0D, 0.06D, 0.0D), flatX, flatZ,
                        healR * 0.86F, healR, 28, 1.00F, 0.886F, 0.471F, 0.88F * (1.0F - healPulse) * flicker);
                renderLocalRing(m, vc, new Vec3(0.0D, 2.08D + 0.04D * Mth.sin(age * 0.12F), 0.0D), flatX, flatZ,
                        0.32F, 0.44F, 24, 1.00F, 0.99F, 0.92F, 0.96F * flicker);

                // 6 gracefully folded Solar Seraph wings cradling the Decoy effigy
                for (int w = 0; w < 6; w++) {
                    float wAng = age * 0.03F + (float) (Math.PI * 2.0D * w / 6.0D);
                    double breath = 0.04D * Mth.sin(age * 0.14F + w);
                    Vec3 rad = new Vec3(Mth.cos(wAng), 0.0D, Mth.sin(wAng));
                    Vec3 tang = new Vec3(-Mth.sin(wAng), 0.0D, Mth.cos(wAng));
                    Vec3 b0 = rad.scale(0.48D + breath).subtract(tang.scale(0.18D)).add(0.0D, 0.24D, 0.0D);
                    Vec3 b1 = rad.scale(0.48D + breath).add(tang.scale(0.18D)).add(0.0D, 0.24D, 0.0D);
                    Vec3 t1 = rad.scale(0.36D + breath).add(tang.scale(0.08D)).add(0.0D, 1.84D, 0.0D);
                    Vec3 t0 = rad.scale(0.36D + breath).subtract(tang.scale(0.08D)).add(0.0D, 1.84D, 0.0D);
                    boolean ivoryFeather = (w % 2 == 0);
                    emitDoubleSidedQuad(m, vc, b0, b1, t1, t0,
                            1.00F, ivoryFeather ? 0.99F : 0.886F, ivoryFeather ? 0.94F : 0.471F, 0.72F * flicker);
                }
            } else if (state.materia == EssenceType.UMBRAL) {
                // Section 6.9 Shadow Stalker Decoy (#211A21): shifting shadow-mirror ribbons framing copied player equipment!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.04D, 0.0D), flatX, flatZ, 0.15F, 0.88F, 24,
                        0.085F, 0.058F, 0.095F, 0.88F * flicker);
                for (int rIdx = 0; rIdx < 4; rIdx++) {
                    float rBase = -age * 0.12F + (float) (Math.PI * 0.5D * rIdx);
                    Vec3 prevR = null;
                    for (int s = 0; s <= 10; s++) {
                        float u = s / 10.0F;
                        double ry = 0.06D + u * 1.92D;
                        double rr = 0.48D + 0.08D * Mth.sin(age * 0.22F + u * 5.0F + rIdx);
                        float ra = rBase + u * 2.8F;
                        Vec3 rpt = new Vec3(Mth.cos(ra) * rr, ry, Mth.sin(ra) * rr);
                        if (prevR != null) {
                            Vec3 perp = new Vec3(-Mth.sin(ra) * 0.055D, 0.025D, Mth.cos(ra) * 0.055D);
                            boolean violet = (rIdx % 2 == 1);
                            emitDoubleSidedQuad(m, vc, prevR.subtract(perp), prevR.add(perp), rpt.add(perp), rpt.subtract(perp),
                                    violet ? 0.56F : 0.129F, violet ? 0.36F : 0.102F, violet ? 1.00F : 0.129F, 0.82F * flicker);
                        }
                        prevR = rpt;
                    }
                }
            } else if (state.materia == EssenceType.UNDEAD) {
                // Section 6.10 Cryptguard Skeletal Effigy Decoy (#4D6453): ghostly skeletal ribcage effigy,
                // floating skull visage, swirling necrotic soul wisps & volatile Bone-Nova heart core!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 0.0D).add(0.0D, 0.0D, 1.0D);
                float tauntPulse = (age * 0.052F) % 1.0F;
                float tauntR = 0.44F + tauntPulse * 1.35F;
                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ,
                        tauntR * 0.86F, tauntR, 24, 0.369F, 0.878F, 0.533F, 0.82F * (1.0F - tauntPulse) * flicker);

                // Central bleached-bone spinal column & 5 pairs of curved 3D skeletal ribs
                for (int rIdx = 0; rIdx < 5; rIdx++) {
                    double ry = 0.62D + rIdx * 0.21D;
                    float ribR = 0.36F + 0.08F * Mth.sin((rIdx / 4.0F) * (float) Math.PI);
                    renderLocalRing(m, vc, new Vec3(0.0D, ry, 0.0D), flatX, flatZ,
                            ribR * 0.84F, ribR, 16, 0.902F, 0.875F, 0.812F, 0.88F * flicker);
                }

                // Volatile pulsing #5EE088 Bone-Nova corpse-explosion heart core inside the ribcage!
                float heartBeat = 0.84F + 0.24F * Mth.sin(age * 0.34F);
                Vec3 heartPos = new Vec3(0.0D, 1.06D, 0.0D);
                renderOctahedron(m, vc, heartPos, 0.16F * heartBeat, 0.24F * heartBeat, age * 0.16F,
                        0.369F, 0.878F, 0.533F, 0.96F * flicker);
                renderOctahedron(m, vc, heartPos, 0.24F * heartBeat, 0.18F * heartBeat, -age * 0.12F,
                        0.302F, 0.392F, 0.325F, 0.72F * flicker);

                // Floating ghostly #E6DFCF ossuary skull crown above the skeletal effigy
                Vec3 skullCrown = new Vec3(0.0D, 1.96D + 0.05D * Mth.sin(age * 0.15F), 0.0D);
                renderOctahedron(m, vc, skullCrown, 0.19F, 0.22F, age * 0.05F, 0.902F, 0.875F, 0.812F, 0.94F * flicker);
                renderLocalRing(m, vc, skullCrown.add(0.0D, 0.02D, 0.0D), flatX, flatZ, 0.22F, 0.29F, 18,
                        0.369F, 0.878F, 0.533F, 0.88F * flicker);

                // 3 rising #5EE088 necrotic ectoplasm soul-ribbons spiraling around the skeletal effigy
                for (int s = 0; s < 3; s++) {
                    float sBase = age * 0.14F + (float) (Math.PI * 2.0D * s / 3.0D);
                    Vec3 prevS = null;
                    for (int step = 0; step <= 10; step++) {
                        float u = step / 10.0F;
                        double sy = 0.08D + u * 1.94D;
                        double sr = 0.50D + 0.06D * Mth.sin(age * 0.20F + u * 5.0F + s);
                        float sa = sBase + u * 3.4F;
                        Vec3 spt = new Vec3(Mth.cos(sa) * sr, sy, Mth.sin(sa) * sr);
                        if (prevS != null) {
                            Vec3 perp = new Vec3(-Mth.sin(sa) * 0.046D, 0.024D, Mth.cos(sa) * 0.046D);
                            emitDoubleSidedQuad(m, vc, prevS.subtract(perp), prevS.add(perp), spt.add(perp), spt.subtract(perp),
                                    (s == 0) ? 0.369F : 0.302F,
                                    (s == 0) ? 0.878F : 0.392F,
                                    (s == 0) ? 0.533F : 0.325F,
                                    0.80F * (1.0F - 0.3F * u) * flicker);
                        }
                        prevS = spt;
                    }
                }
            } else if (state.materia == EssenceType.VOID) {
                // Section 6.11 Void Singularity Silhouette Decoy (#41304E): celestial cosmic silhouette,
                // dark event-horizon heart core (ready to collapse into a 5m micro-singularity on death!) & nebula ribbons!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float inwardWave = 1.0F - ((age * 0.048F) % 1.0F);
                float waveR = 0.38F + inwardWave * 1.35F;
                renderLocalRing(m, vc, new Vec3(0.0D, 0.05D, 0.0D), flatX, flatZ,
                        waveR * 0.86F, waveR, 26, 0.780F, 0.490F, 1.000F, 0.84F * (1.0F - inwardWave) * flicker);

                // Pitch-black micro-singularity heart core + #C77DFF accretion halo inside chest
                float pulse = 0.88F + 0.20F * Mth.sin(age * 0.30F);
                Vec3 bhHeart = new Vec3(0.0D, 1.12D, 0.0D);
                renderOctahedron(m, vc, bhHeart, 0.22F * pulse, 0.26F * pulse, -age * 0.16F,
                        0.045F, 0.025F, 0.070F, 0.98F * flicker);
                renderLocalRing(m, vc, bhHeart, flatX, flatZ, 0.26F * pulse, 0.36F * pulse, 20,
                        0.780F, 0.490F, 1.000F, 0.92F * flicker);

                // Celestial crown halo above silhouette head
                Vec3 crown = new Vec3(0.0D, 1.96D + 0.04D * Mth.sin(age * 0.14F), 0.0D);
                renderLocalRing(m, vc, crown, flatX, flatZ, 0.24F, 0.32F, 20,
                        0.255F, 0.188F, 0.306F, 0.92F * flicker);
                renderOctahedron(m, vc, crown, 0.12F, 0.16F, age * 0.12F,
                        0.780F, 0.490F, 1.000F, 0.96F * flicker);

                // 4 cosmic #41304E / #C77DFF starfield nebula ribbons wrapping the celestial silhouette
                for (int s = 0; s < 4; s++) {
                    float sBase = -age * 0.13F + (float) (Math.PI * 0.5D * s);
                    Vec3 prevS = null;
                    for (int step = 0; step <= 10; step++) {
                        float u = step / 10.0F;
                        double sy = 0.08D + u * 1.92D;
                        double sr = 0.48D + 0.08D * Mth.sin(age * 0.22F + u * 5.0F + s);
                        float sa = sBase + u * 3.1F;
                        Vec3 spt = new Vec3(Mth.cos(sa) * sr, sy, Mth.sin(sa) * sr);
                        if (prevS != null) {
                            Vec3 perp = new Vec3(-Mth.sin(sa) * 0.050D, 0.025D, Mth.cos(sa) * 0.050D);
                            boolean bright = (s % 2 == 0);
                            emitDoubleSidedQuad(m, vc, prevS.subtract(perp), prevS.add(perp), spt.add(perp), spt.subtract(perp),
                                    bright ? 0.780F : 0.255F,
                                    bright ? 0.490F : 0.188F,
                                    bright ? 1.000F : 0.306F,
                                    0.84F * flicker);
                        }
                        prevS = spt;
                    }
                }
            } else if (state.materia == EssenceType.ARID) {
                // Section 6.12 Heat Mirage Decoy (#C2B28F): shimmering wavy desert heat-haze rings,
                // volatile compressed dust-blast core & 4 whirling sandstorm mirage ribbons!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                for (int h = 0; h < 4; h++) {
                    double hy = 0.22D + h * 0.46D;
                    double shimmerX = 0.065D * Mth.sin(age * 0.28F + h * 1.4F);
                    double shimmerZ = 0.065D * Mth.cos(age * 0.24F + h * 1.4F);
                    float hr = 0.46F + 0.08F * Mth.sin(age * 0.20F + h);
                    renderLocalRing(m, vc, new Vec3(shimmerX, hy, shimmerZ), flatX, flatZ,
                            hr * 0.82F, hr, 22,
                            (h % 2 == 0) ? 0.961F : 0.761F,
                            (h % 2 == 0) ? 0.894F : 0.698F,
                            (h % 2 == 0) ? 0.745F : 0.561F,
                            0.78F * flicker);
                }

                // Volatile desert dust-blast heart core inside chest
                float pulse = 0.88F + 0.20F * Mth.sin(age * 0.34F);
                Vec3 dustHeart = new Vec3(0.0D, 1.10D, 0.0D);
                renderOctahedron(m, vc, dustHeart, 0.22F * pulse, 0.28F * pulse, age * 0.22F,
                        0.612F, 0.478F, 0.298F, 0.94F * flicker);
                renderLocalRing(m, vc, dustHeart, flatX, flatZ, 0.25F * pulse, 0.36F * pulse, 20,
                        0.961F, 0.894F, 0.745F, 0.92F * flicker);

                // 4 swirling sandstorm mirage ribbons wrapping the shimmering silhouette
                for (int s = 0; s < 4; s++) {
                    float sBase = age * 0.18F + (float) (Math.PI * 0.5D * s);
                    Vec3 prevS = null;
                    for (int step = 0; step <= 10; step++) {
                        float u = step / 10.0F;
                        double sy = 0.08D + u * 1.88D;
                        double sr = 0.46D + 0.10D * Mth.sin(age * 0.26F + u * 5.5F + s);
                        float sa = sBase + u * 3.4F;
                        Vec3 spt = new Vec3(Mth.cos(sa) * sr, sy, Mth.sin(sa) * sr);
                        if (prevS != null) {
                            Vec3 perp = new Vec3(-Mth.sin(sa) * 0.048D, 0.024D, Mth.cos(sa) * 0.048D);
                            boolean sunlit = (s % 2 == 0);
                            emitDoubleSidedQuad(m, vc, prevS.subtract(perp), prevS.add(perp), spt.add(perp), spt.subtract(perp),
                                    sunlit ? 0.961F : 0.761F,
                                    sunlit ? 0.894F : 0.698F,
                                    sunlit ? 0.745F : 0.561F,
                                    0.80F * flicker);
                        }
                        prevS = spt;
                    }
                }
            } else if (state.materia == EssenceType.LIGHTNING) {
                // Section 6.13 Volt-Spark Decoy (#FFE600): crackling high-voltage Faraday rings & snapping electric arcs
                // warning hostiles that passing through or touching the decoy delivers a live shock!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 0.0D + 1.0D);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.05D, 0.0D), flatX, flatZ, 0.52F, 0.72F, 24,
                        1.00F, 0.902F, 0.00F, 0.92F * flicker);
                Vec3 voltHeart = new Vec3(0.0D, 1.12D, 0.0D);
                renderOctahedron(m, vc, voltHeart, 0.24F, 0.24F, age * 0.35F, 1.00F, 1.00F, 0.95F, 0.98F * flicker);
                renderOctahedron(m, vc, voltHeart, 0.32F, 0.32F, -age * 0.28F, 1.00F, 0.902F, 0.00F, 0.78F * flicker);

                for (int s = 0; s < 4; s++) {
                    float sBase = age * 0.26F + (float) (Math.PI * 0.5D * s);
                    Vec3 prevS = null;
                    for (int step = 0; step <= 8; step++) {
                        float u = step / 8.0F;
                        double sy = 0.08D + u * 1.88D;
                        double sr = 0.48D + ((step & 1) == 0 ? 0.09D : -0.09D);
                        float sa = sBase + u * 3.8F;
                        Vec3 spt = new Vec3(Mth.cos(sa) * sr, sy, Mth.sin(sa) * sr);
                        if (prevS != null) {
                            Vec3 perp = new Vec3(-Mth.sin(sa) * 0.044D, 0.022D, Mth.cos(sa) * 0.044D);
                            boolean whiteHot = (s % 2 == 0);
                            emitDoubleSidedQuad(m, vc, prevS.subtract(perp), prevS.add(perp), spt.add(perp), spt.subtract(perp),
                                    whiteHot ? 1.00F : 1.00F,
                                    whiteHot ? 1.00F : 0.902F,
                                    whiteHot ? 0.95F : 0.00F,
                                    0.90F * flicker);
                        }
                        prevS = spt;
                    }
                }
            } else if (state.materia == EssenceType.MAGMA) {
                // Section 6.14 Caldera Nova Effigy (#B83C08): glowing volcanic lava effigy wrapped in cracked dark pumice rings,
                // volatile pulsing Caldera Nova magma heart core & 4 rising liquid-lava ribbons!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float tauntWave = (age * 0.048F) % 1.0F;
                float tauntR = 0.48F + tauntWave * 1.45F;
                renderLocalRing(m, vc, new Vec3(0.0D, 0.05D, 0.0D), flatX, flatZ,
                        tauntR * 0.85F, tauntR, 26, 0.722F, 0.235F, 0.031F, 0.86F * (1.0F - tauntWave) * flicker);

                float magmaBeat = 0.86F + 0.24F * Mth.sin(age * 0.32F);
                Vec3 calderaHeart = new Vec3(0.0D, 1.10D, 0.0D);
                renderOctahedron(m, vc, calderaHeart, 0.26F * magmaBeat, 0.28F * magmaBeat, age * 0.20F,
                        0.722F, 0.235F, 0.031F, 0.94F * flicker);
                renderOctahedron(m, vc, calderaHeart, 0.18F * magmaBeat, 0.20F * magmaBeat, -age * 0.28F,
                        1.00F, 0.820F, 0.361F, 0.99F * flicker);
                renderLocalRing(m, vc, calderaHeart, flatX, flatZ, 0.28F * magmaBeat, 0.38F * magmaBeat, 18,
                        0.165F, 0.090F, 0.071F, 0.90F * flicker);

                for (int s = 0; s < 4; s++) {
                    float sBase = age * 0.16F + (float) (Math.PI * 0.5D * s);
                    Vec3 prevS = null;
                    for (int step = 0; step <= 9; step++) {
                        float u = step / 9.0F;
                        double sy = 0.08D + u * 1.88D;
                        double sr = 0.48D + 0.08D * Mth.sin(age * 0.24F + u * 5.0F + s);
                        float sa = sBase + u * 3.2F;
                        Vec3 spt = new Vec3(Mth.cos(sa) * sr, sy, Mth.sin(sa) * sr);
                        if (prevS != null) {
                            Vec3 perp = new Vec3(-Mth.sin(sa) * 0.052D, 0.025D, Mth.cos(sa) * 0.052D);
                            boolean moltenGold = (s % 2 == 0);
                            emitDoubleSidedQuad(m, vc, prevS.subtract(perp), prevS.add(perp), spt.add(perp), spt.subtract(perp),
                                    moltenGold ? 1.00F : 0.722F,
                                    moltenGold ? 0.820F : 0.235F,
                                    moltenGold ? 0.361F : 0.031F,
                                    0.88F * flicker);
                        }
                        prevS = spt;
                    }
                }
            } else if (state.materia == EssenceType.STORM) {
                // Section 6.15 Tempest Nova Effigy (#429BA3): spinning hurricane waterspout funnel,
                // compressed Tempest Nova heart core & 4 spiraling monsoon wind-and-rain ribbons!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                for (int h = 0; h < 5; h++) {
                    double hy = 0.16D + h * 0.40D;
                    float hr = 0.34F + h * 0.09F;
                    boolean foam = (h % 2 == 0);
                    renderLocalRing(m, vc, new Vec3(0.0D, hy, 0.0D), flatX, flatZ, hr * 0.82F, hr, 22,
                            foam ? 0.847F : 0.259F,
                            foam ? 0.980F : 0.608F,
                            foam ? 1.000F : 0.639F,
                            0.84F * flicker);
                }
                Vec3 stormHeart = new Vec3(0.0D, 1.12D, 0.0D);
                renderOctahedron(m, vc, stormHeart, 0.22F, 0.28F, age * 0.36F, 0.847F, 0.980F, 1.000F, 0.98F * flicker);
                renderOctahedron(m, vc, stormHeart, 0.30F, 0.34F, -age * 0.28F, 0.259F, 0.608F, 0.639F, 0.80F * flicker);
            } else if (state.materia == EssenceType.GLACIAL) {
                // Section 6.16 Crystalline Freeze-Nova Effigy & Ice Statue Blink (#98DCF2):
                // 6 translucent Blue-Ice / Pale-Cryo prism slabs, 4m freeze-nova warning ring & diamond-frost crown!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float cryoPulse = (age * 0.055F) % 1.0F;
                float cryoR = 0.48F + cryoPulse * 1.25F;
                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ,
                        cryoR * 0.85F, cryoR, 6, 0.88F, 1.00F, 1.00F, 0.85F * (1.0F - cryoPulse) * flicker);
                renderOpenFrustumWall(m, vc, 0.06F, 1.92F, 0.58F, 0.48F, 6, -age * 0.02F,
                        0.596F, 0.863F, 0.949F, 0.40F * flicker);
                for (int p = 0; p < 6; p++) {
                    float pAng = age * 0.025F + (float) (Math.PI * 2.0D * p / 6.0D);
                    Vec3 rad = new Vec3(Mth.cos(pAng), 0.0D, Mth.sin(pAng));
                    Vec3 tang = new Vec3(-Mth.sin(pAng), 0.0D, Mth.cos(pAng));
                    Vec3 b0 = rad.scale(0.54D).subtract(tang.scale(0.18D)).add(0.0D, 0.14D, 0.0D);
                    Vec3 b1 = rad.scale(0.54D).add(tang.scale(0.18D)).add(0.0D, 0.14D, 0.0D);
                    Vec3 t1 = rad.scale(0.44D).add(tang.scale(0.13D)).add(0.0D, 1.84D, 0.0D);
                    Vec3 t0 = rad.scale(0.44D).subtract(tang.scale(0.13D)).add(0.0D, 1.84D, 0.0D);
                    boolean pale = (p % 2 == 0);
                    emitDoubleSidedQuad(m, vc, b0, b1, t1, t0,
                            pale ? 0.596F : 0.173F, pale ? 0.863F : 0.510F, pale ? 0.949F : 0.788F, 0.68F * flicker);
                }
                Vec3 crown = new Vec3(0.0D, 2.10D + 0.05D * Mth.sin(age * 0.14F), 0.0D);
                renderOctahedron(m, vc, crown, 0.18F, 0.32F, -age * 0.10F, 0.88F, 1.00F, 1.00F, 0.98F * flicker);
                renderOctahedron(m, vc, crown, 0.25F, 0.22F, age * 0.07F, 0.173F, 0.510F, 0.788F, 0.82F * flicker);
            } else if (state.materia == EssenceType.OVERGROWTH) {
                // Section 6.17 Overgrowth Floral Effigy (#1A5923): mossy-stone snare ring, twining briar vines & jungle blossom crown!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float snarePulse = (age * 0.05F) % 1.0F;
                float snareR = 0.48F + snarePulse * 1.25F;
                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ,
                        snareR * 0.86F, snareR, 18, 0.173F, 0.902F, 0.408F, 0.84F * (1.0F - snarePulse) * flicker);
                renderOpenFrustumWall(m, vc, 0.06F, 1.88F, 0.54F, 0.42F, 6, age * 0.025F,
                        0.102F, 0.349F, 0.137F, 0.46F * flicker);
                Vec3 floralCrown = new Vec3(0.0D, 2.08D + 0.05D * Mth.sin(age * 0.12F), 0.0D);
                renderOctahedron(m, vc, floralCrown, 0.24F, 0.18F, age * 0.08F, 1.000F, 0.580F, 0.760F, 0.96F * flicker);
                renderOctahedron(m, vc, floralCrown, 0.14F, 0.28F, -age * 0.11F, 1.000F, 0.847F, 0.239F, 0.99F * flicker);
            } else if (state.materia == EssenceType.DUST) {
                // Section 6.18 Whiteout Chalk Silhouette & Chalk-Step Decoy (#C2B28F):
                // dense swirling #F2ECE1 / #C2B28F chalk-powder rings, volatile 5m Whiteout Nova core & 4 chalk ribbons!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float powderWave = (age * 0.052F) % 1.0F;
                float powderR = 0.46F + powderWave * 1.35F;
                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ,
                        powderR * 0.85F, powderR, 26, 0.949F, 0.925F, 0.882F, 0.86F * (1.0F - powderWave) * flicker);
                renderOpenFrustumWall(m, vc, 0.06F, 1.88F, 0.50F, 0.58F, 18, age * 0.14F,
                        0.949F, 0.925F, 0.882F, 0.34F * flicker);

                float corePulse = 0.88F + 0.22F * Mth.sin(age * 0.36F);
                Vec3 chalkCore = new Vec3(0.0D, 1.10D, 0.0D);
                renderOctahedron(m, vc, chalkCore, 0.24F * corePulse, 0.26F * corePulse, age * 0.24F,
                        0.949F, 0.925F, 0.882F, 0.96F * flicker);
                renderLocalRing(m, vc, chalkCore, flatX, flatZ, 0.28F * corePulse, 0.38F * corePulse, 20,
                        0.761F, 0.698F, 0.561F, 0.92F * flicker);
            } else if (state.materia == EssenceType.SPORE) {
                // Section 6.19 Mushroom Decoy (#1A5923 / #A88654): plump spotted fungal toadstool effigy cap,
                // golden gill collar & luring calming bio-pollen rings!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float lureWave = (age * 0.042F) % 1.0F;
                float lureR = 0.50F + lureWave * 1.45F;
                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ,
                        lureR * 0.86F, lureR, 26, 0.910F, 0.784F, 0.510F, 0.84F * (1.0F - lureWave) * flicker);
                renderCylinderFrustum(m, vc, 1.62F, 1.88F, 0.82F, 0.48F, 14, age * 0.04F,
                        0.102F, 0.349F, 0.137F, 0.92F * flicker);
                renderCylinderFrustum(m, vc, 1.88F, 2.08F, 0.48F, 0.08F, 14, age * 0.04F,
                        0.173F, 0.902F, 0.408F, 0.90F * flicker);
                renderLocalRing(m, vc, new Vec3(0.0D, 1.61D, 0.0D), flatX, flatZ, 0.22F, 0.80F, 20,
                        0.910F, 0.784F, 0.510F, 0.90F * flicker);
            } else if (state.materia == EssenceType.TAIGA) {
                // Section 6.20 Phantom Snow Stalker Decoy (#1A5923 / #98DCF2): frosted evergreen conifer effigy boughs
                // & swirling snow-whiteout taunt rings!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float tauntWave = (age * 0.048F) % 1.0F;
                float tauntR = 0.55F + tauntWave * 1.45F;
                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ,
                        tauntR * 0.86F, tauntR, 26, 0.596F, 0.863F, 0.949F, 0.86F * (1.0F - tauntWave) * flicker);
                renderCylinderFrustum(m, vc, 1.15F, 1.62F, 0.72F, 0.32F, 10, age * 0.05F,
                        0.102F, 0.349F, 0.137F, 0.88F * flicker);
                renderCylinderFrustum(m, vc, 1.56F, 2.02F, 0.52F, 0.08F, 10, -age * 0.05F,
                        0.596F, 0.863F, 0.949F, 0.86F * flicker);
            } else if (state.materia == EssenceType.OASIS) {
                // Section 6.21 Mirage Blossom Effigy Decoy (#1A5923 / #C2B28F): shimmering desert-lotus blossom crown
                // & pacifying springwater mirage rings!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float mirageWave = (age * 0.045F) % 1.0F;
                float mirageR = 0.55F + mirageWave * 1.45F;
                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ,
                        mirageR * 0.86F, mirageR, 26, 0.220F, 0.780F, 0.960F, 0.86F * (1.0F - mirageWave) * flicker);
                renderCylinderFrustum(m, vc, 1.52F, 1.82F, 0.36F, 0.78F, 8, age * 0.04F,
                        0.102F, 0.349F, 0.137F, 0.90F * flicker);
                renderCylinderFrustum(m, vc, 1.78F, 2.04F, 0.64F, 0.18F, 8, -age * 0.04F,
                        0.761F, 0.698F, 0.561F, 0.92F * flicker);
            } else if (state.materia == EssenceType.VITAE) {
                // Section 6.22 Heart-Husk Effigy Decoy (#FF6B9D): translucent rose-ribbed heart chrysalis
                // & expanding 5-block heartbeat restoration rings!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float beat = 0.88F + 0.16F * Mth.abs(Mth.sin(age * 0.22F));
                float heartWave = (age * 0.052F) % 1.0F;
                float heartR = 0.52F + heartWave * 1.48F;
                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ,
                        heartR * 0.86F, heartR, 26, 1.000F, 0.420F, 0.616F, 0.88F * (1.0F - heartWave) * flicker);
                renderOpenFrustumWall(m, vc, 0.12F, 1.88F, 0.54F, 0.44F, 10, age * 0.04F,
                        1.000F, 0.420F, 0.616F, 0.42F * flicker);
                Vec3 heartCore = new Vec3(0.0D, 1.12D, 0.0D);
                renderOctahedron(m, vc, heartCore, 0.26F * beat, 0.34F * beat, age * 0.16F,
                        1.000F, 0.420F, 0.616F, 0.94F * flicker);
                renderOctahedron(m, vc, heartCore, 0.16F * beat, 0.22F * beat, -age * 0.22F,
                        1.000F, 0.820F, 0.890F, 0.98F * flicker);
            } else if (state.materia == EssenceType.BLOOD) {
                // Section 6.23 Crimson Effigy Decoy (#8A0303): hardened crimson blood-cage,
                // boiling venous vapor funnel & volatile 5-block boiling blood heart core!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float boil = 0.88F + 0.18F * Mth.abs(Mth.sin(age * 0.26F));
                float boilWave = (age * 0.055F) % 1.0F;
                float boilR = 0.50F + boilWave * 1.52F;
                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ,
                        boilR * 0.85F, boilR, 26, 0.541F, 0.012F, 0.012F, 0.90F * (1.0F - boilWave) * flicker);
                renderOpenFrustumWall(m, vc, 0.10F, 1.90F, 0.48F, 0.62F, 10, -age * 0.06F,
                        0.541F, 0.012F, 0.012F, 0.46F * flicker);
                for (int sp = 0; sp < 6; sp++) {
                    float spAng = age * 0.08F + (float) (Math.PI * 2.0D * sp / 6.0D);
                    Vec3 spPos = new Vec3(Mth.cos(spAng) * 0.50D, 0.85D + (sp % 2) * 0.35D, Mth.sin(spAng) * 0.50D);
                    renderOctahedron(m, vc, spPos, 0.08F, 0.26F, spAng, 0.541F, 0.012F, 0.012F, 0.92F * flicker);
                }
                Vec3 bloodCore = new Vec3(0.0D, 1.12D, 0.0D);
                renderOctahedron(m, vc, bloodCore, 0.26F * boil, 0.34F * boil, age * 0.18F,
                        0.541F, 0.012F, 0.012F, 0.95F * flicker);
                renderOctahedron(m, vc, bloodCore, 0.16F * boil, 0.22F * boil, -age * 0.24F,
                        1.000F, 0.220F, 0.298F, 0.99F * flicker);
            } else if (state.materia == EssenceType.AURA) {
                // Section 6.24 Spirit-Vane Effigy Decoy (#FF6B9D / #A88654): spinning golden-pink prayer-wheel cyclone,
                // sweeping wind-vane blades & inward 5m vacuum implosion rings!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float inWave = 1.0F - ((age * 0.050F) % 1.0F);
                float inR = 0.45F + inWave * 1.45F;
                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ,
                        inR * 0.86F, inR, 26, 0.839F, 0.706F, 0.471F, 0.88F * (1.0F - inWave) * flicker);
                for (int c = 0; c < 4; c++) {
                    float cAng = age * 0.34F + (float) (Math.PI * 0.5D * c);
                    double cy = 0.46D + 0.34D * c;
                    boolean pink = (c % 2 == 0);
                    renderSweptArcBlade(m, vc, new Vec3(0.0D, cy, 0.0D), 0.52F + 0.06F * c, cAng, 1.15F, 0.095F,
                            pink ? 1.000F : 0.839F,
                            pink ? 0.420F : 0.706F,
                            pink ? 0.616F : 0.471F,
                            0.90F * flicker);
                }
            } else if (state.materia == EssenceType.AMBER) {
                // Section 6.25 Petrified Amber Effigy Decoy (#FF6B9D / #773214 / #F29B2E): hexagonal golden-amber
                // stasis chrysalis encasing a suspended #FF6B9D vital crystal & 5m sticky-root shatter rings!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ,
                        0.56F, 0.86F, 6, 0.467F, 0.196F, 0.078F, 0.92F * flicker);
                renderOpenFrustumWall(m, vc, 0.06F, 1.84F, 0.50F, 0.44F, 6, age * 0.04F,
                        0.949F, 0.608F, 0.180F, 0.42F * flicker);
                Vec3 amberHeart = new Vec3(0.0D, 1.12D, 0.0D);
                renderOctahedron(m, vc, amberHeart, 0.24F, 0.36F, age * 0.10F,
                        1.000F, 0.420F, 0.616F, 0.96F * flicker);
            } else if (state.materia == EssenceType.ECLIPSE) {
                // Section 6.26 Penumbra Silhouette Effigy Decoy (#FFEA78 / #211A21): inward-pulling twilight vortex
                // & 4 sweeping golden coronal flare blades around the abyssal silhouette!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float pullWave = 1.0F - ((age * 0.052F) % 1.0F);
                float pullR = 0.42F + pullWave * 1.50F;
                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ,
                        pullR * 0.86F, pullR, 26, 1.000F, 0.918F, 0.471F, 0.90F * (1.0F - pullWave) * flicker);
                for (int c = 0; c < 4; c++) {
                    float cAng = -age * 0.36F + (float) (Math.PI * 0.5D * c);
                    double cy = 0.44D + 0.34D * c;
                    boolean gold = (c % 2 == 0);
                    renderSweptArcBlade(m, vc, new Vec3(0.0D, cy, 0.0D), 0.54F + 0.05F * c, cAng, 1.25F, 0.10F,
                            gold ? 1.000F : 0.129F,
                            gold ? 0.918F : 0.102F,
                            gold ? 0.471F : 0.129F,
                            0.92F * flicker);
                }
            } else if (state.materia == EssenceType.BLIGHT) {
                // Section 6.27 Cadaverous Effigy Decoy (#4D6453 / #34CC48): rotting bone-briar cage & virulent miasma rings!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ,
                        0.54F, 0.86F, 14, 0.302F, 0.392F, 0.325F, 0.92F * flicker);
                for (int r = 0; r < 6; r++) {
                    float rAng = age * 0.10F + (float) (Math.PI * 2.0D * r / 6.0D);
                    Vec3 thorn = new Vec3(Mth.cos(rAng) * 0.56D, 0.72D + (r % 2) * 0.42D, Mth.sin(rAng) * 0.56D);
                    boolean bone = (r % 2 == 0);
                    renderOctahedron(m, vc, thorn, 0.085F, 0.24F, rAng,
                            bone ? 0.902F : 0.204F,
                            bone ? 0.875F : 0.800F,
                            bone ? 0.812F : 0.282F,
                            0.94F * flicker);
                }
            } else if (state.materia == EssenceType.ASTRAL) {
                // Section 6.28 Cosmic Silhouette Effigy Decoy (#41304E / #FFE600): star-matter singularity rings & EMP arcs!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ,
                        0.52F, 0.86F, 24, 0.255F, 0.188F, 0.306F, 0.92F * flicker);
                renderLocalRing(m, vc, new Vec3(0.0D, 1.12D, 0.0D), flatX, flatZ,
                        0.38F, 0.50F, 22, 1.000F, 0.902F, 0.000F, 0.94F * flicker);
                for (int s = 0; s < 6; s++) {
                    float sAng = age * 0.16F + (float) (Math.PI * 2.0D * s / 6.0D);
                    Vec3 star = new Vec3(Mth.cos(sAng) * 0.58D, 0.68D + (s % 2) * 0.52D, Mth.sin(sAng) * 0.58D);
                    boolean gold = (s % 2 == 0);
                    renderOctahedron(m, vc, star, 0.085F, 0.18F, sAng,
                            gold ? 1.000F : 0.255F,
                            gold ? 0.902F : 0.188F,
                            gold ? 0.000F : 0.306F,
                            0.95F * flicker);
                }
            } else if (state.materia == EssenceType.SOULFIRE) {
                // Section 6.29 Howling Soul Effigy Decoy (#1DF0E5 / #0D6469): roaring cyan soulfire rings & wailing soul wisps!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ,
                        0.54F, 0.88F, 22, 0.051F, 0.392F, 0.412F, 0.92F * flicker);
                renderLocalRing(m, vc, new Vec3(0.0D, 1.08D, 0.0D), flatX, flatZ,
                        0.36F, 0.48F, 22, 0.114F, 0.941F, 0.898F, 0.94F * flicker);
                for (int w = 0; w < 6; w++) {
                    float wAng = age * 0.15F + (float) (Math.PI * 2.0D * w / 6.0D);
                    Vec3 wisp = new Vec3(Mth.cos(wAng) * 0.56D, 0.65D + (w % 2) * 0.50D, Mth.sin(wAng) * 0.56D);
                    boolean cyan = (w % 2 == 0);
                    renderOctahedron(m, vc, wisp, 0.09F, 0.22F, wAng,
                            cyan ? 0.114F : 0.051F,
                            cyan ? 0.941F : 0.392F,
                            cyan ? 0.898F : 0.412F,
                            0.94F * flicker);
                }
            } else if (state.materia == EssenceType.VAPOR) {
                // Section 6.30 Condensation Effigy Decoy (#E0F7FA / #30A9B0): billowing steam rings & condensing water drops!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ,
                        0.54F, 0.90F, 24, 0.878F, 0.969F, 0.980F, 0.90F * flicker);
                renderLocalRing(m, vc, new Vec3(0.0D, 1.08D, 0.0D), flatX, flatZ,
                        0.38F, 0.52F, 22, 0.188F, 0.663F, 0.690F, 0.92F * flicker);
                for (int d = 0; d < 6; d++) {
                    float dAng = age * 0.14F + (float) (Math.PI * 2.0D * d / 6.0D);
                    Vec3 drop = new Vec3(Mth.cos(dAng) * 0.56D, 0.66D + (d % 2) * 0.48D, Mth.sin(dAng) * 0.56D);
                    boolean steam = (d % 2 == 0);
                    renderOctahedron(m, vc, drop, 0.09F, 0.16F, dAng,
                            steam ? 0.878F : 0.188F,
                            steam ? 0.969F : 0.663F,
                            steam ? 0.980F : 0.690F,
                            0.94F * flicker);
                }
            } else if (state.materia == EssenceType.NULL_R) {
                // Section 6.31 Antimatter Effigy Decoy (#FFFFFF / #41304E): white-hole flare rings & cosmic violet gravity diamonds!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.048D, 0.0D), flatX, flatZ,
                        0.56F, 0.92F, 26, 0.255F, 0.188F, 0.306F, 0.94F * flicker);
                renderLocalRing(m, vc, new Vec3(0.0D, 1.12D, 0.0D), flatX, flatZ,
                        0.40F, 0.56F, 24, 1.000F, 1.000F, 1.000F, 0.96F * flicker);
                for (int d = 0; d < 6; d++) {
                    float dAng = -age * 0.16F + (float) (Math.PI * 2.0D * d / 6.0D);
                    Vec3 flare = new Vec3(Mth.cos(dAng) * 0.58D, 0.68D + (d % 2) * 0.48D, Mth.sin(dAng) * 0.58D);
                    boolean white = (d % 2 == 0);
                    renderOctahedron(m, vc, flare, 0.095F, 0.19F, dAng,
                            white ? 1.000F : 0.255F,
                            white ? 1.000F : 0.188F,
                            white ? 1.000F : 0.306F,
                            0.96F * flicker);
                }
            } else if (state.materia == EssenceType.NULL_U) {
                // Section 6.32 Abyssal Silhouette Effigy Decoy (#110B17 / #41304E): pitch-black accretion hoops & cosmic violet shards!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.048D, 0.0D), flatX, flatZ,
                        0.56F, 0.94F, 26, 0.067F, 0.043F, 0.090F, 0.98F * flicker);
                renderLocalRing(m, vc, new Vec3(0.0D, 1.12D, 0.0D), flatX, flatZ,
                        0.38F, 0.56F, 24, 0.255F, 0.188F, 0.306F, 0.96F * flicker);
                for (int d = 0; d < 6; d++) {
                    float dAng = -age * 0.15F + (float) (Math.PI * 2.0D * d / 6.0D);
                    Vec3 shard = new Vec3(Mth.cos(dAng) * 0.58D, 0.68D + (d % 2) * 0.48D, Mth.sin(dAng) * 0.58D);
                    boolean dark = (d % 2 == 0);
                    renderOctahedron(m, vc, shard, 0.095F, 0.20F, dAng,
                            dark ? 0.067F : 0.255F,
                            dark ? 0.043F : 0.188F,
                            dark ? 0.090F : 0.306F,
                            0.97F * flicker);
                }
            } else if (state.materia == EssenceType.DAWN) {
                // Section 6.33 Daybreak Mirage Effigy Decoy (#FFEA78 / #A88654): sunrise halo & floating wind-feather sun prisms!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.048D, 0.0D), flatX, flatZ,
                        0.56F, 0.94F, 26, 0.659F, 0.525F, 0.329F, 0.92F * flicker);
                renderLocalRing(m, vc, new Vec3(0.0D, 1.14D, 0.0D), flatX, flatZ,
                        0.40F, 0.56F, 24, 1.000F, 0.918F, 0.471F, 0.97F * flicker);
                for (int d = 0; d < 6; d++) {
                    float dAng = age * 0.16F + (float) (Math.PI * 2.0D * d / 6.0D);
                    Vec3 feather = new Vec3(Mth.cos(dAng) * 0.58D, 0.68D + (d % 2) * 0.48D, Mth.sin(dAng) * 0.58D);
                    boolean sunrise = (d % 2 == 0);
                    renderOctahedron(m, vc, feather, 0.09F, 0.20F, dAng,
                            sunrise ? 1.000F : 0.659F,
                            sunrise ? 0.918F : 0.525F,
                            sunrise ? 0.471F : 0.329F,
                            0.96F * flicker);
                }
            } else if (state.materia == EssenceType.ABYSS) {
                // Section 6.34 Hadopelagic Effigy Decoy (#211A21 / #225C7C): swirling black-ink whirlpool & deep-sea pressure orbs!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.048D, 0.0D), flatX, flatZ,
                        0.58F, 0.96F, 26, 0.129F, 0.102F, 0.129F, 0.95F * flicker);
                renderLocalRing(m, vc, new Vec3(0.0D, 1.10D, 0.0D), flatX, flatZ,
                        0.40F, 0.58F, 24, 0.133F, 0.361F, 0.486F, 0.94F * flicker);
                for (int d = 0; d < 6; d++) {
                    float dAng = -age * 0.16F + (float) (Math.PI * 2.0D * d / 6.0D);
                    Vec3 inkNode = new Vec3(Mth.cos(dAng) * 0.56D, 0.62D + (d % 2) * 0.50D, Mth.sin(dAng) * 0.56D);
                    boolean ink = (d % 2 == 0);
                    renderOctahedron(m, vc, inkNode, 0.10F, 0.18F, dAng,
                            ink ? 0.129F : 0.133F,
                            ink ? 0.102F : 0.361F,
                            ink ? 0.129F : 0.486F,
                            0.97F * flicker);
                }
            } else if (state.materia == EssenceType.AEGIS) {
                // Section 6.35 Gilded Granite Effigy & Bulwark Phalanx Tower-Shield Decoy (#FFEA78 / #773214):
                // interlocking golden granite pavise slabs & holy rune halo!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.048D, 0.0D), flatX, flatZ,
                        0.60F, 1.00F, 8, 0.467F, 0.196F, 0.078F, 0.96F * flicker);
                renderLocalRing(m, vc, new Vec3(0.0D, 1.88D, 0.0D), flatX, flatZ,
                        0.34F, 0.50F, 24, 1.000F, 0.918F, 0.471F, 0.98F * flicker);
                for (int p = 0; p < 6; p++) {
                    float pAng = age * 0.06F + (float) (Math.PI * 2.0D * p / 6.0D);
                    Vec3 centerP = new Vec3(Mth.cos(pAng) * 0.56D, 0.95D, Mth.sin(pAng) * 0.56D);
                    Vec3 tang = new Vec3(-Mth.sin(pAng) * 0.16D, 0.0D, Mth.cos(pAng) * 0.16D);
                    Vec3 up = new Vec3(0.0D, 0.54D, 0.0D);
                    boolean gold = (p % 2 == 0);
                    emitDoubleSidedQuad(m, vc, centerP.subtract(tang).add(up), centerP.add(tang).add(up),
                            centerP.add(tang).subtract(up), centerP.subtract(tang).subtract(up),
                            gold ? 1.000F : 0.467F,
                            gold ? 0.918F : 0.196F,
                            gold ? 0.471F : 0.078F,
                            0.92F * flicker);
                }
            } else if (state.materia == EssenceType.AURORA) {
                // Section 6.36 Cryo-Prism Effigy & Borealis Prism-Flash Hologram (#FFEA78 / #98DCF2):
                // shimmering northern-light curtains & orbiting cryo-prism diamonds!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.048D, 0.0D), flatX, flatZ,
                        0.58F, 0.98F, 6, 0.596F, 0.863F, 0.949F, 0.95F * flicker);
                renderLocalRing(m, vc, new Vec3(0.0D, 1.90D, 0.0D), flatX, flatZ,
                        0.34F, 0.50F, 24, 1.000F, 0.918F, 0.471F, 0.98F * flicker);
                for (int rIdx = 0; rIdx < 4; rIdx++) {
                    float rBase = age * 0.12F + (float) (Math.PI * 0.5D * rIdx);
                    boolean gold = (rIdx % 2 == 0);
                    Vec3 prevR = null;
                    for (int s = 0; s <= 8; s++) {
                        float u = s / 8.0F;
                        double ry = 0.08D + u * 1.84D;
                        double rr = 0.52D + 0.08D * Mth.sin(age * 0.22F + u * 4.5F + rIdx);
                        float ra = rBase + u * 2.2F;
                        Vec3 rpt = new Vec3(Mth.cos(ra) * rr, ry, Mth.sin(ra) * rr);
                        if (prevR != null) {
                            Vec3 perp = new Vec3(-Mth.sin(ra) * 0.055D, 0.025D, Mth.cos(ra) * 0.055D);
                            emitDoubleSidedQuad(m, vc, prevR.subtract(perp), prevR.add(perp), rpt.add(perp), rpt.subtract(perp),
                                    gold ? 1.000F : 0.596F,
                                    gold ? 0.918F : 0.863F,
                                    gold ? 0.471F : 0.949F,
                                    0.86F * flicker);
                        }
                        prevR = rpt;
                    }
                }
            } else if (state.materia == EssenceType.MIRAGE) {
                // Section 6.37 Phantom Mirror Effigy & Fata Morgana Clone (#FFEA78 / #C2B28F):
                // refractive golden mirror panels & shimmering heat-haze distortion rings!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.048D, 0.0D), flatX, flatZ,
                        0.58F, 0.96F, 24, 0.761F, 0.698F, 0.561F, 0.92F * flicker);
                renderLocalRing(m, vc, new Vec3(0.0D, 1.14D, 0.0D), flatX, flatZ,
                        0.40F, 0.58F, 24, 1.000F, 0.918F, 0.471F, 0.96F * flicker);
                for (int p = 0; p < 4; p++) {
                    float pAng = age * 0.10F + (float) (Math.PI * 0.5D * p);
                    Vec3 mCenter = new Vec3(Mth.cos(pAng) * 0.56D, 0.96D + 0.06D * Mth.sin(age * 0.24F + p), Mth.sin(pAng) * 0.56D);
                    Vec3 tang = new Vec3(-Mth.sin(pAng) * 0.14D, 0.0D, Mth.cos(pAng) * 0.14D);
                    Vec3 up = new Vec3(0.0D, 0.48D, 0.0D);
                    boolean gold = (p % 2 == 0);
                    emitDoubleSidedQuad(m, vc, mCenter.subtract(tang).add(up), mCenter.add(tang).add(up),
                            mCenter.add(tang).subtract(up), mCenter.subtract(tang).subtract(up),
                            gold ? 1.000F : 0.761F,
                            gold ? 0.918F : 0.698F,
                            gold ? 0.471F : 0.561F,
                            0.74F * flicker);
                }
            } else if (state.materia == EssenceType.WRAITH) {
                // Section 6.38 Ectoplasmic Soul-Husk Effigy (#41304E / #4D6453):
                // floating translucent spectral soul-husk bound in void-ectoplasm ribbons & ghostly vortex rings!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.048D, 0.0D), flatX, flatZ,
                        0.56F, 0.94F, 24, 0.255F, 0.188F, 0.306F, 0.94F * flicker);
                renderOpenFrustumWall(m, vc, 0.08F, 1.84F, 0.54F, 0.42F, 8, -age * 0.05F,
                        0.302F, 0.392F, 0.325F, 0.44F * flicker);
                for (int r = 0; r < 4; r++) {
                    float rBase = age * 0.11F + (float) (Math.PI * 0.5D * r);
                    Vec3 prevW = null;
                    for (int step = 0; step <= 8; step++) {
                        float u = step / 8.0F;
                        double wy = 0.10D + u * 1.82D;
                        double wr = 0.48D + 0.08D * Mth.sin(age * 0.22F + u * 4.5F + r);
                        float wa = rBase + u * 3.0F;
                        Vec3 wpt = new Vec3(Mth.cos(wa) * wr, wy, Mth.sin(wa) * wr);
                        if (prevW != null) {
                            Vec3 perp = new Vec3(-Mth.sin(wa) * 0.048D, 0.025D, Mth.cos(wa) * 0.048D);
                            boolean moss = (r % 2 == 0);
                            emitDoubleSidedQuad(m, vc, prevW.subtract(perp), prevW.add(perp), wpt.add(perp), wpt.subtract(perp),
                                    moss ? 0.302F : 0.255F,
                                    moss ? 0.392F : 0.188F,
                                    moss ? 0.325F : 0.306F,
                                    0.84F * flicker);
                        }
                        prevW = wpt;
                    }
                }
            } else if (state.materia == EssenceType.BARROW) {
                // Section 6.39 Hollow Bone Effigy (#773214 / #4D6453 + #E6DFCF petrified bone):
                // hollow petrified ribcage ribs, mossy gravestone slabs & ancient grave soil ring!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.048D, 0.0D), flatX, flatZ,
                        0.60F, 1.00F, 16, 0.467F, 0.196F, 0.078F, 0.95F * flicker);
                for (int r = 0; r < 5; r++) {
                    double ry = 0.44D + r * 0.28D;
                    float rr = 0.46F + 0.06F * Mth.sin(r * 0.8F);
                    boolean bone = (r % 2 == 0);
                    renderLocalRing(m, vc, new Vec3(0.0D, ry, 0.0D), flatX, flatZ,
                            rr * 0.80F, rr, 14,
                            bone ? 0.902F : 0.302F,
                            bone ? 0.875F : 0.392F,
                            bone ? 0.812F : 0.325F,
                            0.92F * flicker);
                }
                for (int s = 0; s < 4; s++) {
                    float sAng = age * 0.03F + (float) (Math.PI * 0.5D * s);
                    Vec3 sCenter = new Vec3(Mth.cos(sAng) * 0.54D, 0.92D, Mth.sin(sAng) * 0.54D);
                    Vec3 tang = new Vec3(-Mth.sin(sAng) * 0.16D, 0.0D, Mth.cos(sAng) * 0.16D);
                    Vec3 up = new Vec3(0.0D, 0.56D, 0.0D);
                    boolean granite = (s % 2 == 0);
                    emitDoubleSidedQuad(m, vc, sCenter.subtract(tang).add(up), sCenter.add(tang).add(up),
                            sCenter.add(tang).subtract(up), sCenter.subtract(tang).subtract(up),
                            granite ? 0.467F : 0.302F,
                            granite ? 0.196F : 0.392F,
                            granite ? 0.078F : 0.325F,
                            0.84F * flicker);
                }
            } else if (state.materia == EssenceType.PYRE) {
                // Section 6.40 Smoldering Hearth Effigy (#773214 / #FF4500): stacked burning pyre timber logs,
                // glowing coal hearth bed, & roaring wildfire tongues!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.048D, 0.0D), flatX, flatZ,
                        0.58F, 1.02F, 14, 0.467F, 0.196F, 0.078F, 0.96F * flicker);
                for (int r = 0; r < 5; r++) {
                    double ry = 0.36D + r * 0.28D;
                    float rr = 0.50F - r * 0.035F;
                    boolean flame = (r % 2 == 1);
                    renderLocalRing(m, vc, new Vec3(0.0D, ry, 0.0D), flatX, flatZ,
                            rr * 0.78F, rr, 12,
                            flame ? 1.000F : 0.467F,
                            flame ? 0.271F : 0.196F,
                            flame ? 0.000F : 0.078F,
                            0.94F * flicker);
                }
                for (int t = 0; t < 6; t++) {
                    float tAng = age * 0.07F + (float) (Math.PI * 2.0D * t / 6.0D);
                    double ty = 0.70D + 0.55D * ((age * 0.05F + t / 6.0F) % 1.0F);
                    Vec3 tPos = new Vec3(Mth.cos(tAng) * 0.42D, ty, Mth.sin(tAng) * 0.42D);
                    renderOctahedron(m, vc, tPos, 0.085F, 0.22F, tAng, 1.000F, 0.271F, 0.000F, 0.92F * flicker);
                }
            } else if (state.materia == EssenceType.PENUMBRA) {
                // Section 6.41 Volatile Eclipse Singularity Effigy (#FFEA78 / #281E3C): blinding coronal halo
                // & inward-collapsing deep indigo event horizon singularity!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                float collapse = 1.0F - ((age * 0.055F) % 1.0F);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.05D, 0.0D), flatX, flatZ,
                        0.45F + collapse * 0.85F, 0.65F + collapse * 0.95F, 24,
                        1.000F, 0.918F, 0.471F, 0.94F * flicker * (1.0F - (1.0F - collapse) * 0.45F));

                Vec3 eye = new Vec3(0.0D, 1.12D, 0.0D);
                renderOctahedron(m, vc, eye, 0.28F, 0.34F, -age * 0.28F, 0.157F, 0.118F, 0.235F, 0.98F * flicker);
                renderLocalRing(m, vc, eye, flatX, flatZ, 0.34F, 0.52F, 24, 1.000F, 0.918F, 0.471F, 0.96F * flicker);
                for (int r = 0; r < 4; r++) {
                    double ry = 0.42D + r * 0.36D;
                    boolean corona = (r % 2 == 0);
                    renderLocalRing(m, vc, new Vec3(0.0D, ry, 0.0D), flatX, flatZ, 0.38F, 0.48F, 18,
                            corona ? 1.000F : 0.157F,
                            corona ? 0.918F : 0.118F,
                            corona ? 0.471F : 0.235F,
                            0.88F * flicker);
                }
            } else if (state.materia == EssenceType.RIME) {
                // Section 6.42 Brittle Permafrost Effigy (#6395B1 / #DCF0FF): compacted hoarfrost ice statue
                // & jagged crystalline shatter spikes!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.048D, 0.0D), flatX, flatZ,
                        0.60F, 1.04F, 6, 0.388F, 0.584F, 0.694F, 0.96F * flicker);
                renderOpenFrustumWall(m, vc, 0.10F, 1.84F, 0.52F, 0.34F, 6, age * 0.04F,
                        0.863F, 0.941F, 1.000F, 0.72F * flicker);
                for (int s = 0; s < 6; s++) {
                    float sAng = -age * 0.06F + (float) (Math.PI * 2.0D * s / 6.0D);
                    boolean white = (s % 2 == 0);
                    Vec3 spikePos = new Vec3(Mth.cos(sAng) * 0.54D, 0.58D + (s % 3) * 0.36D, Mth.sin(sAng) * 0.54D);
                    renderOctahedron(m, vc, spikePos, 0.095F, 0.28F, sAng,
                            white ? 0.863F : 0.388F,
                            white ? 0.941F : 0.584F,
                            white ? 1.000F : 0.694F,
                            0.95F * flicker);
                }
            } else if (state.materia == EssenceType.SPRING) {
                // Section 6.43 Blossoming Vernal Nectar Effigy (#FF6B9D / #64C8FF): woven rose vines & dripping springwater!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.048D, 0.0D), flatX, flatZ,
                        0.60F, 1.04F, 24, 0.392F, 0.784F, 1.000F, 0.94F * flicker);
                renderOpenFrustumWall(m, vc, 0.12F, 1.82F, 0.50F, 0.34F, 12, age * 0.12F,
                        1.000F, 0.420F, 0.616F, 0.64F * flicker);
                for (int p = 0; p < 6; p++) {
                    float pAng = age * 0.18F + (float) (Math.PI * 2.0D * p / 6.0D);
                    boolean blossom = (p % 2 == 0);
                    Vec3 petalPos = new Vec3(Mth.cos(pAng) * 0.56D, 0.52D + (p % 3) * 0.38D, Mth.sin(pAng) * 0.56D);
                    renderOctahedron(m, vc, petalPos, 0.11F, 0.20F, pAng,
                            blossom ? 1.000F : 0.392F,
                            blossom ? 0.420F : 0.784F,
                            blossom ? 0.616F : 1.000F,
                            0.94F * flicker);
                }
            } else if (state.materia == EssenceType.STATIC) {
                // Section 6.44 Volatile Dielectric Spark Effigy (#FFE600 / #878DA2): crackling humanoid lightning rod
                // emitting blinding high-voltage sparks & polarized slate conductor bands!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.048D, 0.0D), flatX, flatZ,
                        0.64F, 1.08F, 8, 1.000F, 0.902F, 0.000F, 0.96F * flicker);
                renderOpenFrustumWall(m, vc, 0.10F, 1.88F, 0.48F, 0.28F, 8, age * 0.22F,
                        0.529F, 0.553F, 0.635F, 0.68F * flicker);
                renderOctahedron(m, vc, new Vec3(0.0D, 1.98D, 0.0D), 0.16F, 0.36F, -age * 0.35F,
                        1.000F, 0.902F, 0.000F, 0.99F * flicker);
                for (int s = 0; s < 6; s++) {
                    float sAng = -age * 0.32F + (float) (Math.PI * 2.0D * s / 6.0D);
                    boolean yellow = (s % 2 == 0);
                    Vec3 sparkPos = new Vec3(Mth.cos(sAng) * 0.58D, 0.48D + (s % 3) * 0.42D, Mth.sin(sAng) * 0.58D);
                    renderOctahedron(m, vc, sparkPos, 0.10F, 0.24F, sAng,
                            yellow ? 1.000F : 0.529F,
                            yellow ? 0.902F : 0.553F,
                            yellow ? 0.000F : 0.635F,
                            0.97F * flicker);
                }
            } else if (state.materia == EssenceType.SYLVAN) {
                // Section 6.45 Thorn-Bramble Effigy (#1A5923 / #773214 / #FFEA78): dense petrified briar effigy
                // laced with ironwood thorns & golden canopy light!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.048D, 0.0D), flatX, flatZ,
                        0.62F, 1.06F, 12, 0.467F, 0.196F, 0.078F, 0.96F * flicker);
                renderOpenFrustumWall(m, vc, 0.10F, 1.84F, 0.52F, 0.34F, 8, age * 0.08F,
                        0.102F, 0.349F, 0.137F, 0.72F * flicker);
                for (int t = 0; t < 6; t++) {
                    float tAng = age * 0.14F + (float) (Math.PI * 2.0D * t / 6.0D);
                    boolean gold = (t % 2 == 0);
                    Vec3 thornPos = new Vec3(Mth.cos(tAng) * 0.56D, 0.50D + (t % 3) * 0.40D, Mth.sin(tAng) * 0.56D);
                    renderOctahedron(m, vc, thornPos, 0.11F, 0.26F, tAng,
                            gold ? 1.000F : 0.467F,
                            gold ? 0.918F : 0.196F,
                            gold ? 0.471F : 0.078F,
                            0.96F * flicker);
                }
            } else if (state.materia == EssenceType.MIASMA) {
                // Section 6.46 Decomposing Bog-Husk (#1A5923 / #4D6453 / #225C7C): mud-and-bone swamp effigy
                // bubbling with toxic teal gas sacs & virulent marsh spores!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.048D, 0.0D), flatX, flatZ,
                        0.62F, 1.08F, 14, 0.302F, 0.392F, 0.325F, 0.95F * flicker);
                renderOpenFrustumWall(m, vc, 0.10F, 1.82F, 0.54F, 0.36F, 8, age * 0.09F,
                        0.133F, 0.361F, 0.486F, 0.72F * flicker);
                for (int r = 0; r < 6; r++) {
                    float rAng = age * 0.15F + (float) (Math.PI * 2.0D * r / 6.0D);
                    boolean green = (r % 2 == 0);
                    Vec3 ribPos = new Vec3(Mth.cos(rAng) * 0.54D, 0.48D + (r % 3) * 0.40D, Mth.sin(rAng) * 0.54D);
                    renderOctahedron(m, vc, ribPos, 0.13F, 0.22F, rAng,
                            green ? 0.102F : 0.302F,
                            green ? 0.349F : 0.392F,
                            green ? 0.137F : 0.325F,
                            0.96F * flicker);
                }
            } else if (state.materia == EssenceType.GENESIS) {
                // Section 6.47 Amniotic Water-Phantom (#FF6B9D / #FFEA78 / #225C7C): shimmering liquid decoy
                // ringed in sacred oceanic tides & floating golden-pink vitae lotus blossoms!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.048D, 0.0D), flatX, flatZ,
                        0.64F, 1.12F, 18, 0.133F, 0.361F, 0.486F, 0.95F * flicker);
                renderOpenFrustumWall(m, vc, 0.10F, 1.86F, 0.54F, 0.36F, 10, -age * 0.11F,
                        0.133F, 0.361F, 0.486F, 0.68F * flicker);
                for (int p = 0; p < 6; p++) {
                    float pAng = age * 0.16F + (float) (Math.PI * 2.0D * p / 6.0D);
                    boolean gold = (p % 2 == 0);
                    Vec3 petalPos = new Vec3(Mth.cos(pAng) * 0.56D, 0.52D + (p % 3) * 0.42D, Mth.sin(pAng) * 0.56D);
                    renderOctahedron(m, vc, petalPos, 0.12F, 0.24F, pAng,
                            1.000F,
                            gold ? 0.918F : 0.420F,
                            gold ? 0.471F : 0.616F,
                            0.97F * flicker);
                }
            } else if (state.materia == EssenceType.OBLIVION) {
                // Section 6.48 Smoldering Wither-Effigy (#B83C08 / #211A21 / #4D6453): smoldering darkfire bone-totem
                // encased in stygian obsidian ash, blazing magma hearth rings & charred necrotic bone ribs!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.048D, 0.0D), flatX, flatZ,
                        0.66F, 1.14F, 16, 0.722F, 0.235F, 0.031F, 0.96F * flicker);
                renderOpenFrustumWall(m, vc, 0.10F, 1.88F, 0.56F, 0.38F, 8, age * 0.10F,
                        0.129F, 0.102F, 0.129F, 0.84F * flicker);
                for (int r = 0; r < 6; r++) {
                    float rAng = -age * 0.17F + (float) (Math.PI * 2.0D * r / 6.0D);
                    boolean bone = (r % 2 == 0);
                    Vec3 ribPos = new Vec3(Mth.cos(rAng) * 0.58D, 0.50D + (r % 3) * 0.42D, Mth.sin(rAng) * 0.58D);
                    renderOctahedron(m, vc, ribPos, 0.14F, 0.24F, rAng,
                            bone ? 0.302F : 0.722F,
                            bone ? 0.392F : 0.235F,
                            bone ? 0.325F : 0.031F,
                            0.98F * flicker);
                }
            } else if (state.materia == EssenceType.AETHER) {
                // Section 6.50 Harmonized Quintessence Effigy (#A88654 / #773214 / #225C7C / #B83C08):
                // Shimmering multi-elemental holographic effigy ringed in luminous atmospheric halos & 4 primordial prisms!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.048D, 0.0D), flatX, flatZ,
                        0.68F, 1.18F, 22, 0.659F, 0.525F, 0.329F, 0.96F * flicker);
                renderOpenFrustumWall(m, vc, 0.10F, 1.92F, 0.58F, 0.40F, 12, age * 0.12F,
                        0.133F, 0.361F, 0.486F, 0.66F * flicker);
                float[][] quadRgb = {
                        {0.659F, 0.525F, 0.329F},
                        {0.467F, 0.196F, 0.078F},
                        {0.133F, 0.361F, 0.486F},
                        {0.722F, 0.235F, 0.031F}
                };
                for (int q = 0; q < 8; q++) {
                    float qAng = age * 0.18F + (float) (Math.PI * 2.0D * q / 8.0D);
                    float[] c = quadRgb[q & 3];
                    Vec3 nodePos = new Vec3(Mth.cos(qAng) * 0.60D, 0.44D + (q % 4) * 0.34D, Mth.sin(qAng) * 0.60D);
                    renderOctahedron(m, vc, nodePos, 0.13F, 0.23F, qAng, c[0], c[1], c[2], 0.98F * flicker);
                }
            } else if (state.materia == EssenceType.ENTROPIC) {
                // Section 6.51 Smoldering Void-Husk Effigy (#41304E / #211A21 / #4D6453 / #8A0303):
                // Menacing bone-and-darkfire effigy ringed in collapsed void-indigo & boiling crimson blood droplets!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.048D, 0.0D), flatX, flatZ,
                        0.68F, 1.18F, 20, 0.541F, 0.012F, 0.012F, 0.96F * flicker);
                renderOpenFrustumWall(m, vc, 0.10F, 1.92F, 0.58F, 0.40F, 10, -age * 0.11F,
                        0.129F, 0.102F, 0.129F, 0.86F * flicker);
                for (int e = 0; e < 8; e++) {
                    float eAng = -age * 0.18F + (float) (Math.PI * 2.0D * e / 8.0D);
                    int idx = e & 3;
                    float cr = idx == 0 ? 0.255F : (idx == 1 ? 0.129F : (idx == 2 ? 0.302F : 0.541F));
                    float cg = idx == 0 ? 0.188F : (idx == 1 ? 0.102F : (idx == 2 ? 0.392F : 0.012F));
                    float cb = idx == 0 ? 0.306F : (idx == 1 ? 0.129F : (idx == 2 ? 0.325F : 0.012F));
                    Vec3 nodePos = new Vec3(Mth.cos(eAng) * 0.60D, 0.44D + (e % 4) * 0.34D, Mth.sin(eAng) * 0.60D);
                    renderOctahedron(m, vc, nodePos, 0.14F, 0.24F, eAng, cr, cg, cb, 0.98F * flicker);
                }
            } else if (state.materia == EssenceType.CELESTIAL) {
                // Section 6.52 Holy Genesis Effigy (#FFEA78 / #FF6B9D / #1A5923 / #A88654): shimmering solar-bloom & rose-prana effigy!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.048D, 0.0D), flatX, flatZ,
                        0.68F, 1.20F, 22, 1.000F, 0.918F, 0.471F, 0.96F * flicker);
                renderOpenFrustumWall(m, vc, 0.10F, 1.94F, 0.58F, 0.42F, 10, age * 0.11F,
                        1.000F, 0.420F, 0.616F, 0.56F * flicker);
                for (int c = 0; c < 8; c++) {
                    float cAng = age * 0.18F + (float) (Math.PI * 2.0D * c / 8.0D);
                    int idx = c & 3;
                    float cr = idx == 0 ? 1.000F : (idx == 1 ? 1.000F : (idx == 2 ? 0.102F : 0.659F));
                    float cg = idx == 0 ? 0.918F : (idx == 1 ? 0.420F : (idx == 2 ? 0.349F : 0.525F));
                    float cb = idx == 0 ? 0.471F : (idx == 1 ? 0.616F : (idx == 2 ? 0.137F : 0.329F));
                    Vec3 nodePos = new Vec3(Mth.cos(cAng) * 0.60D, 0.44D + (c % 4) * 0.34D, Mth.sin(cAng) * 0.60D);
                    renderOctahedron(m, vc, nodePos, 0.14F, 0.24F, cAng, cr, cg, cb, 0.98F * flicker);
                }
            } else if (state.materia == EssenceType.PRISMATIC) {
                // Section 6.53 Sacrificial Solar Relic & Prismatic Mirror Decoy:
                // Gleaming diamond prism-cage, rainbow taunt rings, blinding white-gold solar relic core & 8 spectrum nodes!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                int rRgb = Mth.hsvToRgb((age * 0.030F) % 1.0F, 0.76F, 1.0F);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.050D, 0.0D), flatX, flatZ,
                        0.70F, 1.24F, 24,
                        ((rRgb >> 16) & 0xFF) / 255.0F,
                        ((rRgb >> 8) & 0xFF) / 255.0F,
                        (rRgb & 0xFF) / 255.0F,
                        0.97F * flicker);
                renderOpenFrustumWall(m, vc, 0.12F, 1.96F, 0.60F, 0.44F, 12, age * 0.14F,
                        0.447F, 0.965F, 1.000F, 0.52F * flicker);
                renderOctahedron(m, vc, new Vec3(0.0D, 1.12D, 0.0D), 0.30F, 0.66F, -age * 0.22F,
                        1.000F, 1.000F, 1.000F, 0.92F * flicker);
                for (int p = 0; p < 8; p++) {
                    float pAng = age * 0.22F + (float) (Math.PI * 2.0D * p / 8.0D);
                    int pRgb = Mth.hsvToRgb(((p / 8.0F) + age * 0.035F) % 1.0F, 0.80F, 1.0F);
                    float cr = ((pRgb >> 16) & 0xFF) / 255.0F;
                    float cg = ((pRgb >> 8) & 0xFF) / 255.0F;
                    float cb = (pRgb & 0xFF) / 255.0F;
                    Vec3 nodePos = new Vec3(Mth.cos(pAng) * 0.64D, 0.40D + (p % 4) * 0.36D, Mth.sin(pAng) * 0.64D);
                    renderOctahedron(m, vc, nodePos, 0.14F, 0.25F, pAng, cr, cg, cb, 0.98F * flicker);
                }
            } else if (state.materia == EssenceType.EMPYREAN) {
                // Section 6.54 Golden Willow Effigy (#FFEA78 / #FF6B9D / #1A5923):
                // Shimmering living timber, golden willow boughs, & rose-lotus dewdrop nodes!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.050D, 0.0D), flatX, flatZ,
                        0.74F, 1.32F, 24, 1.000F, 0.918F, 0.471F, 0.97F * flicker);
                renderOpenFrustumWall(m, vc, 0.10F, 2.02F, 0.62F, 0.46F, 10, age * 0.10F,
                        0.102F, 0.349F, 0.137F, 0.58F * flicker);
                for (int w = 0; w < 9; w++) {
                    float wAng = age * 0.16F + (float) (Math.PI * 2.0D * w / 9.0D);
                    int idx = w % 3;
                    float cr = idx == 0 ? 1.000F : (idx == 1 ? 1.000F : 0.102F);
                    float cg = idx == 0 ? 0.918F : (idx == 1 ? 0.420F : 0.349F);
                    float cb = idx == 0 ? 0.471F : (idx == 1 ? 0.616F : 0.137F);
                    Vec3 nodePos = new Vec3(Mth.cos(wAng) * 0.66D, 0.38D + (w % 3) * 0.46D, Mth.sin(wAng) * 0.66D);
                    renderOctahedron(m, vc, nodePos, 0.15F, 0.26F, wAng, cr, cg, cb, 0.98F * flicker);
                }
            } else if (state.materia == EssenceType.CATACLYSM) {
                // Section 6.55 Molten Core Beacon (#41304E / #211A21 / #B83C08):
                // Smoldering obsidian-and-magma core beacon, collapsing dark-matter singularity heart & 9 magma flare nodes!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.050D, 0.0D), flatX, flatZ,
                        0.76F, 1.36F, 24, 0.722F, 0.235F, 0.031F, 0.97F * flicker);
                renderOpenFrustumWall(m, vc, 0.10F, 2.02F, 0.64F, 0.44F, 8, -age * 0.14F,
                        0.129F, 0.102F, 0.129F, 0.78F * flicker);
                renderOctahedron(m, vc, new Vec3(0.0D, 1.12D, 0.0D), 0.32F, 0.62F, -age * 0.26F,
                        0.722F, 0.235F, 0.031F, 0.96F * flicker);
                for (int w = 0; w < 9; w++) {
                    float wAng = -age * 0.20F + (float) (Math.PI * 2.0D * w / 9.0D);
                    int idx = w % 3;
                    float cr = idx == 0 ? 0.722F : (idx == 1 ? 0.255F : 1.000F);
                    float cg = idx == 0 ? 0.235F : (idx == 1 ? 0.188F : 0.369F);
                    float cb = idx == 0 ? 0.031F : (idx == 1 ? 0.306F : 0.102F);
                    Vec3 nodePos = new Vec3(Mth.cos(wAng) * 0.68D, 0.36D + (w % 3) * 0.48D, Mth.sin(wAng) * 0.68D);
                    renderOctahedron(m, vc, nodePos, 0.15F, 0.27F, wAng, cr, cg, cb, 0.99F * flicker);
                }
            } else if (state.materia == EssenceType.TERMINUS) {
                // Section 6.56 Unstable Cryogenic Elemental Effigy (#A88654 / #98DCF2 / #225C7C):
                // Shimmering ice-sculpted prism effigy, deep-azure glacial core, golden-buff gale rings & 9 frost-mirror shards!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.050D, 0.0D), flatX, flatZ,
                        0.76F, 1.36F, 24, 0.596F, 0.863F, 0.949F, 0.96F * flicker);
                renderOpenFrustumWall(m, vc, 0.10F, 2.02F, 0.62F, 0.44F, 6, age * 0.10F,
                        0.133F, 0.361F, 0.486F, 0.64F * flicker);
                renderOctahedron(m, vc, new Vec3(0.0D, 1.12D, 0.0D), 0.30F, 0.64F, age * 0.22F,
                        0.596F, 0.863F, 0.949F, 0.95F * flicker);
                for (int w = 0; w < 9; w++) {
                    float wAng = age * 0.18F + (float) (Math.PI * 2.0D * w / 9.0D);
                    int idx = w % 3;
                    float cr = idx == 0 ? 0.596F : (idx == 1 ? 0.659F : 0.133F);
                    float cg = idx == 0 ? 0.863F : (idx == 1 ? 0.525F : 0.361F);
                    float cb = idx == 0 ? 0.949F : (idx == 1 ? 0.329F : 0.486F);
                    Vec3 nodePos = new Vec3(Mth.cos(wAng) * 0.68D, 0.36D + (w % 3) * 0.48D, Mth.sin(wAng) * 0.68D);
                    renderOctahedron(m, vc, nodePos, 0.14F, 0.28F, wAng, cr, cg, cb, 0.98F * flicker);
                }
            } else if (state.materia == EssenceType.ESCHATON) {
                // Section 6.57 Collapsing Micro-Nova Effigy (#0B0813 / #7012A5 / #D4C2FC):
                // Shimmering gravitational effigy, collapsing void-black core, searing violet accretion funnel & 9 chrono-silver distortion shards!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.050D, 0.0D), flatX, flatZ,
                        0.82F, 1.48F, 26, 0.439F, 0.071F, 0.647F, 0.97F * flicker);
                renderOpenFrustumWall(m, vc, 0.10F, 2.08F, 0.66F, 0.46F, 8, -age * 0.14F,
                        0.043F, 0.031F, 0.075F, 0.72F * flicker);
                renderOctahedron(m, vc, new Vec3(0.0D, 1.14D, 0.0D), 0.32F, 0.66F, -age * 0.26F,
                        0.043F, 0.031F, 0.075F, 0.99F * flicker);
                renderLocalRing(m, vc, new Vec3(0.0D, 1.14D, 0.0D), flatX, flatZ,
                        0.44F, 0.82F, 22, 0.831F, 0.761F, 0.988F, 0.96F * flicker);
                for (int w = 0; w < 9; w++) {
                    float wAng = -age * 0.22F + (float) (Math.PI * 2.0D * w / 9.0D);
                    int idx = w % 3;
                    float cr = idx == 0 ? 0.439F : (idx == 1 ? 0.831F : 0.043F);
                    float cg = idx == 0 ? 0.071F : (idx == 1 ? 0.761F : 0.031F);
                    float cb = idx == 0 ? 0.647F : (idx == 1 ? 0.988F : 0.075F);
                    Vec3 nodePos = new Vec3(Mth.cos(wAng) * 0.74D, 0.36D + (w % 3) * 0.48D, Mth.sin(wAng) * 0.74D);
                    renderOctahedron(m, vc, nodePos, 0.15F, 0.30F, wAng, cr, cg, cb, 0.99F * flicker);
                }
            } else if (state.materia == EssenceType.APOTHEOSIS) {
                // Section 6.58 Radiant Ascendance Mirage (#FFFFFF / #FFE484 / #72F6FF):
                // Shimmering solar-winged avatar effigy, blazing white-hole supernova core, astral-gold halo & 6 hard-light wing blades!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.050D, 0.0D), flatX, flatZ,
                        0.84F, 1.50F, 28, 1.000F, 0.894F, 0.518F, 0.97F * flicker);
                renderOpenFrustumWall(m, vc, 0.10F, 2.10F, 0.66F, 0.48F, 8, age * 0.14F,
                        1.000F, 1.000F, 1.000F, 0.62F * flicker);
                renderOctahedron(m, vc, new Vec3(0.0D, 1.16D, 0.0D), 0.34F, 0.68F, age * 0.26F,
                        1.000F, 1.000F, 1.000F, 0.99F * flicker);
                renderLocalRing(m, vc, new Vec3(0.0D, 1.16D, 0.0D), flatX, flatZ,
                        0.46F, 0.84F, 24, 0.447F, 0.965F, 1.000F, 0.96F * flicker);
                for (int w = 0; w < 6; w++) {
                    int side = (w % 2 == 0) ? -1 : 1;
                    int tierIdx = w / 2;
                    double wy = 1.02D + tierIdx * 0.32D + 0.06D * Mth.sin(age * 0.28F + w);
                    Vec3 root = new Vec3(side * 0.24D, wy, -0.18D);
                    Vec3 tip = new Vec3(side * (0.98D + tierIdx * 0.18D), wy + 0.28D, -0.44D);
                    Vec3 low = new Vec3(side * (0.78D + tierIdx * 0.14D), wy - 0.12D, -0.36D);
                    boolean gold = (tierIdx == 1);
                    emitDoubleSidedQuad(m, vc, root, tip, low, root,
                            gold ? 1.000F : (tierIdx == 0 ? 1.000F : 0.447F),
                            gold ? 0.894F : (tierIdx == 0 ? 1.000F : 0.965F),
                            gold ? 0.518F : 1.000F,
                            0.94F * flicker);
                }
            } else if (state.materia == EssenceType.ENTROPICA) {
                // Section 6.59 Fractured Paradox Core (#E0E5EC / #1A153B / #FAD02C):
                // End-portal parallax effigy with counter-spinning event-horizon indigo core, chrono-silver & genesis-gold halos & 9 fractured paradox shards!
                Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
                Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
                renderLocalRing(m, vc, new Vec3(0.0D, 0.050D, 0.0D), flatX, flatZ,
                        0.86F, 1.54F, 28, 0.980F, 0.816F, 0.173F, 0.97F * flicker);
                renderOpenFrustumWall(m, vc, 0.10F, 2.12F, 0.68F, 0.48F, 8, -age * 0.15F,
                        0.102F, 0.082F, 0.231F, 0.76F * flicker);
                renderOctahedron(m, vc, new Vec3(0.0D, 1.16D, 0.0D), 0.35F, 0.68F, age * 0.28F,
                        0.102F, 0.082F, 0.231F, 0.99F * flicker);
                renderLocalRing(m, vc, new Vec3(0.0D, 1.16D, 0.0D), flatX, flatZ,
                        0.48F, 0.88F, 26, 0.878F, 0.898F, 0.925F, 0.97F * flicker);
                for (int w = 0; w < 9; w++) {
                    float wAng = age * 0.22F * ((w & 1) == 0 ? 1.0F : -1.0F) + (float) (Math.PI * 2.0D * w / 9.0D);
                    int idx = w % 3;
                    float cr = idx == 0 ? 0.878F : (idx == 1 ? 0.980F : 0.102F);
                    float cg = idx == 0 ? 0.898F : (idx == 1 ? 0.816F : 0.082F);
                    float cb = idx == 0 ? 0.925F : (idx == 1 ? 0.173F : 0.231F);
                    Vec3 nodePos = new Vec3(Mth.cos(wAng) * 0.78D, 0.36D + (w % 3) * 0.50D, Mth.sin(wAng) * 0.78D);
                    renderOctahedron(m, vc, nodePos, 0.16F, 0.32F, wAng, cr, cg, cb, 0.99F * flicker);
                }
            }

            // Pulsing mirage core diamond inside the cyclone eye
            renderOctahedron(m, vc, new Vec3(0.0D, 1.02D, 0.0D), 0.20F, 0.52F, age * 0.18F, ar, ag, ab, 0.85F * flicker);
        });

        // 2. Semi-transparent spectral effigy silhouette inside the funnel (+ mirrored summoner equipment for mirage decoys)
        poseStack.mulPose(Axis.YP.rotationDegrees(180f - state.yRot));
        poseStack.scale(-0.92F, -0.92F, 0.92F);
        poseStack.translate(0.0F, -1.52F, 0.0F);
        this.model.setupAnim(state);
        final int decoyAlpha = (state.materia == EssenceType.UMBRAL || state.materia == EssenceType.ECLIPSE
                || state.materia == EssenceType.NULL_U || state.materia == EssenceType.ABYSS
                || state.materia == EssenceType.OBLIVION || state.materia == EssenceType.ENTROPIC
                || state.materia == EssenceType.CATACLYSM || state.materia == EssenceType.ESCHATON) ? 0xC8 : 0x72;
        final int decoyTint = (decoyAlpha << 24) | (state.primaryRgb & 0xFFFFFF);
        collector.submitCustomGeometry(poseStack, RenderType.entityTranslucentEmissive(EIDOLON_TEXTURE), (pose, vc) -> {
            PoseStack localStack = new PoseStack();
            localStack.last().pose().set(pose.pose());
            localStack.last().normal().set(pose.normal());
            this.model.renderToBuffer(localStack, vc, FULL_LIGHT, NO_OVERLAY, decoyTint);
        });

        if (state.hasHeadEquip) {
            poseStack.pushPose();
            this.model.body.translateAndRotate(poseStack);
            this.model.head.translateAndRotate(poseStack);
            poseStack.translate(0.0f, -0.15f, -0.12f);
            poseStack.scale(0.65f, -0.65f, -0.65f);
            state.headEquipRenderState.submit(poseStack, collector, FULL_LIGHT, NO_OVERLAY, 0);
            poseStack.popPose();
        }
        if (state.hasChestEquip) {
            poseStack.pushPose();
            this.model.body.translateAndRotate(poseStack);
            poseStack.translate(0.0f, -0.15f, -0.40f);
            poseStack.scale(0.65f, -0.65f, -0.65f);
            state.chestEquipRenderState.submit(poseStack, collector, FULL_LIGHT, NO_OVERLAY, 0);
            poseStack.popPose();
        }
        if (state.hasLegsEquip) {
            poseStack.pushPose();
            this.model.body.translateAndRotate(poseStack);
            poseStack.translate(0.0f, 0.42f, -0.26f);
            poseStack.scale(0.62f, -0.62f, -0.62f);
            state.legsEquipRenderState.submit(poseStack, collector, FULL_LIGHT, NO_OVERLAY, 0);
            poseStack.popPose();
        }
        if (state.hasFeetEquip) {
            poseStack.pushPose();
            this.model.rightLeg.translateAndRotate(poseStack);
            poseStack.translate(-0.08f, 0.62f, -0.18f);
            poseStack.scale(0.55f, -0.55f, -0.55f);
            state.feetEquipRenderState.submit(poseStack, collector, FULL_LIGHT, NO_OVERLAY, 0);
            poseStack.popPose();
        }
        if (state.hasItem) {
            poseStack.pushPose();
            this.model.body.translateAndRotate(poseStack);
            this.model.rightArm.translateAndRotate(poseStack);
            poseStack.translate(-0.1f, 0.8f, -0.2f);
            poseStack.mulPose(Axis.XP.rotationDegrees(90f));
            poseStack.scale(0.8f, 0.8f, 0.8f);
            state.itemRenderState.submit(poseStack, collector, FULL_LIGHT, NO_OVERLAY, 0);
            poseStack.popPose();
        }
        if (state.hasOffhandItem) {
            poseStack.pushPose();
            this.model.body.translateAndRotate(poseStack);
            this.model.leftArm.translateAndRotate(poseStack);
            poseStack.translate(0.1f, 0.8f, -0.2f);
            poseStack.mulPose(Axis.XP.rotationDegrees(90f));
            poseStack.scale(0.8f, 0.8f, 0.8f);
            state.offhandRenderState.submit(poseStack, collector, FULL_LIGHT, NO_OVERLAY, 0);
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    /**
     * Renders majestic swept golden-ivory Zephyr Wing-Pinions and a swirling ankle cyclone for Air Autonomous Agents.
     */
    private static void renderZephyrAgentOrnaments(
            Matrix4f m, VertexConsumer vc, float age, float bodyYawDeg,
            float pr, float pg, float pb, float ar, float ag, float ab
    ) {
        // Swirling miniature ankle/waist slipstream funnel
        renderOpenFrustumWall(m, vc, 0.04F, 0.68F, 0.22F, 0.46F, 16, age * 0.25F, pr, pg, pb, 0.30F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 backDir = new Vec3(Mth.sin(yawRad), 0.0D, -Mth.cos(yawRad));
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));
        Vec3 shoulderHub = new Vec3(0.0D, 1.24D, 0.0D).add(backDir.scale(0.16D));

        float flap = 0.22F * Mth.sin(age * 0.22F);
        for (int side = -1; side <= 1; side += 2) {
            Vec3 sideVec = rightDir.scale(side * Mth.cos(flap)).add(backDir.scale(0.42D + 0.18D * Mth.sin(flap))).normalize();
            for (int feather = 0; feather < 3; feather++) {
                double span = 0.88D - feather * 0.16D;
                double lift = 0.42D - feather * 0.24D;
                Vec3 root = shoulderHub.add(0.0D, -feather * 0.09D, 0.0D);
                Vec3 midLead = root.add(sideVec.scale(span * 0.55D)).add(0.0D, lift * 0.72D, 0.0D);
                Vec3 tip = root.add(sideVec.scale(span)).add(backDir.scale(0.22D)).add(0.0D, lift, 0.0D);
                Vec3 trail = root.add(sideVec.scale(span * 0.48D)).add(backDir.scale(0.26D)).add(0.0D, lift - 0.16D, 0.0D);

                boolean ivory = (feather == 0);
                emitDoubleSidedQuad(m, vc, root, midLead, tip, trail,
                        ivory ? 1.0F : ar, ivory ? 1.0F : ag, ivory ? 1.0F : ab, ivory ? 0.90F : 0.72F);
            }
        }
    }

    /**
     * Section 6.3 Aqueous Eidolon Agent (#225C7C): swirling azure tide pool & 4 undulating 3D lashing water tendrils!
     */
    private static void renderAqueousAgentOrnaments(
            Matrix4f m, VertexConsumer vc, float age, float bodyYawDeg,
            float pr, float pg, float pb, float ar, float ag, float ab
    ) {
        // Swirling deep-azure (#225C7C) ankle tide vortex & whitewater surf rim
        renderOpenFrustumWall(m, vc, 0.03F, 0.56F, 0.46F, 0.26F, 16, -age * 0.20F, 0.133F, 0.361F, 0.486F, 0.42F);
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.04D, 0.0D), flatX, flatZ, 0.44F, 0.56F, 20, 0.88F, 0.98F, 1.00F, 0.78F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 fwdDir = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));
        Vec3 upDir = new Vec3(0.0D, 1.0D, 0.0D);

        // 4 dynamic undulating 3D water tendrils arching & lashing from the shoulders and waist
        for (int t = 0; t < 4; t++) {
            int side = (t % 2 == 0) ? -1 : 1;
            boolean upper = (t < 2);
            Vec3 root = new Vec3(0.0D, upper ? 1.26D : 0.92D, 0.0D)
                    .add(rightDir.scale(side * 0.26D))
                    .subtract(fwdDir.scale(upper ? 0.10D : 0.16D));

            int segs = 10;
            Vec3 prev = root;
            float prevHalfW = 0.068F;
            for (int s = 1; s <= segs; s++) {
                float frac = s / (float) segs;
                float wave1 = Mth.sin(age * 0.24F - frac * 4.2F + t * 1.35F);
                float wave2 = Mth.cos(age * 0.19F - frac * 3.6F + t * 2.10F);

                double reach = frac * (upper ? 1.15D : 0.95D);
                double sideSpan = side * (0.18D + frac * 0.58D + 0.16D * wave1);
                double fwdSpan = (upper ? 0.36D : -0.18D) * frac + 0.24D * frac * wave2;
                double lift = (upper ? 0.34D : 0.08D) * Mth.sin(frac * (float) Math.PI) + 0.12D * wave1;

                Vec3 cur = root
                        .add(rightDir.scale(sideSpan))
                        .add(fwdDir.scale(fwdSpan + reach * 0.32D))
                        .add(0.0D, lift - 0.18D * frac * frac, 0.0D);

                float halfW = 0.068F * (1.0F - 0.72F * frac);
                Vec3 pOff = upDir.scale(prevHalfW).add(rightDir.scale(prevHalfW * 0.35D));
                Vec3 cOff = upDir.scale(halfW).add(rightDir.scale(halfW * 0.35D));

                boolean seafoamTip = (s >= segs - 2);
                float cr = seafoamTip ? 0.88F : Mth.lerp(frac, 0.133F, 0.18F);
                float cg = seafoamTip ? 0.98F : Mth.lerp(frac, 0.361F, 0.74F);
                float cb = 1.00F;
                emitDoubleSidedQuad(m, vc, prev.subtract(pOff), prev.add(pOff), cur.add(cOff), cur.subtract(cOff),
                        cr, cg, cb, 0.88F * (1.0F - 0.18F * frac));

                if (s == segs) {
                    // Pressurized water droplet bead at the tip of each lashing tendril!
                    renderOctahedron(m, vc, cur, 0.048F, 0.058F, age * 0.3F + t, 0.90F, 0.99F, 1.00F, 0.94F);
                }

                prev = cur;
                prevHalfW = halfW;
            }
        }
    }

    /**
     * Section 6.4 Sylvan Agent (#1A5923): 3D wooden stag-antler branch crown, woven briar ankle wreath,
     * and 4 whipping thorny briar tendrils with sprouting emerald leaves!
     */
    private static void renderSylvanAgentOrnaments(
            Matrix4f m, VertexConsumer vc, float age, float bodyYawDeg,
            float pr, float pg, float pb, float ar, float ag, float ab
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.05D, 0.0D), flatX, flatZ, 0.42F, 0.55F, 20, 0.102F, 0.349F, 0.137F, 0.86F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 fwdDir = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));
        Vec3 upDir = new Vec3(0.0D, 1.0D, 0.0D);

        // 1. 3D Wooden Stag-Branch Antler Crown above temples (#5C3A1E bark + #B4FF78 sprout tips)
        for (int side = -1; side <= 1; side += 2) {
            Vec3 brow = new Vec3(0.0D, 1.84D, 0.0D).add(rightDir.scale(side * 0.18D)).add(fwdDir.scale(0.04D));
            Vec3 mid = brow.add(rightDir.scale(side * 0.22D)).add(0.0D, 0.26D, 0.0D).subtract(fwdDir.scale(0.06D));
            Vec3 tip = mid.add(rightDir.scale(side * 0.14D)).add(0.0D, 0.24D, 0.0D).add(fwdDir.scale(0.08D));
            Vec3 tine = mid.add(fwdDir.scale(0.18D)).add(0.0D, 0.16D, 0.0D);
            Vec3 w = rightDir.scale(0.032D);
            emitDoubleSidedQuad(m, vc, brow.subtract(w), brow.add(w), mid.add(w), mid.subtract(w), 0.36F, 0.23F, 0.12F, 0.95F);
            emitDoubleSidedQuad(m, vc, mid.subtract(w), mid.add(w), tip.add(w.scale(0.5D)), tip.subtract(w.scale(0.5D)), 0.36F, 0.23F, 0.12F, 0.95F);
            emitDoubleSidedQuad(m, vc, mid.subtract(w.scale(0.7D)), mid.add(w.scale(0.7D)), tine, tine, 0.17F, 0.90F, 0.41F, 0.92F);
            renderOctahedron(m, vc, tip, 0.042F, 0.068F, age * 0.15F, 0.71F, 1.00F, 0.47F, 0.96F);
        }

        // 2. 4 whipping 3D thorny briar tendrils arching from the shoulders and waist
        for (int t = 0; t < 4; t++) {
            int side = (t % 2 == 0) ? -1 : 1;
            boolean upper = (t < 2);
            Vec3 root = new Vec3(0.0D, upper ? 1.28D : 0.90D, 0.0D)
                    .add(rightDir.scale(side * 0.26D))
                    .subtract(fwdDir.scale(upper ? 0.10D : 0.16D));

            int segs = 10;
            Vec3 prev = root;
            float prevHalfW = 0.056F;
            for (int s = 1; s <= segs; s++) {
                float frac = s / (float) segs;
                float whip1 = Mth.sin(age * 0.26F - frac * 4.0F + t * 1.45F);
                float whip2 = Mth.cos(age * 0.20F - frac * 3.4F + t * 2.05F);

                double reach = frac * (upper ? 1.22D : 1.00D);
                double sideSpan = side * (0.20D + frac * 0.62D + 0.15D * whip1);
                double fwdSpan = (upper ? 0.40D : -0.14D) * frac + 0.22D * frac * whip2;
                double lift = (upper ? 0.36D : 0.10D) * Mth.sin(frac * (float) Math.PI) + 0.10D * whip1;

                Vec3 cur = root
                        .add(rightDir.scale(sideSpan))
                        .add(fwdDir.scale(fwdSpan + reach * 0.34D))
                        .add(0.0D, lift - 0.16D * frac * frac, 0.0D);

                float halfW = 0.056F * (1.0F - 0.68F * frac);
                Vec3 pOff = upDir.scale(prevHalfW).add(rightDir.scale(prevHalfW * 0.35D));
                Vec3 cOff = upDir.scale(halfW).add(rightDir.scale(halfW * 0.35D));

                boolean thornTip = (s >= segs - 2);
                float cr = thornTip ? 0.71F : Mth.lerp(frac, 0.36F, 0.102F);
                float cg = thornTip ? 1.00F : Mth.lerp(frac, 0.23F, 0.48F);
                float cb = thornTip ? 0.47F : Mth.lerp(frac, 0.12F, 0.16F);
                emitDoubleSidedQuad(m, vc, prev.subtract(pOff), prev.add(pOff), cur.add(cOff), cur.subtract(cOff),
                        cr, cg, cb, 0.92F * (1.0F - 0.12F * frac));

                // Sprouting leaf barbs along the briar tendril
                if (s % 3 == 0 && s < segs) {
                    Vec3 barbTip = cur.add(rightDir.scale(side * 0.14D)).add(0.0D, 0.08D, 0.0D);
                    emitDoubleSidedQuad(m, vc, prev, cur, barbTip, barbTip, 0.17F, 0.90F, 0.41F, 0.90F);
                }
                if (s == segs) {
                    renderOctahedron(m, vc, cur, 0.042F, 0.11F, age * 0.25F + t, 0.71F, 1.00F, 0.47F, 0.96F);
                }

                prev = cur;
                prevHalfW = halfW;
            }
        }
    }

    /**
     * Section 6.5 Tectonic Eidolon Agent (#773214): chiseled granite & burnt-terracotta boulder pauldrons,
     * heavy stone ground-punching gauntlets, octagonal seismic foot-ring & orbiting fractured stone shards!
     */
    private static void renderTectonicAgentOrnaments(
            Matrix4f m, VertexConsumer vc, float age, float bodyYawDeg,
            float pr, float pg, float pb, float ar, float ag, float ab
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.055D, 0.0D), flatX, flatZ, 0.44F, 0.58F, 8, 1.00F, 0.76F, 0.47F, 0.88F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 fwdDir = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));

        // 1. Twin massive chiseled granite & terracotta boulder pauldrons + stalagmite shoulder spikes!
        for (int side = -1; side <= 1; side += 2) {
            double bob = 0.025D * Mth.sin(age * 0.12F + (side > 0 ? 0.0F : 3.14F));
            Vec3 shoulder = new Vec3(0.0D, 1.42D + bob, 0.0D).add(rightDir.scale(side * 0.36D));
            renderOctahedron(m, vc, shoulder, 0.19F, 0.18F, yawRad + side * 0.35F, 0.353F, 0.322F, 0.298F, 0.96F);
            renderOctahedron(m, vc, shoulder.add(0.0D, 0.10D, 0.0D).add(rightDir.scale(side * 0.08D)),
                    0.11F, 0.22F, -yawRad, 0.467F, 0.196F, 0.078F, 0.95F);
            renderOctahedron(m, vc, shoulder.add(0.0D, 0.16D, 0.0D).add(rightDir.scale(side * 0.10D)),
                    0.055F, 0.18F, yawRad, 1.00F, 0.76F, 0.47F, 0.96F);

            // Floating heavy stone ground-punching fist gauntlet cuffs around forearms
            Vec3 fist = new Vec3(0.0D, 0.84D - bob, 0.0D)
                    .add(rightDir.scale(side * 0.38D))
                    .add(fwdDir.scale(0.22D));
            renderOctahedron(m, vc, fist, 0.14F, 0.16F, age * 0.05F, 0.467F, 0.196F, 0.078F, 0.94F);
            renderOctahedron(m, vc, fist, 0.085F, 0.18F, -age * 0.08F, 1.00F, 0.76F, 0.47F, 0.92F);
        }

        // 2. 4 orbiting fractured granite/terracotta monolith shards circling the waist
        for (int s = 0; s < 4; s++) {
            float sAng = -age * 0.11F + (float) (Math.PI * 0.5D * s);
            Vec3 shardPos = new Vec3(Mth.cos(sAng) * 0.68D, 0.78D + 0.06D * Mth.sin(age * 0.16F + s), Mth.sin(sAng) * 0.68D);
            boolean granite = (s % 2 == 0);
            renderOctahedron(m, vc, shardPos, 0.085F, 0.18F, sAng,
                    granite ? 0.353F : 0.467F, granite ? 0.322F : 0.196F, granite ? 0.298F : 0.078F, 0.94F);
        }
    }

    /**
     * Section 6.6 Glacial Eidolon Agent (#98DCF2):
     * 6-pointed crystalline snowflake foot-ring, jagged 3D blue-ice spike pauldrons,
     * frost-blade forearm gauntlets, and 6 orbiting razor icicles.
     */
    private static void renderGlacialAgentOrnaments(
            Matrix4f m, VertexConsumer vc, float age, float bodyYawDeg,
            float pr, float pg, float pb, float ar, float ag, float ab
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ, 0.54F, 0.68F, 6, 0.60F, 0.86F, 0.95F, 0.85F);
        for (int arm = 0; arm < 6; arm++) {
            float fAng = age * 0.06F + (float) (Math.PI * 2.0D * arm / 6.0D);
            Vec3 aDir = new Vec3(Mth.cos(fAng), 0.0D, Mth.sin(fAng));
            Vec3 aTang = new Vec3(-Mth.sin(fAng), 0.0D, Mth.cos(fAng));
            Vec3 base = aDir.scale(0.16D).add(0.0D, 0.048D, 0.0D);
            Vec3 tip = aDir.scale(0.76D).add(0.0D, 0.048D, 0.0D);
            Vec3 aw = aTang.scale(0.036D);
            emitDoubleSidedQuad(m, vc, base.subtract(aw), base.add(aw), tip.add(aw.scale(0.3D)), tip.subtract(aw.scale(0.3D)),
                    0.94F, 0.99F, 1.00F, 0.90F);
        }

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 fwdDir = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));

        // 1. Twin jagged blue-ice spike pauldrons & crystalline frost-blade forearm gauntlets
        for (int side = -1; side <= 1; side += 2) {
            double bob = 0.025D * Mth.sin(age * 0.13F + (side > 0 ? 0.0F : 3.14F));
            Vec3 shoulder = new Vec3(0.0D, 1.44D + bob, 0.0D).add(rightDir.scale(side * 0.36D));
            renderOctahedron(m, vc, shoulder, 0.16F, 0.22F, yawRad + side * 0.45F, 0.17F, 0.51F, 0.79F, 0.90F);
            renderOctahedron(m, vc, shoulder.add(0.0D, 0.14D, 0.0D).add(rightDir.scale(side * 0.10D)),
                    0.085F, 0.26F, -yawRad, 0.60F, 0.86F, 0.95F, 0.94F);
            renderOctahedron(m, vc, shoulder.add(0.0D, 0.20D, 0.0D).add(rightDir.scale(side * 0.12D)),
                    0.042F, 0.22F, yawRad, 0.94F, 0.99F, 1.00F, 0.98F);

            Vec3 gauntlet = new Vec3(0.0D, 0.86D - bob, 0.0D)
                    .add(rightDir.scale(side * 0.38D))
                    .add(fwdDir.scale(0.24D));
            renderOctahedron(m, vc, gauntlet, 0.10F, 0.24F, yawRad, 0.60F, 0.86F, 0.95F, 0.92F);
            renderOctahedron(m, vc, gauntlet.add(fwdDir.scale(0.10D)), 0.048F, 0.26F, -yawRad, 0.94F, 0.99F, 1.00F, 0.98F);
        }

        // 2. 6 orbiting razor icicle needles circling the Glacial Agent's waist
        for (int s = 0; s < 6; s++) {
            float sAng = age * 0.14F + (float) (Math.PI * 2.0D * s / 6.0D);
            Vec3 iciclePos = new Vec3(Mth.cos(sAng) * 0.68D, 0.86D + 0.08D * Mth.sin(age * 0.18F + s), Mth.sin(sAng) * 0.68D);
            renderOctahedron(m, vc, iciclePos, 0.055F, 0.20F, sAng,
                    (s % 2 == 0) ? 0.60F : 0.17F, (s % 2 == 0) ? 0.86F : 0.51F, (s % 2 == 0) ? 0.95F : 0.79F, 0.92F);
        }
    }

    /**
     * Section 6.7 Infernal Eidolon Agent (#B83C08):
     * curved 3D volcanic demon horns, basalt-magma pauldrons, twin blazing triple-clawed forearm gauntlets
     * ("burning claws"), volcanic caldera foot-ring, and 4 orbiting hellfire embers.
     */
    private static void renderInfernalAgentOrnaments(
            Matrix4f m, VertexConsumer vc, float age, float bodyYawDeg,
            float pr, float pg, float pb, float ar, float ag, float ab
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.05D, 0.0D), flatX, flatZ, 0.46F, 0.62F, 20, 0.72F, 0.24F, 0.03F, 0.88F);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.06D, 0.0D), flatX, flatZ, 0.50F, 0.57F, 20, 1.00F, 0.82F, 0.36F, 0.94F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 fwdDir = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));

        // 1. Curved 3D Volcanic Demon Horns (#2A1712 basalt root -> #FFD15C molten tips)
        for (int side = -1; side <= 1; side += 2) {
            Vec3 brow = new Vec3(0.0D, 1.84D, 0.0D).add(rightDir.scale(side * 0.18D)).add(fwdDir.scale(0.05D));
            Vec3 mid = brow.add(rightDir.scale(side * 0.20D)).add(0.0D, 0.22D, 0.0D).subtract(fwdDir.scale(0.08D));
            Vec3 tip = mid.add(rightDir.scale(side * 0.06D)).add(0.0D, 0.26D, 0.0D).add(fwdDir.scale(0.16D));
            Vec3 w = rightDir.scale(0.042D);
            emitDoubleSidedQuad(m, vc, brow.subtract(w), brow.add(w), mid.add(w.scale(0.75D)), mid.subtract(w.scale(0.75D)),
                    0.17F, 0.09F, 0.07F, 0.96F);
            emitDoubleSidedQuad(m, vc, mid.subtract(w.scale(0.75D)), mid.add(w.scale(0.75D)), tip, tip,
                    1.00F, 0.82F, 0.36F, 0.98F);
        }

        // 2. Volcanic basalt-magma pauldrons & twin blazing triple-clawed forearm gauntlets ("burning claws"!)
        for (int side = -1; side <= 1; side += 2) {
            double bob = 0.025D * Mth.sin(age * 0.14F + (side > 0 ? 0.0F : 3.14F));
            Vec3 shoulder = new Vec3(0.0D, 1.44D + bob, 0.0D).add(rightDir.scale(side * 0.36D));
            renderOctahedron(m, vc, shoulder, 0.17F, 0.18F, yawRad + side * 0.35F, 0.17F, 0.09F, 0.07F, 0.95F);
            renderOctahedron(m, vc, shoulder.add(0.0D, 0.14D, 0.0D).add(rightDir.scale(side * 0.08D)),
                    0.085F, 0.22F, -yawRad, 1.00F, 0.82F, 0.36F, 0.96F);

            Vec3 wrist = new Vec3(0.0D, 0.86D - bob, 0.0D)
                    .add(rightDir.scale(side * 0.38D))
                    .add(fwdDir.scale(0.22D));
            renderOctahedron(m, vc, wrist, 0.11F, 0.13F, yawRad, 0.72F, 0.24F, 0.03F, 0.92F);
            for (int claw = -1; claw <= 1; claw++) {
                Vec3 cBase = wrist.add(rightDir.scale(claw * 0.055D)).add(fwdDir.scale(0.06D));
                Vec3 cTip = cBase.add(fwdDir.scale(0.36D)).add(0.0D, -0.06D + 0.02D * Math.abs(claw), 0.0D);
                Vec3 cw = new Vec3(0.0D, 0.028D, 0.0D);
                emitDoubleSidedQuad(m, vc, cBase.subtract(cw), cBase.add(cw), cTip, cTip,
                        (claw == 0) ? 1.00F : 0.92F, (claw == 0) ? 0.88F : 0.38F, (claw == 0) ? 0.44F : 0.06F, 0.96F);
            }
        }

        // 3. 4 orbiting blazing hellfire embers circling the Infernal Agent's waist
        for (int e = 0; e < 4; e++) {
            float eAng = -age * 0.16F + (float) (Math.PI * 0.5D * e);
            Vec3 emberPos = new Vec3(Mth.cos(eAng) * 0.68D, 0.88D + 0.08D * Mth.sin(age * 0.22F + e), Mth.sin(eAng) * 0.68D);
            renderOctahedron(m, vc, emberPos, 0.068F, 0.15F, eAng,
                    (e % 2 == 0) ? 1.00F : 0.72F, (e % 2 == 0) ? 0.82F : 0.24F, (e % 2 == 0) ? 0.36F : 0.03F, 0.94F);
        }
    }

    /**
     * Section 6.8 Solar Seraph Agent (#FFE278):
     * 6 articulated feathered solar-gold & ivory-white Seraph wings with aerial wing-beat,
     * floating golden Seraphic Halo, and 8-ray sunburst crown.
     */
    private static void renderSolarSeraphAgentOrnaments(
            Matrix4f m, VertexConsumer vc, float age, float bodyYawDeg,
            float pr, float pg, float pb, float ar, float ag, float ab
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.06D, 0.0D), flatX, flatZ, 0.44F, 0.58F, 22, 1.00F, 0.886F, 0.471F, 0.86F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 fwdDir = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));
        Vec3 backDir = fwdDir.scale(-1.0D);

        // 1. Floating Golden Seraphic Halo & 8-Ray Sunburst Crown above the Seraph's head
        double haloBob = 0.04D * Mth.sin(age * 0.14F);
        Vec3 haloPos = new Vec3(0.0D, 2.08D + haloBob, 0.0D);
        renderLocalRing(m, vc, haloPos, flatX, flatZ, 0.28F, 0.38F, 24, 1.00F, 0.886F, 0.471F, 0.95F);
        renderLocalRing(m, vc, haloPos.add(0.0D, 0.01D, 0.0D), flatX, flatZ, 0.31F, 0.35F, 24, 1.00F, 0.995F, 0.96F, 0.98F);
        for (int rIdx = 0; rIdx < 8; rIdx++) {
            float cAng = age * 0.05F + (float) (Math.PI * 2.0D * rIdx / 8.0D);
            Vec3 rBase = haloPos.add(Mth.cos(cAng) * 0.36D, 0.0D, Mth.sin(cAng) * 0.36D);
            Vec3 rTip = haloPos.add(Mth.cos(cAng) * 0.52D, 0.08D, Mth.sin(cAng) * 0.52D);
            Vec3 w = new Vec3(-Mth.sin(cAng) * 0.026D, 0.022D, Mth.cos(cAng) * 0.026D);
            emitDoubleSidedQuad(m, vc, rBase.subtract(w), rBase.add(w), rTip, rTip, 1.00F, 0.98F, 0.88F, 0.92F);
        }

        // 2. Six Articulated Feathered Solar Seraph Wings (Upper, Middle & Lower pairs beating gracefully!)
        for (int pair = 0; pair < 3; pair++) {
            double rootY = 1.48D - pair * 0.22D;
            float spanScale = (pair == 0) ? 1.15F : ((pair == 1) ? 0.92F : 0.70F);
            double pitchLift = (pair == 0) ? 0.38D : ((pair == 1) ? 0.06D : -0.26D);
            float flap = 0.28F + 0.24F * Mth.sin(age * 0.26F - pair * 0.45F);

            for (int side = -1; side <= 1; side += 2) {
                Vec3 root = new Vec3(0.0D, rootY, 0.0D).add(backDir.scale(0.16D)).add(rightDir.scale(side * 0.12D));
                Vec3 wingDir = rightDir.scale(side * Mth.cos(flap)).add(backDir.scale(Mth.sin(flap))).normalize();
                Vec3 elbow = root.add(wingDir.scale(0.52D * spanScale)).add(0.0D, pitchLift * 0.55D, 0.0D);
                Vec3 tip = elbow.add(wingDir.scale(0.62D * spanScale)).add(0.0D, pitchLift * 0.85D, 0.0D).add(backDir.scale(0.14D));

                // Golden leading-edge wing spar
                Vec3 sparW = new Vec3(0.0D, 0.036D, 0.0D);
                emitDoubleSidedQuad(m, vc, root.subtract(sparW), root.add(sparW), elbow.add(sparW), elbow.subtract(sparW),
                        1.00F, 0.886F, 0.471F, 0.96F);
                emitDoubleSidedQuad(m, vc, elbow.subtract(sparW.scale(0.75D)), elbow.add(sparW.scale(0.75D)), tip, tip,
                        1.00F, 0.99F, 0.92F, 0.98F);

                // 4 luminous ivory-gold primary flight feathers fanning downward from each wing!
                for (int f = 0; f < 4; f++) {
                    float u0 = f / 4.0F;
                    float u1 = (f + 1) / 4.0F;
                    Vec3 p0 = root.lerp(tip, 0.22D + 0.74D * u0);
                    Vec3 p1 = root.lerp(tip, 0.22D + 0.74D * u1);
                    double featherLen = (0.38D - 0.06D * f) * spanScale;
                    Vec3 fTip = p0.lerp(p1, 0.5D).add(0.0D, -featherLen, 0.0D).add(backDir.scale(0.12D * spanScale));
                    boolean ivory = (f % 2 == 0);
                    emitDoubleSidedQuad(m, vc, p0, p1, fTip, fTip,
                            1.00F, ivory ? 0.995F : 0.886F, ivory ? 0.95F : 0.471F, 0.90F);
                }
            }
        }
    }

    /**
     * Section 6.9 Umbral Shadow Stalker Agent (#211A21):
     * Assassin shadow-cowl crest, 4 trailing smoke-cloak ribbons, and twin reverse-grip Umbral Katar forearm blades!
     */
    private static void renderShadowStalkerAgentOrnaments(
            Matrix4f m, VertexConsumer vc, float age, float bodyYawDeg
    ) {
        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 fwdDir = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
        Vec3 rightDir = new Vec3(fwdDir.z, 0.0D, -fwdDir.x);
        Vec3 backDir = fwdDir.scale(-1.0D);

        // 1. Dark Eclipse Shadow-Cowl Halo framing the Assassin's head
        Vec3 cowlCenter = new Vec3(0.0D, 1.78D, 0.0D).add(backDir.scale(0.10D));
        renderLocalRing(m, vc, cowlCenter, rightDir, new Vec3(0.0D, 1.0D, 0.0D),
                0.22F, 0.34F, 18, 0.129F, 0.102F, 0.129F, 0.94F);
        renderLocalRing(m, vc, cowlCenter, rightDir, new Vec3(0.0D, 1.0D, 0.0D),
                0.33F, 0.37F, 18, 0.56F, 0.36F, 1.00F, 0.86F);

        // 2. Twin reverse-grip #211A21 & #8F5CFF Umbral Katar forearm blades
        for (int side = -1; side <= 1; side += 2) {
            Vec3 wrist = new Vec3(0.0D, 0.92D, 0.0D).add(rightDir.scale(side * 0.62D)).add(fwdDir.scale(0.18D));
            Vec3 tipFwd = wrist.add(fwdDir.scale(0.68D)).add(0.0D, -0.10D, 0.0D);
            Vec3 tipBack = wrist.add(backDir.scale(0.42D)).add(0.0D, 0.08D, 0.0D);
            Vec3 bladeW = new Vec3(0.0D, 0.065D, 0.0D);
            emitDoubleSidedQuad(m, vc, wrist.subtract(bladeW), wrist.add(bladeW), tipFwd, tipFwd,
                    0.129F, 0.102F, 0.129F, 0.96F);
            emitDoubleSidedQuad(m, vc, wrist.subtract(bladeW.scale(0.55D)), wrist.add(bladeW.scale(0.55D)), tipFwd, tipFwd,
                    0.56F, 0.36F, 1.00F, 0.90F);
            emitDoubleSidedQuad(m, vc, wrist.subtract(bladeW.scale(0.70D)), wrist.add(bladeW.scale(0.70D)), tipBack, tipBack,
                    0.129F, 0.102F, 0.129F, 0.92F);
        }

        // 3. 4 trailing tattered shadow-cloak ribbons billowing behind the ceiling-stalking assassin
        for (int c = 0; c < 4; c++) {
            double lat = (c - 1.5D) * 0.18D;
            Vec3 shoulder = new Vec3(0.0D, 1.44D, 0.0D).add(backDir.scale(0.20D)).add(rightDir.scale(lat));
            double sway = 0.12D * Mth.sin(age * 0.24F + c * 1.2F);
            Vec3 mid = shoulder.add(backDir.scale(0.36D)).add(rightDir.scale(sway * 0.5D)).add(0.0D, -0.58D, 0.0D);
            Vec3 hem = mid.add(backDir.scale(0.34D)).add(rightDir.scale(sway)).add(0.0D, -0.62D, 0.0D);
            Vec3 ribW = rightDir.scale(0.075D);
            emitDoubleSidedQuad(m, vc, shoulder.subtract(ribW), shoulder.add(ribW), mid.add(ribW), mid.subtract(ribW),
                    0.129F, 0.102F, 0.129F, 0.90F);
            emitDoubleSidedQuad(m, vc, mid.subtract(ribW), mid.add(ribW), hem, hem,
                    (c % 2 == 0) ? 0.129F : 0.44F, (c % 2 == 0) ? 0.102F : 0.26F, (c % 2 == 0) ? 0.129F : 0.78F, 0.76F);
        }
    }

    /**
     * Section 6.10 Cryptguard Eidolon Agent (#4D6453):
     * Desiccated tombstone ossuary crown, bleached skeletal ribcage chest/shoulder armor,
     * twin triple-taloned Withering Bone Claws on both forearms, and 4 orbiting necrotic soul motes!
     */
    private static void renderCryptguardAgentOrnaments(
            Matrix4f m, VertexConsumer vc, float age, float bodyYawDeg
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.05D, 0.0D), flatX, flatZ, 0.44F, 0.58F, 20, 0.302F, 0.392F, 0.325F, 0.88F);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.06D, 0.0D), flatX, flatZ, 0.48F, 0.53F, 20, 0.369F, 0.878F, 0.533F, 0.92F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 fwdDir = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));
        Vec3 backDir = fwdDir.scale(-1.0D);

        // 1. Tombstone Ossuary Skull-Crest Crown & Glowing #5EE088 Soul Eyes on Brow
        Vec3 browHub = new Vec3(0.0D, 1.84D, 0.0D).add(fwdDir.scale(0.06D));
        for (int spike = -1; spike <= 1; spike++) {
            Vec3 cBase = browHub.add(rightDir.scale(spike * 0.14D));
            Vec3 cTip = cBase.add(rightDir.scale(spike * 0.05D)).add(0.0D, (spike == 0 ? 0.28D : 0.20D), 0.0D);
            Vec3 cw = rightDir.scale(0.036D);
            emitDoubleSidedQuad(m, vc, cBase.subtract(cw), cBase.add(cw), cTip, cTip,
                    0.902F, 0.875F, 0.812F, 0.96F);
        }

        // 2. Skeletal Ribcage Chest Guard, Ossuary Pauldrons & Twin Triple-Taloned Withering Bone Claws!
        for (int side = -1; side <= 1; side += 2) {
            double bob = 0.025D * Mth.sin(age * 0.13F + (side > 0 ? 0.0F : 3.14F));
            Vec3 shoulder = new Vec3(0.0D, 1.44D + bob, 0.0D).add(rightDir.scale(side * 0.36D));
            renderOctahedron(m, vc, shoulder, 0.16F, 0.17F, yawRad + side * 0.35F, 0.302F, 0.392F, 0.325F, 0.95F);
            renderOctahedron(m, vc, shoulder.add(0.0D, 0.14D, 0.0D).add(rightDir.scale(side * 0.09D)),
                    0.075F, 0.22F, -yawRad, 0.902F, 0.875F, 0.812F, 0.96F);

            // 3 curved bleached ribs wrapping from spine around the Cryptguard's chest!
            for (int rIdx = 0; rIdx < 3; rIdx++) {
                double ry = 1.04D + rIdx * 0.13D;
                Vec3 spinePt = new Vec3(0.0D, ry, 0.0D).add(backDir.scale(0.15D));
                Vec3 flankPt = new Vec3(0.0D, ry - 0.02D, 0.0D).add(rightDir.scale(side * 0.26D));
                Vec3 sternumPt = new Vec3(0.0D, ry - 0.04D, 0.0D).add(fwdDir.scale(0.18D)).add(rightDir.scale(side * 0.05D));
                Vec3 rw = new Vec3(0.0D, 0.024D, 0.0D);
                emitDoubleSidedQuad(m, vc, spinePt.subtract(rw), spinePt.add(rw), flankPt.add(rw), flankPt.subtract(rw),
                        0.902F, 0.875F, 0.812F, 0.94F);
                emitDoubleSidedQuad(m, vc, flankPt.subtract(rw), flankPt.add(rw), sternumPt.add(rw), sternumPt.subtract(rw),
                        0.902F, 0.875F, 0.812F, 0.94F);
            }

            // Twin Withering Bone-Claw Gauntlets siphoning health on melee hit!
            Vec3 wrist = new Vec3(0.0D, 0.86D - bob, 0.0D)
                    .add(rightDir.scale(side * 0.38D))
                    .add(fwdDir.scale(0.22D));
            renderOctahedron(m, vc, wrist, 0.105F, 0.12F, yawRad, 0.302F, 0.392F, 0.325F, 0.94F);
            for (int claw = -1; claw <= 1; claw++) {
                Vec3 cBase = wrist.add(rightDir.scale(claw * 0.058D)).add(fwdDir.scale(0.06D));
                Vec3 cMid = cBase.add(fwdDir.scale(0.22D)).add(0.0D, 0.02D, 0.0D);
                Vec3 cTip = cMid.add(fwdDir.scale(0.22D)).add(0.0D, -0.09D + 0.02D * Math.abs(claw), 0.0D);
                Vec3 cw = new Vec3(0.0D, 0.028D, 0.0D);
                emitDoubleSidedQuad(m, vc, cBase.subtract(cw), cBase.add(cw), cMid.add(cw.scale(0.75D)), cMid.subtract(cw.scale(0.75D)),
                        0.902F, 0.875F, 0.812F, 0.96F);
                emitDoubleSidedQuad(m, vc, cMid.subtract(cw.scale(0.75D)), cMid.add(cw.scale(0.75D)), cTip, cTip,
                        0.369F, 0.878F, 0.533F, 0.98F);
            }
        }

        // 3. 4 orbiting #5EE088 necrotic soul wisps circling the Cryptguard Agent
        for (int s = 0; s < 4; s++) {
            float sAng = -age * 0.14F + (float) (Math.PI * 0.5D * s);
            Vec3 soulPos = new Vec3(Mth.cos(sAng) * 0.68D, 0.90D + 0.09D * Mth.sin(age * 0.20F + s), Mth.sin(sAng) * 0.68D);
            renderOctahedron(m, vc, soulPos, 0.062F, 0.13F, sAng,
                    (s % 2 == 0) ? 0.369F : 0.902F,
                    (s % 2 == 0) ? 0.878F : 0.875F,
                    (s % 2 == 0) ? 0.533F : 0.812F,
                    0.94F);
        }
    }

    /**
     * Section 6.11 Void-Stalker Agent (#41304E):
     * Deep cosmic violet rift horn-crest, floating black-hole singularity chest core, Rift-Edge pauldrons,
     * twin triple-bladed Armor-Piercing Void Claws, and 4 orbiting singularity motes!
     */
    private static void renderVoidStalkerAgentOrnaments(
            Matrix4f m, VertexConsumer vc, float age, float bodyYawDeg
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.05D, 0.0D), flatX, flatZ, 0.44F, 0.58F, 22, 0.255F, 0.188F, 0.306F, 0.90F);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.06D, 0.0D), flatX, flatZ, 0.49F, 0.54F, 22, 0.780F, 0.490F, 1.000F, 0.94F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 fwdDir = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));

        // 1. Cosmic Rift Horn-Crest on Brow
        Vec3 brow = new Vec3(0.0D, 1.85D, 0.0D).add(fwdDir.scale(0.05D));
        for (int side = -1; side <= 1; side += 2) {
            Vec3 hBase = brow.add(rightDir.scale(side * 0.13D));
            Vec3 hTip = hBase.add(rightDir.scale(side * 0.12D)).add(0.0D, 0.28D, 0.0D).subtract(fwdDir.scale(0.08D));
            Vec3 hw = rightDir.scale(0.038D);
            emitDoubleSidedQuad(m, vc, hBase.subtract(hw), hBase.add(hw), hTip, hTip,
                    0.780F, 0.490F, 1.000F, 0.96F);
        }

        // 2. Floating Singularity Chest Core + #C77DFF Accretion Ring
        Vec3 chestCore = new Vec3(0.0D, 1.22D, 0.0D).add(fwdDir.scale(0.19D));
        renderOctahedron(m, vc, chestCore, 0.095F, 0.115F, age * 0.18F, 0.045F, 0.025F, 0.070F, 0.98F);
        renderLocalRing(m, vc, chestCore, rightDir, new Vec3(0.0D, 1.0D, 0.0D), 0.11F, 0.155F, 16,
                0.780F, 0.490F, 1.000F, 0.94F);

        // 3. Rift-Edge Shoulder Pauldrons & Twin Triple-Bladed Armor-Piercing Void Claws!
        for (int side = -1; side <= 1; side += 2) {
            double bob = 0.025D * Mth.sin(age * 0.14F + (side > 0 ? 0.0F : 3.14F));
            Vec3 shoulder = new Vec3(0.0D, 1.44D + bob, 0.0D).add(rightDir.scale(side * 0.36D));
            renderOctahedron(m, vc, shoulder, 0.16F, 0.18F, yawRad + side * 0.35F, 0.255F, 0.188F, 0.306F, 0.96F);
            renderOctahedron(m, vc, shoulder.add(0.0D, 0.15D, 0.0D).add(rightDir.scale(side * 0.10D)),
                    0.075F, 0.24F, -yawRad, 0.780F, 0.490F, 1.000F, 0.95F);

            Vec3 wrist = new Vec3(0.0D, 0.86D - bob, 0.0D)
                    .add(rightDir.scale(side * 0.38D))
                    .add(fwdDir.scale(0.22D));
            renderOctahedron(m, vc, wrist, 0.105F, 0.12F, yawRad, 0.255F, 0.188F, 0.306F, 0.95F);
            for (int claw = -1; claw <= 1; claw++) {
                Vec3 cBase = wrist.add(rightDir.scale(claw * 0.060D)).add(fwdDir.scale(0.06D));
                Vec3 cMid = cBase.add(fwdDir.scale(0.24D)).add(0.0D, 0.02D, 0.0D);
                Vec3 cTip = cMid.add(fwdDir.scale(0.24D)).add(0.0D, -0.08D + 0.02D * Math.abs(claw), 0.0D);
                Vec3 cw = new Vec3(0.0D, 0.030D, 0.0D);
                emitDoubleSidedQuad(m, vc, cBase.subtract(cw), cBase.add(cw), cMid.add(cw.scale(0.75D)), cMid.subtract(cw.scale(0.75D)),
                        0.255F, 0.188F, 0.306F, 0.96F);
                emitDoubleSidedQuad(m, vc, cMid.subtract(cw.scale(0.75D)), cMid.add(cw.scale(0.75D)), cTip, cTip,
                        0.780F, 0.490F, 1.000F, 0.98F);
            }
        }

        // 4. 4 orbiting singularity motes around the Void-Stalker Agent
        for (int s = 0; s < 4; s++) {
            float sAng = -age * 0.15F + (float) (Math.PI * 0.5D * s);
            Vec3 motePos = new Vec3(Mth.cos(sAng) * 0.68D, 0.92D + 0.09D * Mth.sin(age * 0.22F + s), Mth.sin(sAng) * 0.68D);
            renderOctahedron(m, vc, motePos, 0.062F, 0.13F, sAng,
                    (s % 2 == 0) ? 0.780F : 0.045F,
                    (s % 2 == 0) ? 0.490F : 0.025F,
                    (s % 2 == 0) ? 1.000F : 0.070F,
                    0.95F);
        }
    }

    /**
     * Section 6.12 Dune-Dervish Agent (#C2B28F): desert keffiyeh turban crest, sandstone pauldrons,
     * whirling waist dust-devil crescents, twin curved scimitar Sandblades & 4 orbiting sandstone grains!
     */
    private static void renderDuneDervishAgentOrnaments(
            Matrix4f m, VertexConsumer vc, float age, float bodyYawDeg
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.05D, 0.0D), flatX, flatZ, 0.42F, 0.62F, 22, 0.761F, 0.698F, 0.561F, 0.90F);
        renderSweptArcBlade(m, vc, new Vec3(0.0D, 0.14D, 0.0D), 0.58F, age * 0.28F, 1.35F, 0.14F, 0.961F, 0.894F, 0.745F, 0.90F);
        renderSweptArcBlade(m, vc, new Vec3(0.0D, 0.14D, 0.0D), 0.58F, age * 0.28F + (float) Math.PI, 1.35F, 0.14F, 0.961F, 0.894F, 0.745F, 0.90F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 fwdDir = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));

        // 1. Sunlit Desert Turban Crest & Sandstone Brow Medallion
        Vec3 turbanCenter = new Vec3(0.0D, 1.84D, 0.0D);
        renderLocalRing(m, vc, turbanCenter, flatX, flatZ, 0.20F, 0.30F, 16, 0.761F, 0.698F, 0.561F, 0.96F);
        renderLocalRing(m, vc, turbanCenter.add(0.0D, 0.06D, 0.0D), flatX, flatZ, 0.16F, 0.26F, 16, 0.961F, 0.894F, 0.745F, 0.96F);
        renderOctahedron(m, vc, turbanCenter.add(fwdDir.scale(0.24D)).add(0.0D, 0.04D, 0.0D),
                0.055F, 0.085F, yawRad, 0.612F, 0.478F, 0.298F, 0.98F);

        // 2. Sandstone Shoulder Pauldrons & Twin Curved Scimitar Sandblades!
        for (int side = -1; side <= 1; side += 2) {
            double bob = 0.025D * Mth.sin(age * 0.16F + (side > 0 ? 0.0F : 3.14F));
            Vec3 shoulder = new Vec3(0.0D, 1.44D + bob, 0.0D).add(rightDir.scale(side * 0.36D));
            renderOctahedron(m, vc, shoulder, 0.16F, 0.15F, yawRad, 0.761F, 0.698F, 0.561F, 0.96F);
            renderOctahedron(m, vc, shoulder.add(0.0D, 0.12D, 0.0D).add(rightDir.scale(side * 0.08D)),
                    0.075F, 0.18F, yawRad, 0.612F, 0.478F, 0.298F, 0.95F);

            // Curved scimitar Sandblade extending forward from each hand
            Vec3 hilt = new Vec3(0.0D, 0.86D - bob, 0.0D)
                    .add(rightDir.scale(side * 0.38D))
                    .add(fwdDir.scale(0.18D));
            Vec3 midBlade = hilt.add(fwdDir.scale(0.32D)).add(0.0D, -0.04D, 0.0D);
            Vec3 tipBlade = midBlade.add(fwdDir.scale(0.34D)).add(0.0D, 0.16D, 0.0D);
            Vec3 spineW = new Vec3(0.0D, 0.052D, 0.0D);
            emitDoubleSidedQuad(m, vc, hilt.subtract(spineW), hilt.add(spineW), midBlade.add(spineW.scale(1.25D)), midBlade.subtract(spineW),
                    0.761F, 0.698F, 0.561F, 0.96F);
            emitDoubleSidedQuad(m, vc, midBlade.subtract(spineW), midBlade.add(spineW.scale(1.25D)), tipBlade, tipBlade,
                    0.961F, 0.894F, 0.745F, 0.98F);
        }

        // 3. 4 orbiting sandstone shrapnel grains whirling around the Dune-Dervish Agent
        for (int g = 0; g < 4; g++) {
            float gAng = age * 0.20F + (float) (Math.PI * 0.5D * g);
            Vec3 grainPos = new Vec3(Mth.cos(gAng) * 0.66D, 0.96D + 0.10D * Mth.sin(age * 0.24F + g), Mth.sin(gAng) * 0.66D);
            renderOctahedron(m, vc, grainPos, 0.060F, 0.11F, gAng,
                    (g % 2 == 0) ? 0.961F : 0.612F,
                    (g % 2 == 0) ? 0.894F : 0.478F,
                    (g % 2 == 0) ? 0.745F : 0.298F,
                    0.94F);
        }
    }

    /**
     * Section 6.13 Volt-Spark Agent (#FFE600): lightning-conductor brow horns, Tesla capacitor pauldrons,
     * twin high-voltage plasma arc-blades, underfoot spark-trail rings & 4 orbiting ball-lightning sparks!
     */
    private static void renderVoltSparkAgentOrnaments(
            Matrix4f m, VertexConsumer vc, float age, float bodyYawDeg
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.05D, 0.0D), flatX, flatZ, 0.44F, 0.62F, 22, 1.00F, 0.902F, 0.00F, 0.94F);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.06D, 0.0D), flatX, flatZ, 0.49F, 0.55F, 22, 1.00F, 1.00F, 0.95F, 0.98F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 fwdDir = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));

        // 1. Lightning-Conductor Brow Horns & Voltaic Chest Core
        Vec3 brow = new Vec3(0.0D, 1.85D, 0.0D).add(fwdDir.scale(0.05D));
        for (int side = -1; side <= 1; side += 2) {
            Vec3 hBase = brow.add(rightDir.scale(side * 0.12D));
            Vec3 hTip = hBase.add(rightDir.scale(side * 0.14D)).add(0.0D, 0.30D, 0.0D).add(fwdDir.scale(0.06D));
            Vec3 hw = rightDir.scale(0.036D);
            emitDoubleSidedQuad(m, vc, hBase.subtract(hw), hBase.add(hw), hTip, hTip,
                    1.00F, 0.902F, 0.00F, 0.98F);
        }
        Vec3 chestCore = new Vec3(0.0D, 1.22D, 0.0D).add(fwdDir.scale(0.19D));
        renderOctahedron(m, vc, chestCore, 0.105F, 0.105F, age * 0.35F, 1.00F, 1.00F, 0.95F, 0.99F);
        renderLocalRing(m, vc, chestCore, rightDir, new Vec3(0.0D, 1.0D, 0.0D), 0.115F, 0.165F, 16,
                1.00F, 0.902F, 0.00F, 0.95F);

        // 2. Copper/Gold Tesla Pauldrons & Twin Jagged High-Voltage Plasma Arc-Blades!
        for (int side = -1; side <= 1; side += 2) {
            double bob = 0.025D * Mth.sin(age * 0.22F + (side > 0 ? 0.0F : 3.14F));
            Vec3 shoulder = new Vec3(0.0D, 1.44D + bob, 0.0D).add(rightDir.scale(side * 0.36D));
            renderOctahedron(m, vc, shoulder, 0.16F, 0.15F, yawRad, 0.85F, 0.48F, 0.22F, 0.96F);
            renderOctahedron(m, vc, shoulder.add(0.0D, 0.14D, 0.0D).add(rightDir.scale(side * 0.08D)),
                    0.075F, 0.22F, yawRad, 1.00F, 0.902F, 0.00F, 0.98F);

            Vec3 wrist = new Vec3(0.0D, 0.86D - bob, 0.0D)
                    .add(rightDir.scale(side * 0.38D))
                    .add(fwdDir.scale(0.18D));
            Vec3 zigMid = wrist.add(fwdDir.scale(0.32D)).add(0.0D, 0.06D, 0.0D);
            Vec3 bladeTip = zigMid.add(fwdDir.scale(0.34D)).add(0.0D, -0.05D, 0.0D);
            Vec3 bw = new Vec3(0.0D, 0.048D, 0.0D);
            emitDoubleSidedQuad(m, vc, wrist.subtract(bw), wrist.add(bw), zigMid.add(bw), zigMid.subtract(bw),
                    1.00F, 0.902F, 0.00F, 0.96F);
            emitDoubleSidedQuad(m, vc, zigMid.subtract(bw), zigMid.add(bw), bladeTip, bladeTip,
                    1.00F, 1.00F, 0.96F, 0.99F);
        }

        // 3. 4 orbiting crackling Ball-Lightning sparks!
        for (int s = 0; s < 4; s++) {
            float sAng = age * 0.28F + (float) (Math.PI * 0.5D * s);
            Vec3 sparkPos = new Vec3(Mth.cos(sAng) * 0.68D, 0.94D + 0.10D * Mth.sin(age * 0.30F + s), Mth.sin(sAng) * 0.68D);
            renderOctahedron(m, vc, sparkPos, 0.065F, 0.065F, sAng,
                    (s % 2 == 0) ? 1.00F : 0.722F,
                    (s % 2 == 0) ? 0.902F : 0.957F,
                    (s % 2 == 0) ? 0.00F : 1.00F,
                    0.96F);
        }
    }

    /**
     * Section 6.14 Magma-Brute Agent (#B83C08): jagged volcanic obsidian/pumice shoulder crags,
     * glowing caldera furnace chest core, heavy molten-slag boulder fist gauntlets & burning magma footprint rings!
     */
    private static void renderMagmaBruteAgentOrnaments(
            Matrix4f m, VertexConsumer vc, float age, float bodyYawDeg
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ, 0.44F, 0.68F, 22, 0.165F, 0.090F, 0.071F, 0.94F);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.058D, 0.0D), flatX, flatZ, 0.48F, 0.58F, 22, 0.722F, 0.235F, 0.031F, 0.98F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 fwdDir = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));

        // 1. Volcanic Basalt Brow Crown & Boiling Caldera Furnace Chest Core
        Vec3 brow = new Vec3(0.0D, 1.88D, 0.0D).add(fwdDir.scale(0.04D));
        renderOctahedron(m, vc, brow, 0.16F, 0.12F, yawRad, 0.165F, 0.090F, 0.071F, 0.96F);
        renderOctahedron(m, vc, brow.add(0.0D, 0.10D, 0.0D), 0.085F, 0.14F, yawRad, 1.00F, 0.820F, 0.361F, 0.99F);

        Vec3 chestCore = new Vec3(0.0D, 1.20D, 0.0D).add(fwdDir.scale(0.20D));
        renderOctahedron(m, vc, chestCore, 0.14F, 0.14F, age * 0.20F, 0.722F, 0.235F, 0.031F, 0.96F);
        renderOctahedron(m, vc, chestCore, 0.095F, 0.095F, -age * 0.28F, 1.00F, 0.820F, 0.361F, 0.99F);

        // 2. Massive Jagged Obsidian-Pumice Pauldrons & Heavy Ground-Shaking Molten Boulder Fist Gauntlets!
        for (int side = -1; side <= 1; side += 2) {
            double bob = 0.025D * Mth.sin(age * 0.18F + (side > 0 ? 0.0F : 3.14F));
            Vec3 shoulder = new Vec3(0.0D, 1.46D + bob, 0.0D).add(rightDir.scale(side * 0.38D));
            renderOctahedron(m, vc, shoulder, 0.22F, 0.20F, yawRad, 0.165F, 0.090F, 0.071F, 0.96F);
            renderOctahedron(m, vc, shoulder.add(0.0D, 0.16D, 0.0D).add(rightDir.scale(side * 0.06D)),
                    0.10F, 0.24F, yawRad, 0.722F, 0.235F, 0.031F, 0.98F);

            Vec3 fist = new Vec3(0.0D, 0.82D - bob, 0.0D)
                    .add(rightDir.scale(side * 0.40D))
                    .add(fwdDir.scale(0.24D));
            renderOctahedron(m, vc, fist, 0.20F, 0.22F, yawRad, 0.165F, 0.090F, 0.071F, 0.96F);
            renderOctahedron(m, vc, fist.add(fwdDir.scale(0.06D)), 0.14F, 0.16F, -yawRad, 0.722F, 0.235F, 0.031F, 0.98F);
            renderOctahedron(m, vc, fist.add(fwdDir.scale(0.09D)), 0.08F, 0.09F, age * 0.25F, 1.00F, 0.820F, 0.361F, 0.99F);
        }
    }

    /**
     * Section 6.15 Tempest-Zephyr Agent (#429BA3): swept-back tempest-teal gale wings,
     * swirling monsoon chest core, twin pressurized squall-blade forearm vanes & slipstream updraft rings!
     */
    private static void renderTempestZephyrAgentOrnaments(
            Matrix4f m, VertexConsumer vc, float age, float bodyYawDeg
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.05D, 0.0D), flatX, flatZ, 0.44F, 0.64F, 22, 0.259F, 0.608F, 0.639F, 0.92F);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.06D, 0.0D), flatX, flatZ, 0.49F, 0.56F, 22, 0.847F, 0.980F, 1.000F, 0.98F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 fwdDir = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));

        Vec3 chestCore = new Vec3(0.0D, 1.22D, 0.0D).add(fwdDir.scale(0.19D));
        renderOctahedron(m, vc, chestCore, 0.11F, 0.14F, age * 0.35F, 0.847F, 0.980F, 1.000F, 0.99F);
        renderLocalRing(m, vc, chestCore, rightDir, new Vec3(0.0D, 1.0D, 0.0D), 0.12F, 0.18F, 16,
                0.259F, 0.608F, 0.639F, 0.94F);

        for (int side = -1; side <= 1; side += 2) {
            double flap = 0.06D * Mth.sin(age * 0.26F);
            Vec3 backWing = new Vec3(0.0D, 1.38D, 0.0D)
                    .add(rightDir.scale(side * 0.22D))
                    .subtract(fwdDir.scale(0.16D));
            Vec3 wingTip = backWing.add(rightDir.scale(side * 0.62D))
                    .add(0.0D, 0.38D + flap, 0.0D)
                    .subtract(fwdDir.scale(0.32D));
            Vec3 ww = new Vec3(0.0D, 0.09D, 0.0D);
            emitDoubleSidedQuad(m, vc, backWing.subtract(ww), backWing.add(ww), wingTip.add(ww.scale(0.4D)), wingTip.subtract(ww.scale(0.4D)),
                    0.259F, 0.608F, 0.639F, 0.88F);

            Vec3 wrist = new Vec3(0.0D, 0.86D, 0.0D)
                    .add(rightDir.scale(side * 0.38D))
                    .add(fwdDir.scale(0.18D));
            Vec3 vaneTip = wrist.add(fwdDir.scale(0.58D)).add(0.0D, 0.05D, 0.0D);
            Vec3 vw = new Vec3(0.0D, 0.052D, 0.0D);
            emitDoubleSidedQuad(m, vc, wrist.subtract(vw), wrist.add(vw), vaneTip, vaneTip,
                    0.847F, 0.980F, 1.000F, 0.96F);
        }
    }

    /**
     * Section 6.16 Glacial-Bruiser Agent (#98DCF2): heavy Blue-Ice shoulder pauldrons,
     * spiked permafrost brawler gauntlets, glowing absolute-zero chest core & Frost-Walker ring!
     */
    private static void renderGlacialBruiserAgentOrnaments(
            Matrix4f m, VertexConsumer vc, float age, float bodyYawDeg
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.05D, 0.0D), flatX, flatZ, 0.44F, 0.64F, 6, 0.173F, 0.510F, 0.788F, 0.92F);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.06D, 0.0D), flatX, flatZ, 0.49F, 0.57F, 18, 0.596F, 0.863F, 0.949F, 0.98F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 fwdDir = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));

        Vec3 chestCore = new Vec3(0.0D, 1.22D, 0.0D).add(fwdDir.scale(0.19D));
        renderOctahedron(m, vc, chestCore, 0.12F, 0.15F, age * 0.24F, 0.88F, 1.00F, 1.00F, 0.99F);
        renderOctahedron(m, vc, chestCore, 0.16F, 0.19F, -age * 0.16F, 0.596F, 0.863F, 0.949F, 0.86F);

        for (int side = -1; side <= 1; side += 2) {
            Vec3 shoulder = new Vec3(0.0D, 1.42D, 0.0D).add(rightDir.scale(side * 0.36D));
            renderOctahedron(m, vc, shoulder, 0.19F, 0.26F, yawRad, 0.173F, 0.510F, 0.788F, 0.94F);
            renderOctahedron(m, vc, shoulder.add(0.0D, 0.10D, 0.0D), 0.12F, 0.22F, -yawRad, 0.596F, 0.863F, 0.949F, 0.96F);

            Vec3 gauntlet = new Vec3(0.0D, 0.82D, 0.0D)
                    .add(rightDir.scale(side * 0.40D))
                    .add(fwdDir.scale(0.24D));
            renderOctahedron(m, vc, gauntlet, 0.20F, 0.22F, yawRad, 0.596F, 0.863F, 0.949F, 0.95F);
            renderOctahedron(m, vc, gauntlet.add(fwdDir.scale(0.08D)), 0.13F, 0.18F, -yawRad, 0.88F, 1.00F, 1.00F, 0.99F);
        }
    }

    /**
     * Section 6.17 Sylvan-Goliath Agent (#1A5923): massive mossy-stone shoulder boulders,
     * vine-wrapped earthen smasher fists, blooming emerald heart-stone & creeping root ring!
     */
    private static void renderSylvanGoliathAgentOrnaments(
            Matrix4f m, VertexConsumer vc, float age, float bodyYawDeg
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.05D, 0.0D), flatX, flatZ, 0.46F, 0.66F, 18, 0.467F, 0.196F, 0.078F, 0.92F);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.06D, 0.0D), flatX, flatZ, 0.50F, 0.58F, 18, 0.173F, 0.902F, 0.408F, 0.96F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 fwdDir = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));

        Vec3 heartStone = new Vec3(0.0D, 1.22D, 0.0D).add(fwdDir.scale(0.19D));
        renderOctahedron(m, vc, heartStone, 0.14F, 0.16F, age * 0.20F, 0.173F, 0.902F, 0.408F, 0.98F);
        renderOctahedron(m, vc, heartStone, 0.18F, 0.18F, -age * 0.14F, 0.467F, 0.196F, 0.078F, 0.85F);

        for (int side = -1; side <= 1; side += 2) {
            Vec3 boulder = new Vec3(0.0D, 1.44D, 0.0D).add(rightDir.scale(side * 0.38D));
            renderOctahedron(m, vc, boulder, 0.22F, 0.24F, yawRad, 0.467F, 0.196F, 0.078F, 0.95F);
            renderOctahedron(m, vc, boulder.add(0.0D, 0.08D, 0.0D), 0.16F, 0.18F, -yawRad, 0.102F, 0.349F, 0.137F, 0.92F);

            Vec3 smasher = new Vec3(0.0D, 0.80D, 0.0D)
                    .add(rightDir.scale(side * 0.42D))
                    .add(fwdDir.scale(0.24D));
            renderOctahedron(m, vc, smasher, 0.22F, 0.23F, yawRad, 0.467F, 0.196F, 0.078F, 0.96F);
            renderOctahedron(m, vc, smasher.add(fwdDir.scale(0.07D)), 0.15F, 0.17F, -yawRad, 0.173F, 0.902F, 0.408F, 0.98F);
        }
    }

    /**
     * Section 6.18 Dust-Stalker Agent (#C2B28F): swirling chalk-powder ankle cyclone,
     * chiseled sandstone shoulder pauldrons & twin abrasive mineral katar wrist-blades!
     */
    private static void renderDustStalkerAgentOrnaments(
            Matrix4f m, VertexConsumer vc, float age, float bodyYawDeg
    ) {
        renderOpenFrustumWall(m, vc, 0.04F, 0.66F, 0.24F, 0.50F, 16, age * 0.28F, 0.949F, 0.925F, 0.882F, 0.36F);
        for (int c = 0; c < 2; c++) {
            float cAng = age * 0.38F + c * (float) Math.PI;
            renderSweptArcBlade(m, vc, new Vec3(0.0D, 0.26D + c * 0.28D, 0.0D),
                    0.54F, cAng, 1.15F, 0.12F, 0.761F, 0.698F, 0.561F, 0.88F);
        }

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 fwdDir = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));

        for (int side = -1; side <= 1; side += 2) {
            Vec3 pauldron = new Vec3(0.0D, 1.40D, 0.0D).add(rightDir.scale(side * 0.34D));
            renderOctahedron(m, vc, pauldron, 0.16F, 0.18F, yawRad, 0.761F, 0.698F, 0.561F, 0.94F);

            Vec3 katar = new Vec3(0.0D, 0.84D, 0.0D)
                    .add(rightDir.scale(side * 0.36D))
                    .add(fwdDir.scale(0.32D));
            renderOctahedron(m, vc, katar, 0.11F, 0.28F, yawRad, 0.761F, 0.698F, 0.561F, 0.95F);
            renderOctahedron(m, vc, katar.add(fwdDir.scale(0.14D)), 0.065F, 0.22F, -yawRad, 0.949F, 0.925F, 0.882F, 0.99F);
        }
    }

    /**
     * Section 6.19 Spore-Tender Agent (#1A5923 / #A88654): wide-brimmed dual-tone mushroom cap hat
     * with golden gill underside, shoulder spore-puffball sacs & drifting green-gold pollen motes!
     */
    private static void renderSporeTenderAgentOrnaments(
            Matrix4f m, VertexConsumer vc, float age, float bodyYawDeg
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.05D, 0.0D), flatX, flatZ, 0.42F, 0.58F, 20, 0.173F, 0.902F, 0.408F, 0.84F);

        // Wide-brimmed #1A5923 mushroom cap hat with #A88654 golden gill underside
        double bob = 0.025D * Mth.sin(age * 0.14F);
        renderCylinderFrustum(m, vc, (float) (1.84D + bob), (float) (2.02D + bob), 0.58F, 0.34F, 12, age * 0.03F,
                0.102F, 0.349F, 0.137F, 0.96F);
        renderCylinderFrustum(m, vc, (float) (2.02D + bob), (float) (2.16D + bob), 0.34F, 0.06F, 12, age * 0.03F,
                0.173F, 0.902F, 0.408F, 0.95F);
        renderLocalRing(m, vc, new Vec3(0.0D, 1.835D + bob, 0.0D), flatX, flatZ, 0.18F, 0.56F, 18,
                0.910F, 0.784F, 0.510F, 0.92F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));
        for (int side = -1; side <= 1; side += 2) {
            Vec3 sac = new Vec3(0.0D, 1.38D + bob, 0.0D).add(rightDir.scale(side * 0.34D));
            renderOctahedron(m, vc, sac, 0.14F, 0.15F, yawRad, 0.102F, 0.349F, 0.137F, 0.94F);
            renderOctahedron(m, vc, sac.add(0.0D, 0.06D, 0.0D), 0.085F, 0.09F, -yawRad, 0.910F, 0.784F, 0.510F, 0.98F);
        }

        for (int i = 0; i < 4; i++) {
            float mAng = age * 0.18F + (float) (Math.PI * 0.5D * i);
            Vec3 mote = new Vec3(Mth.cos(mAng) * 0.62D, 0.95D + 0.18D * Mth.sin(age * 0.22F + i), Mth.sin(mAng) * 0.62D);
            boolean gold = (i % 2 == 0);
            renderOctahedron(m, vc, mote, 0.055F, 0.065F, mAng,
                    gold ? 0.910F : 0.173F, gold ? 0.784F : 0.902F, gold ? 0.510F : 0.408F, 0.92F);
        }
    }

    /**
     * Section 6.20 Boreal Stalker Agent (#1A5923 / #98DCF2): dual-tone evergreen conifer shoulder mantle,
     * frosted cyan hunter antler-crest & swirling snow/pine-needle trail motes!
     */
    private static void renderBorealStalkerAgentOrnaments(
            Matrix4f m, VertexConsumer vc, float age, float bodyYawDeg
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.05D, 0.0D), flatX, flatZ, 0.44F, 0.62F, 20, 0.596F, 0.863F, 0.949F, 0.86F);

        double bob = 0.025D * Mth.sin(age * 0.18F);
        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));

        // Frosted cyan antler-crest horns + evergreen pine mantle pauldrons
        for (int side = -1; side <= 1; side += 2) {
            Vec3 antler = new Vec3(0.0D, 1.96D + bob, 0.0D).add(rightDir.scale(side * 0.22D));
            renderOctahedron(m, vc, antler, 0.075F, 0.22F, yawRad + side * 0.35F, 0.596F, 0.863F, 0.949F, 0.96F);
            Vec3 pauldron = new Vec3(0.0D, 1.38D + bob, 0.0D).add(rightDir.scale(side * 0.36D));
            renderOctahedron(m, vc, pauldron, 0.15F, 0.12F, yawRad, 0.102F, 0.349F, 0.137F, 0.95F);
            renderOctahedron(m, vc, pauldron.add(0.0D, 0.08D, 0.0D), 0.085F, 0.14F, -yawRad, 0.596F, 0.863F, 0.949F, 0.92F);
        }

        for (int i = 0; i < 4; i++) {
            float mAng = age * 0.22F + (float) (Math.PI * 0.5D * i);
            Vec3 mote = new Vec3(Mth.cos(mAng) * 0.64D, 0.98D + 0.20D * Mth.sin(age * 0.25F + i), Mth.sin(mAng) * 0.64D);
            boolean frost = (i % 2 == 0);
            renderOctahedron(m, vc, mote, 0.055F, 0.075F, mAng,
                    frost ? 0.596F : 0.102F, frost ? 0.863F : 0.349F, frost ? 0.949F : 0.137F, 0.92F);
        }
    }

    /**
     * Section 6.21 Oasis-Warden Agent (#1A5923 / #C2B28F): sunlit desert-blossom lotus crown,
     * succulent cactus-flesh shoulder pauldrons & orbiting sand-gold + springwater droplet motes!
     */
    private static void renderOasisWardenAgentOrnaments(
            Matrix4f m, VertexConsumer vc, float age, float bodyYawDeg
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.05D, 0.0D), flatX, flatZ, 0.44F, 0.62F, 20, 0.220F, 0.780F, 0.960F, 0.86F);

        double bob = 0.025D * Mth.sin(age * 0.16F);
        renderCylinderFrustum(m, vc, (float) (1.84D + bob), (float) (2.00D + bob), 0.26F, 0.46F, 8, age * 0.04F,
                0.761F, 0.698F, 0.561F, 0.95F);
        renderOctahedron(m, vc, new Vec3(0.0D, 2.06D + bob, 0.0D), 0.11F, 0.16F, age * 0.08F,
                0.220F, 0.780F, 0.960F, 0.98F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));
        for (int side = -1; side <= 1; side += 2) {
            Vec3 pauldron = new Vec3(0.0D, 1.38D + bob, 0.0D).add(rightDir.scale(side * 0.36D));
            renderOctahedron(m, vc, pauldron, 0.15F, 0.13F, yawRad, 0.102F, 0.349F, 0.137F, 0.95F);
            renderOctahedron(m, vc, pauldron.add(0.0D, 0.07D, 0.0D), 0.08F, 0.14F, -yawRad, 0.761F, 0.698F, 0.561F, 0.96F);
        }

        for (int i = 0; i < 4; i++) {
            float mAng = age * 0.20F + (float) (Math.PI * 0.5D * i);
            Vec3 mote = new Vec3(Mth.cos(mAng) * 0.64D, 0.96D + 0.18D * Mth.sin(age * 0.24F + i), Mth.sin(mAng) * 0.64D);
            boolean water = (i % 2 == 0);
            renderOctahedron(m, vc, mote, 0.055F, 0.070F, mAng,
                    water ? 0.220F : 0.761F, water ? 0.780F : 0.698F, water ? 0.960F : 0.561F, 0.94F);
        }
    }

    /**
     * Section 6.22 Vitality Knight Agent (#FF6B9D): radiant rose-crystal paladin crest,
     * pulsating heart-core chest emblem, cellular helix pauldrons & Regeneration aura ring!
     */
    private static void renderVitalityKnightAgentOrnaments(
            Matrix4f m, VertexConsumer vc, float age, float bodyYawDeg
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        float beat = 0.88F + 0.16F * Mth.abs(Mth.sin(age * 0.24F));
        renderLocalRing(m, vc, new Vec3(0.0D, 0.05D, 0.0D), flatX, flatZ,
                0.48F * beat, 0.66F * beat, 22, 1.000F, 0.420F, 0.616F, 0.88F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 fwdDir = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));
        double bob = 0.025D * Mth.sin(age * 0.16F);

        // Pulsating rose-crystal heart emblem on chest + paladin halo crown
        Vec3 chestHeart = new Vec3(0.0D, 1.22D + bob, 0.0D).add(fwdDir.scale(0.18D));
        renderOctahedron(m, vc, chestHeart, 0.13F * beat, 0.16F * beat, yawRad, 1.000F, 0.420F, 0.616F, 0.96F);
        renderOctahedron(m, vc, chestHeart, 0.08F * beat, 0.11F * beat, -yawRad, 1.000F, 0.820F, 0.890F, 0.99F);
        renderLocalRing(m, vc, new Vec3(0.0D, 1.96D + bob, 0.0D), flatX, flatZ,
                0.24F, 0.34F, 18, 1.000F, 0.820F, 0.890F, 0.94F);

        for (int side = -1; side <= 1; side += 2) {
            Vec3 pauldron = new Vec3(0.0D, 1.40D + bob, 0.0D).add(rightDir.scale(side * 0.36D));
            renderOctahedron(m, vc, pauldron, 0.15F, 0.16F, yawRad, 1.000F, 0.420F, 0.616F, 0.95F);
            renderOctahedron(m, vc, pauldron.add(0.0D, 0.07D, 0.0D), 0.085F, 0.13F, -yawRad, 1.000F, 0.820F, 0.890F, 0.98F);
        }
    }

    /**
     * Section 6.23 Blood-Thrall Knight Agent (#8A0303): jagged hardened crimson helm horns,
     * coagulated blood-spike pauldrons, arterial chest-sigil & venous crimson siphon ring!
     */
    private static void renderBloodThrallKnightAgentOrnaments(
            Matrix4f m, VertexConsumer vc, float age, float bodyYawDeg
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        float pulse = 0.88F + 0.14F * Mth.abs(Mth.sin(age * 0.22F));
        renderLocalRing(m, vc, new Vec3(0.0D, 0.05D, 0.0D), flatX, flatZ,
                0.48F * pulse, 0.68F * pulse, 22, 0.541F, 0.012F, 0.012F, 0.90F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 fwdDir = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));
        double bob = 0.025D * Mth.sin(age * 0.16F);

        // Hardened #8A0303 crimson chest core + arterial scarlet inner crystal
        Vec3 chestCore = new Vec3(0.0D, 1.20D + bob, 0.0D).add(fwdDir.scale(0.18D));
        renderOctahedron(m, vc, chestCore, 0.14F * pulse, 0.18F * pulse, yawRad, 0.541F, 0.012F, 0.012F, 0.96F);
        renderOctahedron(m, vc, chestCore, 0.08F * pulse, 0.12F * pulse, -yawRad, 1.000F, 0.220F, 0.298F, 0.99F);

        // Twin hardened crimson knight helm horns & jagged blood-spike pauldrons
        for (int side = -1; side <= 1; side += 2) {
            Vec3 horn = new Vec3(0.0D, 1.92D + bob, 0.0D).add(rightDir.scale(side * 0.20D)).add(fwdDir.scale(0.05D));
            renderOctahedron(m, vc, horn, 0.065F, 0.18F, yawRad, 0.541F, 0.012F, 0.012F, 0.96F);

            Vec3 pauldron = new Vec3(0.0D, 1.40D + bob, 0.0D).add(rightDir.scale(side * 0.37D));
            renderOctahedron(m, vc, pauldron, 0.16F, 0.17F, yawRad, 0.541F, 0.012F, 0.012F, 0.96F);
            renderOctahedron(m, vc, pauldron.add(0.0D, 0.10D, 0.0D), 0.08F, 0.18F, -yawRad, 1.000F, 0.220F, 0.298F, 0.98F);
        }
    }

    /**
     * Section 6.24 Zephyr-Monk Agent (#FF6B9D / #A88654): levitating 6-bead pranic prayer halo,
     * golden-pink wind-sash arcs & ankle glider slipstream!
     */
    private static void renderZephyrMonkAgentOrnaments(
            Matrix4f m, VertexConsumer vc, float age, float bodyYawDeg
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        renderOpenFrustumWall(m, vc, 0.04F, 0.62F, 0.22F, 0.44F, 14, age * 0.24F,
                0.839F, 0.706F, 0.471F, 0.34F);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.05D, 0.0D), flatX, flatZ,
                0.44F, 0.64F, 22, 1.000F, 0.420F, 0.616F, 0.88F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 fwdDir = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
        double bob = 0.035D * Mth.sin(age * 0.18F);

        // Levitating 6-bead golden-pink monk prayer halo behind the Zephyr-Monk's shoulders!
        Vec3 haloCenter = new Vec3(0.0D, 1.56D + bob, 0.0D).subtract(fwdDir.scale(0.24D));
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));
        Vec3 upDir = new Vec3(0.0D, 1.0D, 0.0D);
        renderLocalRing(m, vc, haloCenter, rightDir, upDir, 0.38F, 0.44F, 20,
                0.839F, 0.706F, 0.471F, 0.90F);
        for (int b = 0; b < 6; b++) {
            float bAng = age * 0.08F + (float) (Math.PI * 2.0D * b / 6.0D);
            Vec3 bead = haloCenter.add(rightDir.scale(Mth.cos(bAng) * 0.41D)).add(upDir.scale(Mth.sin(bAng) * 0.41D));
            boolean pink = (b % 2 == 0);
            renderOctahedron(m, vc, bead, 0.065F, 0.075F, bAng,
                    pink ? 1.000F : 0.839F,
                    pink ? 0.420F : 0.706F,
                    pink ? 0.616F : 0.471F,
                    0.98F);
        }
    }

    /**
     * Section 6.25 Amber-Clad Sentinel Agent (#FF6B9D / #773214 / #F29B2E):
     * Petrified deep-earth amber helm crest, heavy hexagonal golden resin shoulder pauldrons & fossilized rose chest core!
     */
    private static void renderAmberSentinelAgentOrnaments(
            Matrix4f m, VertexConsumer vc, float age, float bodyYawDeg
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ,
                0.44F, 0.68F, 6, 0.949F, 0.608F, 0.180F, 0.86F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));
        Vec3 fwdDir = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
        double bob = 0.020D * Mth.sin(age * 0.14F);

        // Petrified #773214 / #F29B2E amber crown crest above the brow
        Vec3 crownPos = new Vec3(0.0D, 1.94D + bob, 0.0D).add(fwdDir.scale(0.08D));
        renderOctahedron(m, vc, crownPos, 0.11F, 0.18F, yawRad, 0.949F, 0.608F, 0.180F, 0.88F);
        renderOctahedron(m, vc, crownPos, 0.065F, 0.11F, yawRad, 1.000F, 0.420F, 0.616F, 0.98F);

        // Twin heavy petrified amber crystal shoulder pauldrons
        for (int side = -1; side <= 1; side += 2) {
            Vec3 shoulder = new Vec3(0.0D, 1.44D + bob, 0.0D).add(rightDir.scale(side * 0.40D));
            renderOctahedron(m, vc, shoulder, 0.15F, 0.19F, yawRad + side * 0.35F,
                    0.467F, 0.196F, 0.078F, 0.94F);
            renderOctahedron(m, vc, shoulder.add(0.0D, 0.08D, 0.0D), 0.10F, 0.13F, yawRad,
                    0.949F, 0.608F, 0.180F, 0.82F);
        }

        // Fossilized #FF6B9D rose crystal encased in golden resin at the Sentinel's chest!
        Vec3 chestGem = new Vec3(0.0D, 1.22D + bob, 0.0D).add(fwdDir.scale(0.18D));
        renderOctahedron(m, vc, chestGem, 0.10F, 0.13F, age * 0.08F, 0.949F, 0.608F, 0.180F, 0.72F);
        renderOctahedron(m, vc, chestGem, 0.065F, 0.085F, -age * 0.08F, 1.000F, 0.420F, 0.616F, 0.98F);
    }

    /**
     * Section 6.26 Eclipse-Knight Agent (#FFEA78 / #211A21):
     * Abyssal crescent helm horns rimmed in coronal gold, black-sun solar corona halo behind the shoulders & eclipse chest sigil!
     */
    private static void renderEclipseKnightAgentOrnaments(
            Matrix4f m, VertexConsumer vc, float age, float bodyYawDeg
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ,
                0.44F, 0.68F, 24, 1.000F, 0.918F, 0.471F, 0.88F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));
        Vec3 fwdDir = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
        Vec3 upDir = new Vec3(0.0D, 1.0D, 0.0D);
        double bob = 0.025D * Mth.sin(age * 0.16F);

        // Blazing Black-Sun Corona Halo behind the Eclipse-Knight's head & shoulders!
        Vec3 haloCenter = new Vec3(0.0D, 1.62D + bob, 0.0D).subtract(fwdDir.scale(0.24D));
        renderLocalRing(m, vc, haloCenter, rightDir, upDir, 0.0F, 0.30F, 20,
                0.095F, 0.075F, 0.100F, 0.96F);
        renderLocalRing(m, vc, haloCenter, rightDir, upDir, 0.30F, 0.44F, 24,
                1.000F, 0.918F, 0.471F, 0.96F);

        // Twin crescent coronal horns rising from the Eclipse-Knight's brow
        for (int side = -1; side <= 1; side += 2) {
            Vec3 hornBase = new Vec3(0.0D, 1.84D + bob, 0.0D).add(rightDir.scale(side * 0.17D)).add(fwdDir.scale(0.08D));
            Vec3 hornMid = hornBase.add(rightDir.scale(side * 0.07D)).add(0.0D, 0.14D, 0.0D);
            renderOctahedron(m, vc, hornMid, 0.065F, 0.22F, yawRad + side * 0.35F, 1.000F, 0.918F, 0.471F, 0.96F);
            renderOctahedron(m, vc, hornBase, 0.06F, 0.07F, yawRad, 0.129F, 0.102F, 0.129F, 0.96F);
        }
    }

    /**
     * Section 6.27 Blight-Revenant Agent (#4D6453 / #34CC48):
     * Rotting bone-bark shoulder pauldrons, skeletal thorn brow crown & festering necrotic spore core!
     */
    private static void renderBlightRevenantAgentOrnaments(
            Matrix4f m, VertexConsumer vc, float age, float bodyYawDeg
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ,
                0.44F, 0.66F, 16, 0.204F, 0.800F, 0.282F, 0.86F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));
        Vec3 fwdDir = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
        double bob = 0.024D * Mth.sin(age * 0.18F);

        // Skeletal bone-briar crown & rotting bark shoulder pauldrons
        for (int side = -1; side <= 1; side += 2) {
            Vec3 browSpike = new Vec3(0.0D, 1.94D + bob, 0.0D).add(rightDir.scale(side * 0.18D));
            renderOctahedron(m, vc, browSpike, 0.065F, 0.20F, yawRad + side * 0.30F, 0.902F, 0.875F, 0.812F, 0.96F);
            Vec3 shoulder = new Vec3(0.0D, 1.42D + bob, 0.0D).add(rightDir.scale(side * 0.38D));
            renderOctahedron(m, vc, shoulder, 0.15F, 0.14F, yawRad, 0.302F, 0.392F, 0.325F, 0.95F);
            renderOctahedron(m, vc, shoulder.add(0.0D, 0.09D, 0.0D), 0.075F, 0.17F, -yawRad, 0.204F, 0.800F, 0.282F, 0.94F);
        }

        // Festering #34CC48 necrotic heart pustule at chest
        Vec3 chest = new Vec3(0.0D, 1.20D + bob, 0.0D).add(fwdDir.scale(0.18D));
        renderOctahedron(m, vc, chest, 0.09F, 0.12F, age * 0.14F, 0.204F, 0.800F, 0.282F, 0.96F);
    }

    /**
     * Section 6.28 Astral-Phase Stalker Agent (#41304E / #FFE600):
     * Cosmic violet phase halo, electric gold pulsar gauntlets/pauldrons & levitating star-matter chest core!
     */
    private static void renderAstralPhaseStalkerAgentOrnaments(
            Matrix4f m, VertexConsumer vc, float age, float bodyYawDeg
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.05D, 0.0D), flatX, flatZ,
                0.42F, 0.64F, 22, 1.000F, 0.902F, 0.000F, 0.88F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));
        Vec3 fwdDir = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
        double bob = 0.032D * Mth.sin(age * 0.22F);

        // Crown pulsar ring & dual-tone cosmic violet / electric gold shoulder pylons
        renderLocalRing(m, vc, new Vec3(0.0D, 1.98D + bob, 0.0D), flatX, flatZ,
                0.18F, 0.28F, 18, 1.000F, 0.902F, 0.000F, 0.95F);
        for (int side = -1; side <= 1; side += 2) {
            Vec3 shoulder = new Vec3(0.0D, 1.44D + bob, 0.0D).add(rightDir.scale(side * 0.40D));
            renderOctahedron(m, vc, shoulder, 0.15F, 0.16F, yawRad, 0.255F, 0.188F, 0.306F, 0.96F);
            renderOctahedron(m, vc, shoulder.add(0.0D, 0.10D, 0.0D), 0.08F, 0.18F, -age * 0.18F, 1.000F, 0.902F, 0.000F, 0.98F);
        }

        Vec3 chest = new Vec3(0.0D, 1.22D + bob, 0.0D).add(fwdDir.scale(0.18D));
        renderOctahedron(m, vc, chest, 0.095F, 0.13F, age * 0.24F, 1.000F, 0.902F, 0.000F, 0.98F);
    }

    /**
     * Section 6.29 Soul-Harvester Revenant Agent (#1DF0E5 / #0D6469):
     * Cyan soulflame horned visage, soul-ember teal shoulder braziers & burning blue soul-heart!
     */
    private static void renderSoulHarvesterAgentOrnaments(
            Matrix4f m, VertexConsumer vc, float age, float bodyYawDeg
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ,
                0.44F, 0.66F, 20, 0.114F, 0.941F, 0.898F, 0.88F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));
        Vec3 fwdDir = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
        double bob = 0.028D * Mth.sin(age * 0.22F);

        for (int side = -1; side <= 1; side += 2) {
            Vec3 horn = new Vec3(0.0D, 1.96D + bob, 0.0D).add(rightDir.scale(side * 0.18D));
            renderOctahedron(m, vc, horn, 0.065F, 0.22F, yawRad + side * 0.25F, 0.114F, 0.941F, 0.898F, 0.96F);
            Vec3 shoulder = new Vec3(0.0D, 1.43D + bob, 0.0D).add(rightDir.scale(side * 0.39D));
            renderOctahedron(m, vc, shoulder, 0.15F, 0.14F, yawRad, 0.051F, 0.392F, 0.412F, 0.96F);
            renderOctahedron(m, vc, shoulder.add(0.0D, 0.12D, 0.0D), 0.085F, 0.20F, age * 0.20F, 0.114F, 0.941F, 0.898F, 0.96F);
        }

        Vec3 chest = new Vec3(0.0D, 1.21D + bob, 0.0D).add(fwdDir.scale(0.18D));
        renderOctahedron(m, vc, chest, 0.095F, 0.14F, -age * 0.22F, 0.114F, 0.941F, 0.898F, 0.98F);
    }

    /**
     * Section 6.30 Boiler-Knight Agent (#E0F7FA / #30A9B0):
     * Pressurized aquatic-cyan shoulder boilers, venting white steam stacks & boiling chest pressure core!
     */
    private static void renderBoilerKnightAgentOrnaments(
            Matrix4f m, VertexConsumer vc, float age, float bodyYawDeg
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.045D, 0.0D), flatX, flatZ,
                0.44F, 0.68F, 22, 0.878F, 0.969F, 0.980F, 0.86F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));
        Vec3 fwdDir = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
        double bob = 0.026D * Mth.sin(age * 0.24F);

        for (int side = -1; side <= 1; side += 2) {
            Vec3 shoulder = new Vec3(0.0D, 1.44D + bob, 0.0D).add(rightDir.scale(side * 0.40D));
            renderOctahedron(m, vc, shoulder, 0.16F, 0.15F, yawRad, 0.188F, 0.663F, 0.690F, 0.96F);
            Vec3 ventStack = shoulder.add(0.0D, 0.20D, -0.04D);
            renderOctahedron(m, vc, ventStack, 0.075F, 0.18F, age * 0.22F, 0.878F, 0.969F, 0.980F, 0.94F);
        }

        Vec3 chest = new Vec3(0.0D, 1.21D + bob, 0.0D).add(fwdDir.scale(0.18D));
        renderOctahedron(m, vc, chest, 0.10F, 0.12F, age * 0.26F, 0.188F, 0.663F, 0.690F, 0.98F);
    }

    /**
     * Section 6.31 White-Hole Paladin Agent (#FFFFFF / #41304E): incandescent white-hole halo crown,
     * cosmic violet event-horizon pauldrons, and twin buff-purging antimatter wing-blades!
     */
    private static void renderWhiteHolePaladinAgentOrnaments(
            Matrix4f m, VertexConsumer vc, float age, float bodyYawDeg
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.04D, 0.0D), flatX, flatZ,
                0.46F, 0.72F, 24, 0.255F, 0.188F, 0.306F, 0.92F);
        renderLocalRing(m, vc, new Vec3(0.0D, 2.02D, 0.0D), flatX, flatZ,
                0.24F, 0.38F, 22, 1.000F, 1.000F, 1.000F, 0.97F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));
        Vec3 fwdDir = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
        double bob = 0.028D * Mth.sin(age * 0.24F);

        for (int side = -1; side <= 1; side += 2) {
            Vec3 shoulder = new Vec3(0.0D, 1.44D + bob, 0.0D).add(rightDir.scale(side * 0.42D));
            renderOctahedron(m, vc, shoulder, 0.16F, 0.15F, yawRad, 0.255F, 0.188F, 0.306F, 0.96F);
            Vec3 bladeCrest = shoulder.add(rightDir.scale(side * 0.08D)).add(0.0D, 0.22D, -0.06D);
            renderOctahedron(m, vc, bladeCrest, 0.075F, 0.24F, yawRad, 1.000F, 1.000F, 1.000F, 0.98F);
        }

        Vec3 chest = new Vec3(0.0D, 1.22D + bob, 0.0D).add(fwdDir.scale(0.18D));
        renderOctahedron(m, vc, chest, 0.11F, 0.14F, age * 0.28F, 1.000F, 1.000F, 1.000F, 0.99F);
        renderOctahedron(m, vc, chest, 0.15F, 0.18F, -age * 0.20F, 0.255F, 0.188F, 0.306F, 0.78F);
    }

    /**
     * Section 6.32 Void-Shadow Stalker Agent (#110B17 / #41304E): silent abyssal cowl ring,
     * cosmic-violet event-horizon pauldrons with pitch-black shadow-blades, and a black-hole chest core!
     */
    private static void renderVoidShadowStalkerAgentOrnaments(
            Matrix4f m, VertexConsumer vc, float age, float bodyYawDeg
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.04D, 0.0D), flatX, flatZ,
                0.46F, 0.74F, 24, 0.067F, 0.043F, 0.090F, 0.96F);
        renderLocalRing(m, vc, new Vec3(0.0D, 1.98D, 0.0D), flatX, flatZ,
                0.22F, 0.36F, 22, 0.255F, 0.188F, 0.306F, 0.95F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));
        Vec3 fwdDir = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
        double bob = 0.026D * Mth.sin(age * 0.22F);

        for (int side = -1; side <= 1; side += 2) {
            Vec3 shoulder = new Vec3(0.0D, 1.43D + bob, 0.0D).add(rightDir.scale(side * 0.42D));
            renderOctahedron(m, vc, shoulder, 0.16F, 0.15F, yawRad, 0.255F, 0.188F, 0.306F, 0.96F);
            Vec3 shadowHorn = shoulder.add(rightDir.scale(side * 0.08D)).add(0.0D, 0.22D, -0.07D);
            renderOctahedron(m, vc, shadowHorn, 0.075F, 0.24F, yawRad, 0.067F, 0.043F, 0.090F, 0.99F);
        }

        Vec3 chest = new Vec3(0.0D, 1.22D + bob, 0.0D).add(fwdDir.scale(0.18D));
        renderOctahedron(m, vc, chest, 0.11F, 0.14F, -age * 0.26F, 0.067F, 0.043F, 0.090F, 0.99F);
        renderOctahedron(m, vc, chest, 0.15F, 0.18F, age * 0.18F, 0.255F, 0.188F, 0.306F, 0.82F);
    }

    /**
     * Section 6.33 Dawn-Strider Agent (#FFEA78 / #A88654): blazing sunrise halo crown,
     * atmospheric glider wing-vanes, and a daybreak sun-core chest emblem!
     */
    private static void renderDawnStriderAgentOrnaments(
            Matrix4f m, VertexConsumer vc, float age, float bodyYawDeg
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.04D, 0.0D), flatX, flatZ,
                0.46F, 0.74F, 24, 0.659F, 0.525F, 0.329F, 0.92F);
        renderLocalRing(m, vc, new Vec3(0.0D, 2.02D, 0.0D), flatX, flatZ,
                0.24F, 0.38F, 24, 1.000F, 0.918F, 0.471F, 0.98F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));
        Vec3 fwdDir = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
        double bob = 0.028D * Mth.sin(age * 0.26F);

        for (int side = -1; side <= 1; side += 2) {
            Vec3 shoulder = new Vec3(0.0D, 1.44D + bob, 0.0D).add(rightDir.scale(side * 0.42D));
            renderOctahedron(m, vc, shoulder, 0.16F, 0.14F, yawRad, 0.659F, 0.525F, 0.329F, 0.96F);
            Vec3 wingVane = shoulder.add(rightDir.scale(side * 0.12D)).add(0.0D, 0.22D, -0.10D);
            renderOctahedron(m, vc, wingVane, 0.085F, 0.25F, yawRad, 1.000F, 0.918F, 0.471F, 0.98F);
        }

        Vec3 chest = new Vec3(0.0D, 1.22D + bob, 0.0D).add(fwdDir.scale(0.18D));
        renderOctahedron(m, vc, chest, 0.11F, 0.15F, age * 0.30F, 1.000F, 0.918F, 0.471F, 0.99F);
        renderOctahedron(m, vc, chest, 0.15F, 0.18F, -age * 0.22F, 0.659F, 0.525F, 0.329F, 0.80F);
    }

    /**
     * Section 6.34 Tentacled Kraken-Husk Agent (#211A21 / #225C7C): writhing cephalopod ink-tentacle mantle,
     * deep-trench navy carapace pauldrons, and an abyssal hydrostatic chest core!
     */
    private static void renderTentacledKrakenHuskAgentOrnaments(
            Matrix4f m, VertexConsumer vc, float age, float bodyYawDeg
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.04D, 0.0D), flatX, flatZ,
                0.48F, 0.78F, 24, 0.129F, 0.102F, 0.129F, 0.96F);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.06D, 0.0D), flatX, flatZ,
                0.66F, 0.86F, 24, 0.133F, 0.361F, 0.486F, 0.88F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));
        Vec3 fwdDir = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
        double bob = 0.026D * Mth.sin(age * 0.24F);

        for (int side = -1; side <= 1; side += 2) {
            Vec3 shoulder = new Vec3(0.0D, 1.44D + bob, 0.0D).add(rightDir.scale(side * 0.42D));
            renderOctahedron(m, vc, shoulder, 0.17F, 0.14F, yawRad, 0.133F, 0.361F, 0.486F, 0.96F);
            Vec3 trenchSpike = shoulder.add(rightDir.scale(side * 0.10D)).add(0.0D, 0.20D, -0.08D);
            renderOctahedron(m, vc, trenchSpike, 0.085F, 0.22F, yawRad, 0.129F, 0.102F, 0.129F, 0.98F);
        }

        // 6 writhing cephalopod ink tentacles curling from the back & waist!
        for (int t = 0; t < 6; t++) {
            float tSide = (t - 2.5F) * 0.22F;
            double sway = 0.14D * Mth.sin(age * 0.26F + t * 1.15F);
            Vec3 tRoot = new Vec3(0.0D, 1.05D + bob, 0.0D)
                    .add(rightDir.scale(tSide))
                    .subtract(fwdDir.scale(0.18D));
            Vec3 tMid = tRoot.add(rightDir.scale(tSide * 0.85D + sway * 0.5D))
                    .subtract(fwdDir.scale(0.28D))
                    .add(0.0D, -0.26D, 0.0D);
            Vec3 tTip = tMid.add(rightDir.scale(sway))
                    .subtract(fwdDir.scale(0.24D))
                    .add(0.0D, -0.30D, 0.0D);
            boolean ink = (t % 2 == 0);
            float cr = ink ? 0.129F : 0.133F;
            float cg = ink ? 0.102F : 0.361F;
            float cb = ink ? 0.129F : 0.486F;
            Vec3 perp = new Vec3(0.038D, 0.022D, 0.038D);
            emitDoubleSidedQuad(m, vc, tRoot.subtract(perp), tRoot.add(perp), tMid.add(perp), tMid.subtract(perp), cr, cg, cb, 0.96F);
            emitDoubleSidedQuad(m, vc, tMid.subtract(perp), tMid.add(perp), tTip.add(perp), tTip.subtract(perp), cr, cg, cb, 0.90F);
        }

        Vec3 chest = new Vec3(0.0D, 1.22D + bob, 0.0D).add(fwdDir.scale(0.18D));
        renderOctahedron(m, vc, chest, 0.11F, 0.14F, -age * 0.26F, 0.129F, 0.102F, 0.129F, 0.99F);
        renderOctahedron(m, vc, chest, 0.15F, 0.18F, age * 0.20F, 0.133F, 0.361F, 0.486F, 0.84F);
    }

    /**
     * Section 6.35 Bulwark-Paladin Agent (#FFEA78 / #773214): glowing golden rune halo,
     * heavy granite tower shield on the off-arm, holy granite warhammer, and gilded pauldrons!
     */
    private static void renderBulwarkPaladinAgentOrnaments(
            Matrix4f m, VertexConsumer vc, float age, float bodyYawDeg
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.04D, 0.0D), flatX, flatZ,
                0.48F, 0.78F, 8, 0.467F, 0.196F, 0.078F, 0.96F);
        renderLocalRing(m, vc, new Vec3(0.0D, 2.04D, 0.0D), flatX, flatZ,
                0.24F, 0.38F, 24, 1.000F, 0.918F, 0.471F, 0.99F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));
        Vec3 fwdDir = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
        double bob = 0.022D * Mth.sin(age * 0.22F);

        for (int side = -1; side <= 1; side += 2) {
            Vec3 shoulder = new Vec3(0.0D, 1.44D + bob, 0.0D).add(rightDir.scale(side * 0.44D));
            renderOctahedron(m, vc, shoulder, 0.18F, 0.15F, yawRad, 0.467F, 0.196F, 0.078F, 0.98F);
            renderOctahedron(m, vc, shoulder.add(0.0D, 0.12D, 0.0D), 0.10F, 0.14F, yawRad, 1.000F, 0.918F, 0.471F, 0.99F);
        }

        // Left-side heavy golden-granite tower shield!
        Vec3 shieldCenter = new Vec3(0.0D, 1.06D + bob, 0.0D).subtract(rightDir.scale(0.50D)).add(fwdDir.scale(0.24D));
        Vec3 shTang = rightDir.scale(0.22D);
        Vec3 shUp = new Vec3(0.0D, 0.44D, 0.0D);
        emitDoubleSidedQuad(m, vc, shieldCenter.subtract(shTang).add(shUp), shieldCenter.add(shTang).add(shUp),
                shieldCenter.add(shTang).subtract(shUp), shieldCenter.subtract(shTang).subtract(shUp),
                0.467F, 0.196F, 0.078F, 0.99F);
        renderOctahedron(m, vc, shieldCenter.add(fwdDir.scale(0.04D)), 0.10F, 0.22F, yawRad, 1.000F, 0.918F, 0.471F, 0.99F);

        // Right-side holy warhammer head!
        Vec3 hammerHead = new Vec3(0.0D, 1.02D + bob, 0.0D).add(rightDir.scale(0.48D)).add(fwdDir.scale(0.32D));
        renderOctahedron(m, vc, hammerHead, 0.15F, 0.12F, yawRad, 1.000F, 0.918F, 0.471F, 0.99F);

        Vec3 chest = new Vec3(0.0D, 1.22D + bob, 0.0D).add(fwdDir.scale(0.18D));
        renderOctahedron(m, vc, chest, 0.12F, 0.16F, age * 0.24F, 1.000F, 0.918F, 0.471F, 0.99F);
    }

    /**
     * Section 6.36 Borealis-Archon Agent (#FFEA78 / #98DCF2): solar-gold polar halo crown,
     * crystalline cryo-cyan pauldrons, 4 shimmering aurora light veils, and a refracting cryo-solar chest prism!
     */
    private static void renderBorealisArchonAgentOrnaments(
            Matrix4f m, VertexConsumer vc, float age, float bodyYawDeg
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.04D, 0.0D), flatX, flatZ,
                0.48F, 0.76F, 6, 0.596F, 0.863F, 0.949F, 0.95F);
        renderLocalRing(m, vc, new Vec3(0.0D, 2.04D, 0.0D), flatX, flatZ,
                0.24F, 0.38F, 24, 1.000F, 0.918F, 0.471F, 0.99F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));
        Vec3 fwdDir = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
        double bob = 0.026D * Mth.sin(age * 0.24F);

        for (int side = -1; side <= 1; side += 2) {
            Vec3 shoulder = new Vec3(0.0D, 1.44D + bob, 0.0D).add(rightDir.scale(side * 0.42D));
            renderOctahedron(m, vc, shoulder, 0.16F, 0.15F, yawRad, 0.596F, 0.863F, 0.949F, 0.96F);
            Vec3 frostSpire = shoulder.add(rightDir.scale(side * 0.10D)).add(0.0D, 0.24D, -0.08D);
            renderOctahedron(m, vc, frostSpire, 0.085F, 0.26F, yawRad, 1.000F, 0.918F, 0.471F, 0.99F);
        }

        // 4 shimmering northern-light veil curtains draping gracefully behind the Borealis-Archon!
        for (int v = 0; v < 4; v++) {
            float sideOff = (v - 1.5F) * 0.22F;
            double wave = 0.10D * Mth.sin(age * 0.22F + v * 1.35F);
            Vec3 topPt = new Vec3(0.0D, 1.40D + bob, 0.0D).add(rightDir.scale(sideOff)).subtract(fwdDir.scale(0.18D));
            Vec3 botPt = topPt.add(rightDir.scale(wave)).subtract(fwdDir.scale(0.28D)).add(0.0D, -0.92D, 0.0D);
            Vec3 tang = rightDir.scale(0.085D);
            boolean gold = (v % 2 == 0);
            emitDoubleSidedQuad(m, vc, topPt.subtract(tang), topPt.add(tang), botPt.add(tang), botPt.subtract(tang),
                    gold ? 1.000F : 0.596F,
                    gold ? 0.918F : 0.863F,
                    gold ? 0.471F : 0.949F,
                    0.82F);
        }

        Vec3 chest = new Vec3(0.0D, 1.22D + bob, 0.0D).add(fwdDir.scale(0.18D));
        renderOctahedron(m, vc, chest, 0.11F, 0.16F, age * 0.28F, 1.000F, 0.918F, 0.471F, 0.99F);
        renderOctahedron(m, vc, chest, 0.15F, 0.20F, -age * 0.20F, 0.596F, 0.863F, 0.949F, 0.84F);
    }

    /**
     * Section 6.37 Mirage-Duelist Agent (#FFEA78 / #C2B28F): sunlit golden duelist halo,
     * desert sandstone scimitar pauldrons, and 2 flickering 3D phantom afterimages trailing behind every step!
     */
    private static void renderMirageDuelistAgentOrnaments(
            Matrix4f m, VertexConsumer vc, float age, float bodyYawDeg
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.04D, 0.0D), flatX, flatZ,
                0.48F, 0.76F, 24, 0.761F, 0.698F, 0.561F, 0.92F);
        renderLocalRing(m, vc, new Vec3(0.0D, 2.02D, 0.0D), flatX, flatZ,
                0.24F, 0.38F, 24, 1.000F, 0.918F, 0.471F, 0.98F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));
        Vec3 fwdDir = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
        double bob = 0.026D * Mth.sin(age * 0.28F);

        for (int side = -1; side <= 1; side += 2) {
            Vec3 shoulder = new Vec3(0.0D, 1.44D + bob, 0.0D).add(rightDir.scale(side * 0.42D));
            renderOctahedron(m, vc, shoulder, 0.16F, 0.14F, yawRad, 0.761F, 0.698F, 0.561F, 0.96F);
            Vec3 bladeFin = shoulder.add(rightDir.scale(side * 0.10D)).add(0.0D, 0.22D, -0.08D);
            renderOctahedron(m, vc, bladeFin, 0.085F, 0.24F, yawRad, 1.000F, 0.918F, 0.471F, 0.98F);
        }

        // 2 flickering 3D phantom afterimage silhouettes trailing behind the Mirage-Duelist!
        for (int img = 1; img <= 2; img++) {
            float flicker = 0.52F + 0.20F * Mth.sin(age * 0.42F + img * 1.9F);
            double sway = (img == 1 ? 0.16D : -0.18D) * Mth.sin(age * 0.26F + img);
            Vec3 ghostBase = fwdDir.scale(-0.52D * img).add(rightDir.scale(sway));
            Vec3 gTorso = ghostBase.add(0.0D, 1.08D + bob, 0.0D);
            Vec3 gHead = ghostBase.add(0.0D, 1.66D + bob, 0.0D);
            Vec3 tang = rightDir.scale(0.22D);
            Vec3 up = new Vec3(0.0D, 0.42D, 0.0D);
            boolean gold = (img == 1);
            float cr = gold ? 1.000F : 0.761F;
            float cg = gold ? 0.918F : 0.698F;
            float cb = gold ? 0.471F : 0.561F;
            emitDoubleSidedQuad(m, vc, gTorso.subtract(tang).add(up), gTorso.add(tang).add(up),
                    gTorso.add(tang).subtract(up), gTorso.subtract(tang).subtract(up),
                    cr, cg, cb, flicker * (img == 1 ? 0.68F : 0.46F));
            renderOctahedron(m, vc, gHead, 0.15F, 0.16F, yawRad, cr, cg, cb, flicker * (img == 1 ? 0.74F : 0.52F));
        }

        Vec3 chest = new Vec3(0.0D, 1.22D + bob, 0.0D).add(fwdDir.scale(0.18D));
        renderOctahedron(m, vc, chest, 0.11F, 0.15F, age * 0.30F, 1.000F, 0.918F, 0.471F, 0.99F);
        renderOctahedron(m, vc, chest, 0.15F, 0.18F, -age * 0.22F, 0.761F, 0.698F, 0.561F, 0.84F);
    }

    /**
     * Section 6.38 Wraith-Stalker Agent (#41304E / #4D6453):
     * Floating abyssal indigo void ring, hooded necrotic-moss ectoplasm cowl, jagged phase pauldrons & trailing wraith shroud!
     */
    private static void renderWraithStalkerAgentOrnaments(
            Matrix4f m,
            VertexConsumer vc,
            float age,
            float bodyYawDeg
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        double hover = 0.06D * Mth.sin(age * 0.20F);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.05D, 0.0D), flatX, flatZ,
                0.46F, 0.78F, 20, 0.255F, 0.188F, 0.306F, 0.94F);
        renderLocalRing(m, vc, new Vec3(0.0D, 1.98D + hover, 0.0D), flatX, flatZ,
                0.22F, 0.36F, 16, 0.302F, 0.392F, 0.325F, 0.92F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));
        Vec3 fwdDir = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));

        for (int side = -1; side <= 1; side += 2) {
            Vec3 shoulder = new Vec3(0.0D, 1.44D + hover, 0.0D).add(rightDir.scale(side * 0.44D));
            renderOctahedron(m, vc, shoulder, 0.17F, 0.15F, yawRad, 0.255F, 0.188F, 0.306F, 0.96F);
            Vec3 spike = shoulder.add(rightDir.scale(side * 0.12D)).add(0.0D, 0.22D, -0.08D);
            renderOctahedron(m, vc, spike, 0.09F, 0.24F, yawRad, 0.302F, 0.392F, 0.325F, 0.94F);
        }

        // 3 tattered streaming void-ectoplasm shroud tails behind the Wraith-Stalker!
        for (int t = -1; t <= 1; t++) {
            double sway = 0.10D * Mth.sin(age * 0.26F + t * 1.4F);
            Vec3 top = new Vec3(0.0D, 1.32D + hover, 0.0D)
                    .add(fwdDir.scale(-0.22D))
                    .add(rightDir.scale(t * 0.20D));
            Vec3 bot = new Vec3(0.0D, 0.18D + hover, 0.0D)
                    .add(fwdDir.scale(-0.56D))
                    .add(rightDir.scale(t * 0.28D + sway));
            Vec3 tang = rightDir.scale(0.09D);
            boolean moss = (t == 0);
            emitDoubleSidedQuad(m, vc, top.subtract(tang), top.add(tang), bot.add(tang), bot.subtract(tang),
                    moss ? 0.302F : 0.255F,
                    moss ? 0.392F : 0.188F,
                    moss ? 0.325F : 0.306F,
                    0.78F);
        }

        Vec3 chest = new Vec3(0.0D, 1.22D + hover, 0.0D).add(fwdDir.scale(0.18D));
        renderOctahedron(m, vc, chest, 0.12F, 0.16F, age * 0.28F, 0.302F, 0.392F, 0.325F, 0.96F);
        renderOctahedron(m, vc, chest, 0.16F, 0.19F, -age * 0.22F, 0.255F, 0.188F, 0.306F, 0.88F);
    }

    /**
     * Section 6.39 Barrow Skeleton Agent (#773214 / #4D6453 + #E6DFCF petrified bone):
     * Heavy petrified mossy tower shield on off-arm, spiked stone-flail head on main-arm, & ossuary rib plates!
     */
    private static void renderBarrowSkeletonAgentOrnaments(
            Matrix4f m,
            VertexConsumer vc,
            float age,
            float bodyYawDeg
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.04D, 0.0D), flatX, flatZ,
                0.52F, 0.84F, 12, 0.467F, 0.196F, 0.078F, 0.96F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));
        Vec3 fwdDir = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));

        // Mossy granite pauldrons & petrified bone spikes
        for (int side = -1; side <= 1; side += 2) {
            Vec3 shoulder = new Vec3(0.0D, 1.44D, 0.0D).add(rightDir.scale(side * 0.46D));
            renderOctahedron(m, vc, shoulder, 0.19F, 0.16F, yawRad, 0.467F, 0.196F, 0.078F, 0.98F);
            Vec3 mossCap = shoulder.add(0.0D, 0.16D, 0.0D);
            renderOctahedron(m, vc, mossCap, 0.12F, 0.14F, yawRad, 0.302F, 0.392F, 0.325F, 0.95F);
        }

        // Heavy petrified tower shield on off-hand side (-rightDir)
        Vec3 shieldCenter = new Vec3(0.0D, 1.02D, 0.0D).add(rightDir.scale(-0.52D)).add(fwdDir.scale(0.24D));
        Vec3 sTang = rightDir.scale(0.26D);
        Vec3 sUp = new Vec3(0.0D, 0.50D, 0.0D);
        emitDoubleSidedQuad(m, vc, shieldCenter.subtract(sTang).add(sUp), shieldCenter.add(sTang).add(sUp),
                shieldCenter.add(sTang).subtract(sUp), shieldCenter.subtract(sTang).subtract(sUp),
                0.467F, 0.196F, 0.078F, 0.98F);
        Vec3 boss = shieldCenter.add(fwdDir.scale(0.05D));
        renderOctahedron(m, vc, boss, 0.14F, 0.22F, yawRad, 0.302F, 0.392F, 0.325F, 0.97F);

        // Swinging spiked stone-flail head on main-hand side (+rightDir)
        double flailSwing = 0.14D * Mth.sin(age * 0.28F);
        Vec3 flailHead = new Vec3(0.0D, 0.78D + flailSwing, 0.0D).add(rightDir.scale(0.56D)).add(fwdDir.scale(0.32D));
        renderOctahedron(m, vc, flailHead, 0.18F, 0.18F, age * 0.32F, 0.467F, 0.196F, 0.078F, 0.99F);
        renderOctahedron(m, vc, flailHead, 0.22F, 0.12F, -age * 0.24F, 0.902F, 0.875F, 0.812F, 0.96F);
    }

    /**
     * Section 6.40 Pyre-Juggernaut Agent (#773214 / #FF4500):
     * Burning petrified cedar-log pauldrons, molten coal furnace chest core, & blazing timber-pyre cleaver!
     */
    private static void renderPyreJuggernautAgentOrnaments(
            Matrix4f m,
            VertexConsumer vc,
            float age,
            float bodyYawDeg
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.04D, 0.0D), flatX, flatZ,
                0.54F, 0.88F, 14, 1.000F, 0.271F, 0.000F, 0.94F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));
        Vec3 fwdDir = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));

        // Burning petrified cedar-log pauldrons (#773214) crowned with roaring #FF4500 wildfire flames
        for (int side = -1; side <= 1; side += 2) {
            Vec3 shoulder = new Vec3(0.0D, 1.46D, 0.0D).add(rightDir.scale(side * 0.48D));
            renderOctahedron(m, vc, shoulder, 0.21F, 0.18F, yawRad, 0.467F, 0.196F, 0.078F, 0.98F);
            Vec3 flameCrown = shoulder.add(0.0D, 0.20D + 0.03D * Mth.sin(age * 0.34F + side), 0.0D);
            renderOctahedron(m, vc, flameCrown, 0.13F, 0.19F, age * 0.22F, 1.000F, 0.271F, 0.000F, 0.96F);
        }

        // Molten coal furnace chest core
        Vec3 hearthCore = new Vec3(0.0D, 1.18D, 0.0D).add(fwdDir.scale(0.18D));
        renderOctahedron(m, vc, hearthCore, 0.16F, 0.20F, age * 0.25F, 1.000F, 0.271F, 0.000F, 0.98F);
        renderOctahedron(m, vc, hearthCore, 0.10F, 0.13F, -age * 0.35F, 1.000F, 0.820F, 0.361F, 0.99F);

        // Blazing Timber-Pyre Cleaver on main-hand side (+rightDir)
        double cleaveBob = 0.08D * Mth.sin(age * 0.26F);
        Vec3 bladeBase = new Vec3(0.0D, 0.88D + cleaveBob, 0.0D).add(rightDir.scale(0.54D)).add(fwdDir.scale(0.24D));
        Vec3 bladeTip = bladeBase.add(fwdDir.scale(0.68D)).add(0.0D, 0.26D, 0.0D);
        Vec3 bladeSpine = bladeBase.add(fwdDir.scale(0.32D)).add(0.0D, 0.32D, 0.0D);
        Vec3 bladeEdge = bladeBase.add(fwdDir.scale(0.42D)).subtract(0.0D, 0.12D, 0.0D);
        emitDoubleSidedQuad(m, vc, bladeBase, bladeSpine, bladeTip, bladeEdge, 0.467F, 0.196F, 0.078F, 0.98F);
        emitDoubleSidedQuad(m, vc, bladeEdge, bladeTip, bladeTip.add(0.0D, -0.06D, 0.0D), bladeBase, 1.000F, 0.271F, 0.000F, 0.96F);
    }

    /**
     * Section 6.41 Penumbral Void-Bladewarden Agent (#FFEA78 / #281E3C):
     * Sleek twin-scythe warrior with coronal halo, #FFEA78 radiant corona scythe & #281E3C twilight shadow scythe!
     */
    private static void renderPenumbraBladewardenAgentOrnaments(
            Matrix4f m,
            VertexConsumer vc,
            float age,
            float bodyYawDeg
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.04D, 0.0D), flatX, flatZ,
                0.52F, 0.86F, 20, 0.157F, 0.118F, 0.235F, 0.95F);
        renderLocalRing(m, vc, new Vec3(0.0D, 2.02D, 0.0D), flatX, flatZ,
                0.24F, 0.36F, 20, 1.000F, 0.918F, 0.471F, 0.96F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));
        Vec3 fwdDir = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));

        // Twin scythes: right arm blazing #FFEA78 corona scythe, left arm #281E3C twilight indigo scythe
        for (int side = -1; side <= 1; side += 2) {
            boolean corona = (side > 0);
            Vec3 shoulder = new Vec3(0.0D, 1.46D, 0.0D).add(rightDir.scale(side * 0.46D));
            renderOctahedron(m, vc, shoulder, 0.16F, 0.16F, yawRad,
                    corona ? 1.000F : 0.157F,
                    corona ? 0.918F : 0.118F,
                    corona ? 0.471F : 0.235F,
                    0.97F);

            Vec3 arcCenter = new Vec3(0.0D, 0.98D + 0.06D * Mth.sin(age * 0.32F + side), 0.0D)
                    .add(rightDir.scale(side * 0.56D))
                    .add(fwdDir.scale(0.32D));
            renderSweptArcBlade(m, vc, arcCenter, 0.48F, yawRad - 0.85F, 1.70F, 0.11F,
                    corona ? 1.000F : 0.157F,
                    corona ? 0.918F : 0.118F,
                    corona ? 0.471F : 0.235F,
                    0.96F);
        }
    }

    /**
     * Section 6.42 Glacial Rime-Vanguard Agent (#6395B1 / #DCF0FF):
     * Heavy armored frost-knight with crystalline hoarfrost pauldrons, permafrost chest core & rime-greatsword!
     */
    private static void renderGlacialRimeVanguardAgentOrnaments(
            Matrix4f m,
            VertexConsumer vc,
            float age,
            float bodyYawDeg
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.04D, 0.0D), flatX, flatZ,
                0.54F, 0.90F, 6, 0.388F, 0.584F, 0.694F, 0.95F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));
        Vec3 fwdDir = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));

        // Crystalline hoarfrost pauldrons (#6395B1 + #DCF0FF spires)
        for (int side = -1; side <= 1; side += 2) {
            Vec3 shoulder = new Vec3(0.0D, 1.46D, 0.0D).add(rightDir.scale(side * 0.48D));
            renderOctahedron(m, vc, shoulder, 0.20F, 0.18F, yawRad, 0.388F, 0.584F, 0.694F, 0.98F);
            Vec3 frostSpire = shoulder.add(0.0D, 0.22D, 0.0D);
            renderOctahedron(m, vc, frostSpire, 0.11F, 0.22F, yawRad, 0.863F, 0.941F, 1.000F, 0.99F);
        }

        // Permafrost chest core
        Vec3 chest = new Vec3(0.0D, 1.18D, 0.0D).add(fwdDir.scale(0.18D));
        renderOctahedron(m, vc, chest, 0.15F, 0.20F, age * 0.20F, 0.863F, 0.941F, 1.000F, 0.98F);

        // Crystalline Rime-Greatsword on main-hand side (+rightDir)
        double swordBob = 0.06D * Mth.sin(age * 0.24F);
        Vec3 hilt = new Vec3(0.0D, 0.86D + swordBob, 0.0D).add(rightDir.scale(0.54D)).add(fwdDir.scale(0.22D));
        Vec3 tip = hilt.add(fwdDir.scale(0.76D)).add(0.0D, 0.34D, 0.0D);
        Vec3 edgeL = hilt.add(fwdDir.scale(0.36D)).add(0.0D, 0.28D, 0.0D);
        Vec3 edgeR = hilt.add(fwdDir.scale(0.42D)).subtract(0.0D, 0.04D, 0.0D);
        emitDoubleSidedQuad(m, vc, hilt, edgeL, tip, edgeR, 0.863F, 0.941F, 1.000F, 0.97F);
        emitDoubleSidedQuad(m, vc, hilt, edgeR, tip, hilt.add(0.0D, 0.08D, 0.0D), 0.388F, 0.584F, 0.694F, 0.94F);
    }

    /**
     * Section 6.43 Vernal Bloom-Bladewarden Agent (#FF6B9D / #64C8FF):
     * Graceful warrior armored in living water-lily plates wielding a rapier of compressed springwater!
     */
    private static void renderVernalBloomBladewardenAgentOrnaments(
            Matrix4f m,
            VertexConsumer vc,
            float age,
            float bodyYawDeg
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.04D, 0.0D), flatX, flatZ,
                0.54F, 0.90F, 20, 0.392F, 0.784F, 1.000F, 0.94F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));
        Vec3 fwdDir = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));

        // Living water-lily & lotus blossom pauldrons (#FF6B9D + #64C8FF)
        for (int side = -1; side <= 1; side += 2) {
            Vec3 shoulder = new Vec3(0.0D, 1.45D, 0.0D).add(rightDir.scale(side * 0.46D));
            renderOctahedron(m, vc, shoulder, 0.19F, 0.14F, yawRad, 0.392F, 0.784F, 1.000F, 0.96F);
            Vec3 blossom = shoulder.add(0.0D, 0.16D, 0.0D);
            renderOctahedron(m, vc, blossom, 0.13F, 0.16F, age * 0.18F, 1.000F, 0.420F, 0.616F, 0.98F);
        }

        // Blooming lotus heart chest core
        Vec3 chest = new Vec3(0.0D, 1.18D, 0.0D).add(fwdDir.scale(0.18D));
        renderOctahedron(m, vc, chest, 0.14F, 0.18F, age * 0.22F, 1.000F, 0.420F, 0.616F, 0.98F);

        // Compressed Springwater Rapier (#64C8FF blade + #FF6B9D blossom bell-guard) on main-hand side (+rightDir)
        double rapierBob = 0.05D * Mth.sin(age * 0.28F);
        Vec3 hilt = new Vec3(0.0D, 0.92D + rapierBob, 0.0D).add(rightDir.scale(0.52D)).add(fwdDir.scale(0.24D));
        renderOctahedron(m, vc, hilt, 0.12F, 0.10F, yawRad, 1.000F, 0.420F, 0.616F, 0.98F);
        Vec3 tip = hilt.add(fwdDir.scale(0.92D)).add(0.0D, 0.18D, 0.0D);
        Vec3 edgeU = hilt.add(fwdDir.scale(0.32D)).add(0.0D, 0.10D, 0.0D);
        Vec3 edgeD = hilt.add(fwdDir.scale(0.32D)).subtract(0.0D, 0.03D, 0.0D);
        emitDoubleSidedQuad(m, vc, hilt, edgeU, tip, edgeD, 0.392F, 0.784F, 1.000F, 0.97F);
    }

    /**
     * Section 6.44 Galvanic Arc-Vanguard (#FFE600 / #878DA2):
     * Galvanized slate conductor pauldrons, sparking dielectric chest core, and twin curved
     * high-voltage lightning-khopeshes (#FFE600 arc edge + #878DA2 galvanized slate spine)!
     */
    private static void renderGalvanicArcVanguardAgentOrnaments(
            Matrix4f m,
            VertexConsumer vc,
            float age,
            float bodyYawDeg
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.04D, 0.0D), flatX, flatZ,
                0.54F, 0.90F, 8, 1.000F, 0.902F, 0.000F, 0.95F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));
        Vec3 fwdDir = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));

        // Sparking Dielectric Tesla Chest Core (#FFE600)
        Vec3 chest = new Vec3(0.0D, 1.18D, 0.0D).add(fwdDir.scale(0.18D));
        renderOctahedron(m, vc, chest, 0.14F, 0.18F, age * 0.38F, 1.000F, 0.902F, 0.000F, 0.99F);

        // Galvanized Slate Pauldrons (#878DA2) & Twin Curved Lightning-Khopeshes (#FFE600 / #878DA2) on BOTH hands!
        for (int side = -1; side <= 1; side += 2) {
            double bob = 0.04D * Mth.sin(age * 0.30F + (side > 0 ? 0.0F : 3.14F));
            Vec3 shoulder = new Vec3(0.0D, 1.44D + bob, 0.0D).add(rightDir.scale(side * 0.38D));
            renderOctahedron(m, vc, shoulder, 0.17F, 0.15F, yawRad + side * 0.30F, 0.529F, 0.553F, 0.635F, 0.96F);
            renderOctahedron(m, vc, shoulder.add(0.0D, 0.14D, 0.0D), 0.08F, 0.18F, -age * 0.30F, 1.000F, 0.902F, 0.000F, 0.98F);

            Vec3 hilt = new Vec3(0.0D, 0.90D - bob, 0.0D).add(rightDir.scale(side * 0.50D)).add(fwdDir.scale(0.22D));
            renderOctahedron(m, vc, hilt, 0.10F, 0.10F, yawRad, 0.529F, 0.553F, 0.635F, 0.97F);
            Vec3 curveKnee = hilt.add(fwdDir.scale(0.38D)).add(0.0D, 0.18D, 0.0D);
            Vec3 hookTip = hilt.add(fwdDir.scale(0.84D)).add(0.0D, 0.04D, 0.0D);
            Vec3 spineMid = hilt.add(fwdDir.scale(0.42D)).subtract(0.0D, 0.05D, 0.0D);
            emitDoubleSidedQuad(m, vc, hilt, curveKnee, hookTip, spineMid, 1.000F, 0.902F, 0.000F, 0.98F);
            emitDoubleSidedQuad(m, vc, hilt, hilt.add(0.0D, 0.05D, 0.0D), hookTip, spineMid, 0.529F, 0.553F, 0.635F, 0.90F);
        }
    }

    /**
     * Section 6.45 Ironwood Juggernaut (#1A5923 / #773214 / #FFEA78):
     * Massive petrified ironwood bark pauldrons, emerald canopy crest, golden solar core,
     * and heavy two-handed Ironwood Sun-Maul!
     */
    private static void renderIronwoodJuggernautAgentOrnaments(
            Matrix4f m,
            VertexConsumer vc,
            float age,
            float bodyYawDeg
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.04D, 0.0D), flatX, flatZ,
                0.58F, 0.96F, 12, 0.102F, 0.349F, 0.137F, 0.95F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));
        Vec3 fwdDir = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));

        // Golden Solar Canopy Chest Core (#FFEA78)
        Vec3 chest = new Vec3(0.0D, 1.18D, 0.0D).add(fwdDir.scale(0.19D));
        renderOctahedron(m, vc, chest, 0.16F, 0.20F, age * 0.20F, 1.000F, 0.918F, 0.471F, 0.99F);

        // Massive Petrified Ironwood Pauldrons (#773214) & Emerald Briar Spikes (#1A5923)
        for (int side = -1; side <= 1; side += 2) {
            double bob = 0.03D * Mth.sin(age * 0.20F + (side > 0 ? 0.0F : 3.14F));
            Vec3 shoulder = new Vec3(0.0D, 1.46D + bob, 0.0D).add(rightDir.scale(side * 0.42D));
            renderOctahedron(m, vc, shoulder, 0.21F, 0.18F, yawRad + side * 0.25F, 0.467F, 0.196F, 0.078F, 0.98F);
            renderOctahedron(m, vc, shoulder.add(0.0D, 0.16D, 0.0D), 0.11F, 0.20F, -yawRad, 0.102F, 0.349F, 0.137F, 0.96F);
        }

        // Heavy Petrified Ironwood Sun-Maul (#773214 shaft + #FFEA78 / #1A5923 warhammer head) on main-hand side!
        double maulBob = 0.04D * Mth.sin(age * 0.22F);
        Vec3 grip = new Vec3(0.0D, 0.88D + maulBob, 0.0D).add(rightDir.scale(0.54D)).add(fwdDir.scale(0.20D));
        Vec3 head = grip.add(fwdDir.scale(0.76D)).add(0.0D, 0.22D, 0.0D);
        Vec3 shaftW = new Vec3(0.0D, 0.045D, 0.0D);
        emitDoubleSidedQuad(m, vc, grip.subtract(shaftW), grip.add(shaftW), head.add(shaftW), head.subtract(shaftW),
                0.467F, 0.196F, 0.078F, 0.98F);
        renderOctahedron(m, vc, head, 0.24F, 0.18F, yawRad, 0.467F, 0.196F, 0.078F, 0.98F);
        renderOctahedron(m, vc, head, 0.16F, 0.24F, yawRad + 0.78F, 1.000F, 0.918F, 0.471F, 0.99F);
    }

    /**
     * Section 6.46 Septic Bog-Stalker Agent (#1A5923 / #4D6453 / #225C7C):
     * Predatory swamp beast with bubbling bog-mud ankle wake, necrotic olive dorsal spines, toxic teal siphon sacs & hooked mire claws!
     */
    private static void renderSepticBogStalkerAgentOrnaments(
            Matrix4f m, VertexConsumer vc, float age, float bodyYawDeg
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.048D, 0.0D), flatX, flatZ,
                0.46F, 0.80F, 16, 0.133F, 0.361F, 0.486F, 0.90F);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.068D, 0.0D), flatX, flatZ,
                0.32F, 0.52F, 14, 0.102F, 0.349F, 0.137F, 0.88F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));
        Vec3 fwdDir = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
        double bob = 0.032D * Mth.sin(age * 0.24F);

        // Necrotic olive dorsal swamp fins (#4D6453) & virulent green/teal shoulder sacs (#1A5923 / #225C7C)
        for (int d = 0; d < 3; d++) {
            Vec3 spine = new Vec3(0.0D, 1.18D + d * 0.26D + bob, 0.0D).subtract(fwdDir.scale(0.24D));
            renderOctahedron(m, vc, spine, 0.08F, 0.18F, yawRad, 0.302F, 0.392F, 0.325F, 0.96F);
        }
        for (int side = -1; side <= 1; side += 2) {
            Vec3 shoulder = new Vec3(0.0D, 1.42D + bob, 0.0D).add(rightDir.scale(side * 0.40D));
            renderOctahedron(m, vc, shoulder, 0.17F, 0.15F, yawRad + side * 0.25F, 0.302F, 0.392F, 0.325F, 0.96F);
            renderOctahedron(m, vc, shoulder.add(0.0D, 0.12D, 0.0D), 0.10F, 0.17F, -yawRad, 0.133F, 0.361F, 0.486F, 0.97F);
            Vec3 claw = new Vec3(0.0D, 0.86D + bob, 0.0D).add(rightDir.scale(side * 0.46D)).add(fwdDir.scale(0.32D));
            renderOctahedron(m, vc, claw, 0.085F, 0.22F, yawRad + side * 0.30F, 0.102F, 0.349F, 0.137F, 0.98F);
        }
    }

    /**
     * Section 6.47 Solar-Hydro Templar Agent (#FF6B9D / #FFEA78 / #225C7C):
     * Swift crusader with radiant dawn halo, vitae lotus pauldrons, sacred oceanic wake & pressurized water-jet partisan!
     */
    private static void renderSolarHydroTemplarAgentOrnaments(
            Matrix4f m, VertexConsumer vc, float age, float bodyYawDeg
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.048D, 0.0D), flatX, flatZ,
                0.46F, 0.82F, 18, 0.133F, 0.361F, 0.486F, 0.92F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));
        Vec3 fwdDir = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
        Vec3 upDir = new Vec3(0.0D, 1.0D, 0.0D);
        double bob = 0.030D * Mth.sin(age * 0.22F);

        // Radiant dawn templar halo (#FFEA78) behind head & vitae lotus pauldrons (#FF6B9D / #225C7C)
        Vec3 halo = new Vec3(0.0D, 1.66D + bob, 0.0D).subtract(fwdDir.scale(0.22D));
        renderLocalRing(m, vc, halo, rightDir, upDir, 0.26F, 0.40F, 20, 1.000F, 0.918F, 0.471F, 0.98F);
        for (int side = -1; side <= 1; side += 2) {
            Vec3 shoulder = new Vec3(0.0D, 1.45D + bob, 0.0D).add(rightDir.scale(side * 0.40D));
            renderOctahedron(m, vc, shoulder, 0.18F, 0.15F, yawRad + side * 0.25F, 0.133F, 0.361F, 0.486F, 0.96F);
            renderOctahedron(m, vc, shoulder.add(0.0D, 0.13D, 0.0D), 0.10F, 0.18F, -yawRad, 1.000F, 0.420F, 0.616F, 0.98F);
        }

        // Pressurized Solar Water-Jet Partisan Lance (#225C7C shaft + #FFEA78 / #FF6B9D spearhead) on main-hand side!
        Vec3 grip = new Vec3(0.0D, 0.92D + bob, 0.0D).add(rightDir.scale(0.52D)).add(fwdDir.scale(0.18D));
        Vec3 tip = grip.add(fwdDir.scale(0.88D)).add(0.0D, 0.18D, 0.0D);
        Vec3 shaftW = new Vec3(0.0D, 0.038D, 0.0D);
        emitDoubleSidedQuad(m, vc, grip.subtract(shaftW), grip.add(shaftW), tip.add(shaftW), tip.subtract(shaftW),
                0.133F, 0.361F, 0.486F, 0.96F);
        renderOctahedron(m, vc, tip, 0.14F, 0.24F, yawRad, 1.000F, 0.918F, 0.471F, 0.99F);
    }

    /**
     * Section 6.48 Stygian Dread-Juggernaut Agent (#B83C08 / #211A21 / #4D6453):
     * Towering stygian obsidian & necrotic bone pauldrons, burning magma furnace chest core,
     * horned dread helm crest, and massive Stygian Dread-Cleaver greatsword!
     */
    private static void renderStygianDreadJuggernautAgentOrnaments(
            Matrix4f m, VertexConsumer vc, float age, float bodyYawDeg
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.048D, 0.0D), flatX, flatZ,
                0.48F, 0.86F, 16, 0.722F, 0.235F, 0.031F, 0.94F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));
        Vec3 fwdDir = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
        double bob = 0.026D * Mth.sin(age * 0.20F);

        // Blazing magma furnace core (#B83C08) framed in stygian obsidian (#211A21) at the chest
        Vec3 chestFurnace = new Vec3(0.0D, 1.24D + bob, 0.0D).add(fwdDir.scale(0.20D));
        renderOctahedron(m, vc, chestFurnace, 0.14F, 0.18F, yawRad, 0.129F, 0.102F, 0.129F, 0.96F);
        renderOctahedron(m, vc, chestFurnace, 0.095F, 0.13F, -age * 0.18F, 0.722F, 0.235F, 0.031F, 0.99F);

        // Twin heavy stygian obsidian pauldrons (#211A21) & necrotic grave-slate horns/spikes (#4D6453)
        for (int side = -1; side <= 1; side += 2) {
            Vec3 shoulder = new Vec3(0.0D, 1.46D + bob, 0.0D).add(rightDir.scale(side * 0.44D));
            renderOctahedron(m, vc, shoulder, 0.20F, 0.18F, yawRad + side * 0.28F, 0.129F, 0.102F, 0.129F, 0.98F);
            renderOctahedron(m, vc, shoulder.add(0.0D, 0.15D, 0.0D).add(rightDir.scale(side * 0.06D)),
                    0.11F, 0.22F, -yawRad, 0.302F, 0.392F, 0.325F, 0.97F);

            Vec3 horn = new Vec3(0.0D, 1.88D + bob, 0.0D).add(rightDir.scale(side * 0.18D)).add(fwdDir.scale(0.06D));
            renderOctahedron(m, vc, horn, 0.065F, 0.20F, yawRad + side * 0.34F, 0.302F, 0.392F, 0.325F, 0.98F);
        }

        // Massive Stygian Dread-Cleaver (#211A21 obsidian spine + #B83C08 blazing magma cutting edge) on main-hand side!
        Vec3 grip = new Vec3(0.0D, 0.88D + bob, 0.0D).add(rightDir.scale(0.54D)).add(fwdDir.scale(0.16D));
        Vec3 tip = grip.add(fwdDir.scale(0.92D)).add(0.0D, 0.22D, 0.0D);
        Vec3 bladeUp = new Vec3(0.0D, 0.095D, 0.0D);
        emitDoubleSidedQuad(m, vc, grip.subtract(bladeUp), grip.add(bladeUp), tip.add(bladeUp), tip.subtract(bladeUp),
                0.129F, 0.102F, 0.129F, 0.98F);
        Vec3 edgeUp = new Vec3(0.0D, 0.048D, 0.0D);
        emitDoubleSidedQuad(m, vc, grip.subtract(bladeUp), grip.subtract(edgeUp), tip.subtract(edgeUp), tip.subtract(bladeUp.scale(1.25D)),
                0.722F, 0.235F, 0.031F, 0.99F);
    }

    /**
     * Section 6.50 Firmament Archon-Colossus Agent (#A88654 / #773214 / #225C7C / #B83C08):
     * Celestial firmament halo behind head & shoulders, quad-elemental bronzed/azure pauldrons,
     * 4 orbiting primordial motes, and luminous Quintessential Firmament Greatblade!
     */
    private static void renderFirmamentArchonColossusAgentOrnaments(
            Matrix4f m, VertexConsumer vc, float age, float bodyYawDeg
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.048D, 0.0D), flatX, flatZ,
                0.50F, 0.90F, 22, 0.659F, 0.525F, 0.329F, 0.94F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));
        Vec3 fwdDir = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
        Vec3 upDir = new Vec3(0.0D, 1.0D, 0.0D);
        double bob = 0.032D * Mth.sin(age * 0.22F);

        // Luminous atmospheric firmament halo (#A88654 & #225C7C) behind head & shoulders
        Vec3 halo = new Vec3(0.0D, 1.68D + bob, 0.0D).subtract(fwdDir.scale(0.24D));
        renderLocalRing(m, vc, halo, rightDir, upDir, 0.30F, 0.46F, 24, 0.659F, 0.525F, 0.329F, 0.98F);
        renderLocalRing(m, vc, halo, rightDir, upDir, 0.20F, 0.28F, 20, 0.133F, 0.361F, 0.486F, 0.94F);

        // Quad-elemental bronzed & azure archon pauldrons (#773214 + #B83C08 / #225C7C)
        for (int side = -1; side <= 1; side += 2) {
            Vec3 shoulder = new Vec3(0.0D, 1.48D + bob, 0.0D).add(rightDir.scale(side * 0.45D));
            renderOctahedron(m, vc, shoulder, 0.20F, 0.17F, yawRad + side * 0.26F, 0.467F, 0.196F, 0.078F, 0.98F);
            renderOctahedron(m, vc, shoulder.add(0.0D, 0.15D, 0.0D), 0.12F, 0.21F, -yawRad,
                    side < 0 ? 0.133F : 0.722F,
                    side < 0 ? 0.361F : 0.235F,
                    side < 0 ? 0.486F : 0.031F,
                    0.99F);
        }

        // Luminous Quintessential Firmament Greatblade (#A88654 chalk-gold spine + #225C7C / #B83C08 plasma edges)
        Vec3 grip = new Vec3(0.0D, 0.90D + bob, 0.0D).add(rightDir.scale(0.55D)).add(fwdDir.scale(0.18D));
        Vec3 tip = grip.add(fwdDir.scale(0.98D)).add(0.0D, 0.24D, 0.0D);
        Vec3 bladeUp = new Vec3(0.0D, 0.088D, 0.0D);
        emitDoubleSidedQuad(m, vc, grip.subtract(bladeUp), grip.add(bladeUp), tip.add(bladeUp), tip.subtract(bladeUp),
                0.659F, 0.525F, 0.329F, 0.99F);
        renderOctahedron(m, vc, tip, 0.15F, 0.25F, yawRad, 0.133F, 0.361F, 0.486F, 0.99F);
    }

    /**
     * Section 6.51 Entropic Dread-Titan Agent (#41304E / #211A21 / #4D6453 / #8A0303):
     * Collapsed event-horizon back halo, heavy umbral/necrotic bone pauldrons, boiling sanguine heart core,
     * and colossal curved Entropic Singularity Scythe!
     */
    private static void renderEntropicDreadTitanAgentOrnaments(
            Matrix4f m, VertexConsumer vc, float age, float bodyYawDeg
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.048D, 0.0D), flatX, flatZ,
                0.50F, 0.90F, 20, 0.255F, 0.188F, 0.306F, 0.95F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));
        Vec3 fwdDir = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
        Vec3 upDir = new Vec3(0.0D, 1.0D, 0.0D);
        double bob = 0.028D * Mth.sin(age * 0.20F);

        // Collapsed event-horizon halo (#211A21 + #8A0303 sanguine rim) behind the Dread-Titan's head & shoulders
        Vec3 halo = new Vec3(0.0D, 1.68D + bob, 0.0D).subtract(fwdDir.scale(0.24D));
        renderLocalRing(m, vc, halo, rightDir, upDir, 0.0F, 0.28F, 18, 0.129F, 0.102F, 0.129F, 0.98F);
        renderLocalRing(m, vc, halo, rightDir, upDir, 0.28F, 0.44F, 22, 0.541F, 0.012F, 0.012F, 0.98F);

        // Heavy umbral-void pauldrons (#211A21 / #41304E) & necrotic ossuary bone spikes (#4D6453)
        for (int side = -1; side <= 1; side += 2) {
            Vec3 shoulder = new Vec3(0.0D, 1.48D + bob, 0.0D).add(rightDir.scale(side * 0.45D));
            renderOctahedron(m, vc, shoulder, 0.21F, 0.18F, yawRad + side * 0.28F, 0.255F, 0.188F, 0.306F, 0.98F);
            renderOctahedron(m, vc, shoulder.add(0.0D, 0.16D, 0.0D).add(rightDir.scale(side * 0.06D)),
                    0.11F, 0.23F, -yawRad, 0.302F, 0.392F, 0.325F, 0.97F);
        }

        // Colossal Entropic Singularity Scythe (#41304E shaft + sweeping #211A21 / #8A0303 scythe crescent)
        Vec3 grip = new Vec3(0.0D, 0.86D + bob, 0.0D).add(rightDir.scale(0.55D)).add(fwdDir.scale(0.16D));
        Vec3 shaftTop = grip.add(fwdDir.scale(0.86D)).add(0.0D, 0.44D, 0.0D);
        Vec3 shaftW = new Vec3(0.0D, 0.044D, 0.0D);
        emitDoubleSidedQuad(m, vc, grip.subtract(shaftW), grip.add(shaftW), shaftTop.add(shaftW), shaftTop.subtract(shaftW),
                0.255F, 0.188F, 0.306F, 0.98F);
        Vec3 scytheTip = shaftTop.subtract(rightDir.scale(0.62D)).subtract(0.0D, 0.28D, 0.0D);
        Vec3 bladeW = new Vec3(0.0D, 0.092D, 0.0D);
        emitDoubleSidedQuad(m, vc, shaftTop.subtract(bladeW), shaftTop.add(bladeW), scytheTip.add(bladeW.scale(0.4D)), scytheTip.subtract(bladeW),
                0.541F, 0.012F, 0.012F, 0.99F);
    }

    /**
     * Section 6.52 Celestial Seraph-Colossus Agent (#FFEA78 / #FF6B9D / #1A5923 / #A88654):
     * Six radiant starlight seraph wings, blazing white-hole crown halo, emerald/rose-prana pauldrons,
     * and sweeping Solar-Genesis Greatblade!
     */
    private static void renderCelestialSeraphColossusAgentOrnaments(
            Matrix4f m, VertexConsumer vc, float age, float bodyYawDeg
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.048D, 0.0D), flatX, flatZ,
                0.52F, 0.92F, 22, 1.000F, 0.918F, 0.471F, 0.96F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));
        Vec3 fwdDir = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
        Vec3 backDir = fwdDir.scale(-1.0D);
        Vec3 upDir = new Vec3(0.0D, 1.0D, 0.0D);
        double bob = 0.030D * Mth.sin(age * 0.22F);

        // Blazing white-hole starlight halo (#FFFFFF + #FF6B9D rose-prana rim) behind the Seraph-Colossus's head!
        Vec3 halo = new Vec3(0.0D, 1.72D + bob, 0.0D).add(backDir.scale(0.24D));
        renderLocalRing(m, vc, halo, rightDir, upDir, 0.0F, 0.26F, 20, 1.000F, 1.000F, 1.000F, 0.99F);
        renderLocalRing(m, vc, halo, rightDir, upDir, 0.26F, 0.44F, 22, 1.000F, 0.420F, 0.616F, 0.98F);

        // Six seraphic starlight & pale-gold wing-pinions + emerald-briar pauldrons!
        float flap = 0.18F * Mth.sin(age * 0.24F);
        for (int side = -1; side <= 1; side += 2) {
            Vec3 shoulder = new Vec3(0.0D, 1.48D + bob, 0.0D).add(rightDir.scale(side * 0.44D));
            renderOctahedron(m, vc, shoulder, 0.20F, 0.18F, yawRad + side * 0.28F, 0.102F, 0.349F, 0.137F, 0.97F);
            renderOctahedron(m, vc, shoulder.add(0.0D, 0.14D, 0.0D), 0.10F, 0.22F, -yawRad, 1.000F, 0.918F, 0.471F, 0.99F);

            Vec3 wingAxis = rightDir.scale(side * Mth.cos(flap)).add(backDir.scale(0.38D + 0.16D * Mth.sin(flap))).normalize();
            for (int tier = 0; tier < 3; tier++) {
                Vec3 wRoot = new Vec3(0.0D, 1.44D - tier * 0.18D + bob, 0.0D).add(backDir.scale(0.18D));
                Vec3 wTip = wRoot.add(wingAxis.scale(0.96D - tier * 0.14D)).add(0.0D, 0.44D - tier * 0.24D, 0.0D);
                Vec3 wW = new Vec3(0.0D, 0.068D, 0.0D);
                boolean gold = (tier != 1);
                emitDoubleSidedQuad(m, vc, wRoot.subtract(wW), wRoot.add(wW), wTip.add(wW.scale(0.35D)), wTip.subtract(wW.scale(0.35D)),
                        gold ? 1.000F : 1.000F,
                        gold ? 0.918F : 0.420F,
                        gold ? 0.471F : 0.616F,
                        0.95F);
            }
        }

        // Sweeping Solar-Genesis Greatblade (#FFEA78 starlight spine + #FF6B9D / #1A5923 tip crystal)
        Vec3 grip = new Vec3(0.0D, 0.90D + bob, 0.0D).add(rightDir.scale(0.55D)).add(fwdDir.scale(0.18D));
        Vec3 tip = grip.add(fwdDir.scale(1.02D)).add(0.0D, 0.26D, 0.0D);
        Vec3 bladeUp = new Vec3(0.0D, 0.090D, 0.0D);
        emitDoubleSidedQuad(m, vc, grip.subtract(bladeUp), grip.add(bladeUp), tip.add(bladeUp), tip.subtract(bladeUp),
                1.000F, 0.918F, 0.471F, 0.99F);
        renderOctahedron(m, vc, tip, 0.15F, 0.26F, yawRad, 1.000F, 0.420F, 0.616F, 0.99F);
    }

    /**
     * Section 6.53 Prismatic Valkyrie Agent (#FFFFFF / #72F6FF / Full HSB Spectrum):
     * Six faceted rainbow prism wings, diamond-starlight crown halo, crystalline prism pauldrons,
     * and blazing piercing Diamond Light-Javelin!
     */
    private static void renderPrismaticValkyrieAgentOrnaments(
            Matrix4f m, VertexConsumer vc, float age, float bodyYawDeg
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        int ringRgb = Mth.hsvToRgb((age * 0.028F) % 1.0F, 0.75F, 1.0F);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.048D, 0.0D), flatX, flatZ,
                0.54F, 0.96F, 24,
                ((ringRgb >> 16) & 0xFF) / 255.0F,
                ((ringRgb >> 8) & 0xFF) / 255.0F,
                (ringRgb & 0xFF) / 255.0F,
                0.96F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));
        Vec3 fwdDir = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
        Vec3 backDir = fwdDir.scale(-1.0D);
        Vec3 upDir = new Vec3(0.0D, 1.0D, 0.0D);
        double bob = 0.032D * Mth.sin(age * 0.24F);

        // Diamond-white starlight halo + spectrum refractive outer crown behind the Valkyrie's head
        Vec3 halo = new Vec3(0.0D, 1.74D + bob, 0.0D).add(backDir.scale(0.24D));
        renderLocalRing(m, vc, halo, rightDir, upDir, 0.0F, 0.24F, 20, 1.000F, 1.000F, 1.000F, 0.99F);
        renderLocalRing(m, vc, halo, rightDir, upDir, 0.24F, 0.44F, 24, 0.447F, 0.965F, 1.000F, 0.97F);

        // Six spectrum-cycling crystalline prism wings + faceted diamond pauldrons
        float flap = 0.20F * Mth.sin(age * 0.26F);
        for (int side = -1; side <= 1; side += 2) {
            Vec3 shoulder = new Vec3(0.0D, 1.48D + bob, 0.0D).add(rightDir.scale(side * 0.44D));
            renderOctahedron(m, vc, shoulder, 0.20F, 0.18F, yawRad + side * 0.28F, 0.447F, 0.965F, 1.000F, 0.97F);
            renderOctahedron(m, vc, shoulder.add(0.0D, 0.14D, 0.0D), 0.11F, 0.24F, -yawRad, 1.000F, 1.000F, 1.000F, 0.99F);

            Vec3 wingAxis = rightDir.scale(side * Mth.cos(flap)).add(backDir.scale(0.36D + 0.16D * Mth.sin(flap))).normalize();
            for (int tier = 0; tier < 3; tier++) {
                int wRgb = Mth.hsvToRgb(((tier * 0.22F + (side > 0 ? 0.0F : 0.11F)) + age * 0.032F) % 1.0F, 0.78F, 1.0F);
                float wr = ((wRgb >> 16) & 0xFF) / 255.0F;
                float wg = ((wRgb >> 8) & 0xFF) / 255.0F;
                float wb = (wRgb & 0xFF) / 255.0F;
                Vec3 wRoot = new Vec3(0.0D, 1.46D - tier * 0.18D + bob, 0.0D).add(backDir.scale(0.18D));
                Vec3 wTip = wRoot.add(wingAxis.scale(1.02D - tier * 0.14D)).add(0.0D, 0.48D - tier * 0.24D, 0.0D);
                Vec3 wW = new Vec3(0.0D, 0.072D, 0.0D);
                emitDoubleSidedQuad(m, vc, wRoot.subtract(wW), wRoot.add(wW), wTip.add(wW.scale(0.30D)), wTip.subtract(wW.scale(0.30D)),
                        wr, wg, wb, 0.96F);
            }
        }

        // Blazing Piercing Diamond Light-Javelin (#FFFFFF core shaft + spectrum prism spearhead)
        Vec3 grip = new Vec3(0.0D, 0.94D + bob, 0.0D).add(rightDir.scale(0.55D)).add(fwdDir.scale(0.14D));
        Vec3 butt = grip.subtract(fwdDir.scale(0.42D)).subtract(0.0D, 0.08D, 0.0D);
        Vec3 tip = grip.add(fwdDir.scale(1.16D)).add(0.0D, 0.22D, 0.0D);
        Vec3 shaftUp = new Vec3(0.0D, 0.052D, 0.0D);
        emitDoubleSidedQuad(m, vc, butt.subtract(shaftUp), butt.add(shaftUp), tip.add(shaftUp), tip.subtract(shaftUp),
                1.000F, 1.000F, 1.000F, 0.99F);
        renderOctahedron(m, vc, tip, 0.16F, 0.28F, yawRad + age * 0.20F, 0.447F, 0.965F, 1.000F, 0.99F);
    }

    /**
     * Section 6.54 Yggdrasil Arboreal Juggernaut Agent (#FFEA78 / #FF6B9D / #1A5923):
     * Colossal living golden titan barkskin pauldrons, emerald World-Tree canopy antler-boughs,
     * rose-vitae heart halo, and sweeping Siphoning World-Tree Root-Greatsword!
     */
    private static void renderYggdrasilArborealJuggernautAgentOrnaments(
            Matrix4f m, VertexConsumer vc, float age, float bodyYawDeg
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.050D, 0.0D), flatX, flatZ,
                0.58F, 1.04F, 24, 1.000F, 0.918F, 0.471F, 0.97F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));
        Vec3 fwdDir = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
        Vec3 backDir = fwdDir.scale(-1.0D);
        Vec3 upDir = new Vec3(0.0D, 1.0D, 0.0D);
        double bob = 0.030D * Mth.sin(age * 0.20F);

        // Radiant World-Tree canopy crown halo (#FF6B9D inner vitae bloom + #FFEA78 golden solar ring)
        Vec3 halo = new Vec3(0.0D, 1.78D + bob, 0.0D).add(backDir.scale(0.24D));
        renderLocalRing(m, vc, halo, rightDir, upDir, 0.0F, 0.26F, 20, 1.000F, 0.420F, 0.616F, 0.98F);
        renderLocalRing(m, vc, halo, rightDir, upDir, 0.26F, 0.48F, 24, 1.000F, 0.918F, 0.471F, 0.97F);

        // Living golden titan barkskin pauldrons + 6 overarching World-Tree emerald & gold root-boughs
        float sway = 0.14F * Mth.sin(age * 0.20F);
        for (int side = -1; side <= 1; side += 2) {
            Vec3 shoulder = new Vec3(0.0D, 1.50D + bob, 0.0D).add(rightDir.scale(side * 0.48D));
            renderOctahedron(m, vc, shoulder, 0.24F, 0.20F, yawRad + side * 0.25F, 0.102F, 0.349F, 0.137F, 0.98F);
            renderOctahedron(m, vc, shoulder.add(0.0D, 0.16D, 0.0D), 0.13F, 0.26F, -yawRad, 1.000F, 0.918F, 0.471F, 0.99F);

            Vec3 boughAxis = rightDir.scale(side * Mth.cos(sway)).add(backDir.scale(0.38D + 0.12D * Mth.sin(sway))).normalize();
            for (int tier = 0; tier < 3; tier++) {
                Vec3 bRoot = new Vec3(0.0D, 1.50D - tier * 0.18D + bob, 0.0D).add(backDir.scale(0.20D));
                Vec3 bTip = bRoot.add(boughAxis.scale(1.06D - tier * 0.12D)).add(0.0D, 0.54D - tier * 0.22D, 0.0D);
                Vec3 bW = new Vec3(0.0D, 0.078D, 0.0D);
                float br = tier == 0 ? 1.000F : (tier == 1 ? 0.102F : 1.000F);
                float bg = tier == 0 ? 0.918F : (tier == 1 ? 0.349F : 0.420F);
                float bb = tier == 0 ? 0.471F : (tier == 1 ? 0.137F : 0.616F);
                emitDoubleSidedQuad(m, vc, bRoot.subtract(bW), bRoot.add(bW), bTip.add(bW.scale(0.35D)), bTip.subtract(bW.scale(0.35D)),
                        br, bg, bb, 0.96F);
            }
        }

        // Sweeping Siphoning World-Tree Root-Greatsword (#FFEA78 heartwood blade + #FF6B9D / #1A5923 seed-thorn apex)
        Vec3 grip = new Vec3(0.0D, 0.92D + bob, 0.0D).add(rightDir.scale(0.58D)).add(fwdDir.scale(0.18D));
        Vec3 tip = grip.add(fwdDir.scale(1.12D)).add(0.0D, 0.28D, 0.0D);
        Vec3 bladeUp = new Vec3(0.0D, 0.102D, 0.0D);
        emitDoubleSidedQuad(m, vc, grip.subtract(bladeUp), grip.add(bladeUp), tip.add(bladeUp), tip.subtract(bladeUp),
                1.000F, 0.918F, 0.471F, 0.99F);
        renderOctahedron(m, vc, tip, 0.17F, 0.28F, yawRad, 1.000F, 0.420F, 0.616F, 0.99F);
    }

    /**
     * Section 6.55 Stygian Magma-Void Serpent (#41304E / #211A21 / #B83C08):
     * Colossal abyssal leviathan serpent coiling up from a volcanic void fissure, dorsal dark-matter spines, & gaping molten jaws!
     */
    private static void renderStygianMagmaVoidSerpentAgentOrnaments(
            Matrix4f m, VertexConsumer vc, float age, float bodyYawDeg
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.050D, 0.0D), flatX, flatZ,
                0.60F, 1.08F, 24, 0.722F, 0.235F, 0.031F, 0.97F);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.068D, 0.0D), flatX, flatZ,
                0.30F, 0.58F, 20, 0.255F, 0.188F, 0.306F, 0.94F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));
        Vec3 fwdDir = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));

        // Coiling 14-segment Stygian Magma-Void Leviathan Serpent spiraling upward with dorsal dark-matter spikes!
        int segs = 14;
        for (int s = 0; s < segs; s++) {
            float frac = s / (float) (segs - 1);
            float coilAng = yawRad + frac * 6.8F - age * 0.22F;
            double coilR = 0.68D - 0.14D * frac + 0.06D * Mth.sin(age * 0.25F + s);
            double cy = 0.18D + frac * 1.82D;
            Vec3 segPos = new Vec3(Mth.cos(coilAng) * coilR, cy, Mth.sin(coilAng) * coilR);
            boolean magmaSeg = (s % 2 == 0);
            renderOctahedron(m, vc, segPos, 0.19F * (1.05F - 0.25F * frac), 0.16F, coilAng,
                    magmaSeg ? 0.722F : 0.129F,
                    magmaSeg ? 0.235F : 0.102F,
                    magmaSeg ? 0.031F : 0.129F,
                    0.98F);
            // Dorsal Void Deep Indigo (#41304E) & Molten Magma spine ridge
            if (s % 2 == 1) {
                Vec3 spinePos = segPos.add(Mth.cos(coilAng) * 0.16D, 0.12D, Mth.sin(coilAng) * 0.16D);
                renderOctahedron(m, vc, spinePos, 0.085F, 0.22F, -coilAng, 0.255F, 0.188F, 0.306F, 0.96F);
            }
        }

        // Gaping molten leviathan serpent jaws & obsidian fangs lunging ahead of the crown!
        double jawSnap = 0.06D + 0.05D * Mth.abs(Mth.sin(age * 0.28F));
        Vec3 snout = new Vec3(0.0D, 1.76D, 0.0D).add(fwdDir.scale(0.46D));
        renderOctahedron(m, vc, snout.add(0.0D, jawSnap, 0.0D), 0.24F, 0.14F, yawRad, 0.129F, 0.102F, 0.129F, 0.99F);
        renderOctahedron(m, vc, snout.subtract(0.0D, jawSnap, 0.0D), 0.22F, 0.12F, yawRad, 0.722F, 0.235F, 0.031F, 0.99F);
        for (int side = -1; side <= 1; side += 2) {
            Vec3 fang = snout.add(rightDir.scale(side * 0.16D)).add(fwdDir.scale(0.14D));
            renderOctahedron(m, vc, fang, 0.055F, 0.16F, yawRad, 1.000F, 0.369F, 0.102F, 0.99F);
        }
    }

    /**
     * Section 6.56 Titanic Glacial Elemental (#A88654 / #98DCF2 / #225C7C):
     * Colossal frost-and-azure glacier pauldrons, howling golden-buff slipstream ankle vortex,
     * dorsal permafrost crystal crown & sweeping glacial slam blades!
     */
    private static void renderTitanicGlacialElementalAgentOrnaments(
            Matrix4f m, VertexConsumer vc, float age, float bodyYawDeg
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.050D, 0.0D), flatX, flatZ,
                0.60F, 1.08F, 24, 0.596F, 0.863F, 0.949F, 0.96F);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.068D, 0.0D), flatX, flatZ,
                0.32F, 0.58F, 20, 0.659F, 0.525F, 0.329F, 0.92F);
        renderOpenFrustumWall(m, vc, 0.04F, 0.72F, 0.56F, 0.34F, 12, age * 0.22F,
                0.133F, 0.361F, 0.486F, 0.54F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));
        Vec3 backDir = new Vec3(Mth.sin(yawRad), 0.0D, -Mth.cos(yawRad));

        // Towering frost-cyan & deep-azure glacier shoulder monoliths + dorsal permafrost spires
        for (int side = -1; side <= 1; side += 2) {
            Vec3 pauldron = new Vec3(0.0D, 1.46D, 0.0D).add(rightDir.scale(side * 0.52D));
            renderOctahedron(m, vc, pauldron, 0.24F, 0.42F, yawRad + side * 0.35F,
                    0.596F, 0.863F, 0.949F, 0.96F);
            renderOctahedron(m, vc, pauldron.add(0.0D, 0.14D, 0.0D), 0.16F, 0.34F, -yawRad,
                    0.133F, 0.361F, 0.486F, 0.98F);
        }
        for (int sp = 0; sp < 5; sp++) {
            double angOff = (sp - 2) * 0.38D;
            Vec3 spirePos = new Vec3(0.0D, 1.58D + (2 - Math.abs(sp - 2)) * 0.14D, 0.0D)
                    .add(backDir.scale(0.32D))
                    .add(rightDir.scale(angOff * 0.85D));
            boolean frost = (sp % 2 == 0);
            renderOctahedron(m, vc, spirePos, 0.11F, 0.36F, yawRad,
                    frost ? 0.596F : 0.659F,
                    frost ? 0.863F : 0.525F,
                    frost ? 0.949F : 0.329F,
                    0.96F);
        }

        // 4 sweeping golden-buff & frost-cyan cryo-slam arc blades orbiting the elemental's torso!
        for (int b = 0; b < 4; b++) {
            float bAng = age * 0.28F + (float) (Math.PI * 0.5D * b);
            boolean gold = (b % 2 == 0);
            renderSweptArcBlade(m, vc, new Vec3(0.0D, 1.12D + 0.08D * (b % 2), 0.0D), 0.86F, bAng, 1.10F, 0.11F,
                    gold ? 0.659F : 0.596F,
                    gold ? 0.525F : 0.863F,
                    gold ? 0.329F : 0.949F,
                    0.92F);
        }
    }

    /**
     * Section 6.57 Event-Horizon Leviathan (#55 Eschaton — #0B0813 / #7012A5 / #D4C2FC, 90s / 140 HP):
     * Colossal wall-phasing abyssal leviathan forged of collapsed void-black event horizons,
     * swirling violet accretion disks, and chrono-silver reality-cleaving scythe arcs!
     */
    private static void renderEventHorizonLeviathanAgentOrnaments(
            Matrix4f m, VertexConsumer vc, float age, float bodyYawDeg
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.050D, 0.0D), flatX, flatZ,
                0.66F, 1.20F, 26, 0.439F, 0.071F, 0.647F, 0.97F);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.070D, 0.0D), flatX, flatZ,
                0.34F, 0.64F, 22, 0.831F, 0.761F, 0.988F, 0.95F);
        renderOpenFrustumWall(m, vc, 0.04F, 0.78F, 0.62F, 0.36F, 12, -age * 0.26F,
                0.043F, 0.031F, 0.075F, 0.74F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));
        Vec3 backDir = new Vec3(Mth.sin(yawRad), 0.0D, -Mth.cos(yawRad));

        // Event-Horizon Void-Black (#0B0813) & Accretion Violet (#7012A5) shoulder pauldrons + Chrono-Silver (#D4C2FC) dorsal spires
        for (int side = -1; side <= 1; side += 2) {
            Vec3 pauldron = new Vec3(0.0D, 1.50D, 0.0D).add(rightDir.scale(side * 0.56D));
            renderOctahedron(m, vc, pauldron, 0.26F, 0.46F, yawRad + side * 0.38F,
                    0.043F, 0.031F, 0.075F, 0.99F);
            renderOctahedron(m, vc, pauldron.add(0.0D, 0.15D, 0.0D), 0.18F, 0.36F, -yawRad,
                    0.439F, 0.071F, 0.647F, 0.96F);
        }
        for (int sp = 0; sp < 5; sp++) {
            double angOff = (sp - 2) * 0.40D;
            Vec3 spirePos = new Vec3(0.0D, 1.62D + (2 - Math.abs(sp - 2)) * 0.16D, 0.0D)
                    .add(backDir.scale(0.34D))
                    .add(rightDir.scale(angOff * 0.88D));
            boolean silver = (sp % 2 == 0);
            renderOctahedron(m, vc, spirePos, 0.12F, 0.40F, yawRad,
                    silver ? 0.831F : 0.439F,
                    silver ? 0.761F : 0.071F,
                    silver ? 0.988F : 0.647F,
                    0.98F);
        }

        // 4 sweeping Violet & Chrono-Silver singularity cleave blades orbiting the leviathan's torso!
        for (int b = 0; b < 4; b++) {
            float bAng = -age * 0.34F + (float) (Math.PI * 0.5D * b);
            boolean silver = (b % 2 == 0);
            renderSweptArcBlade(m, vc, new Vec3(0.0D, 1.14D + 0.08D * (b % 2), 0.0D), 0.94F, bAng, 1.18F, 0.12F,
                    silver ? 0.831F : 0.439F,
                    silver ? 0.761F : 0.071F,
                    silver ? 0.988F : 0.647F,
                    0.95F);
        }
    }

    /**
     * Section 6.58 Celestial Seraph-Titan Agent (#FFFFFF / #FFE484 / #72F6FF):
     * Colossal wall-phasing 6-winged hard-light titan with blazing white-hole solar crown halo,
     * living astral-gold pauldrons, 6 articulated hard-light seraph wings, and sweeping divine cleave blades!
     */
    private static void renderCelestialSeraphTitanAgentOrnaments(
            Matrix4f m, VertexConsumer vc, float age, float bodyYawDeg
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.050D, 0.0D), flatX, flatZ,
                0.76F, 1.38F, 28, 1.000F, 0.894F, 0.518F, 0.96F);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.072D, 0.0D), flatX, flatZ,
                0.96F, 1.18F, 28, 0.447F, 0.965F, 1.000F, 0.94F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));
        Vec3 backDir = new Vec3(Mth.sin(yawRad), 0.0D, -Mth.cos(yawRad));
        Vec3 upDir = new Vec3(0.0D, 1.0D, 0.0D);

        // Blazing Supernova White (#FFFFFF) & Astral Gold (#FFE484) sovereign crown halo behind head!
        Vec3 haloCenter = new Vec3(0.0D, 1.88D, 0.0D).add(backDir.scale(0.24D));
        renderLocalRing(m, vc, haloCenter, rightDir, upDir, 0.28F, 0.48F, 24,
                1.000F, 0.894F, 0.518F, 0.98F);
        renderOctahedron(m, vc, new Vec3(0.0D, 2.08D, 0.0D), 0.14F, 0.26F, age * 0.24F,
                1.000F, 1.000F, 1.000F, 0.99F);

        // Living Gold (#FFE484) & Prismatic Cyan (#72F6FF) shoulder pauldrons
        for (int side = -1; side <= 1; side += 2) {
            Vec3 pauldron = new Vec3(0.0D, 1.50D, 0.0D).add(rightDir.scale(side * 0.54D));
            renderOctahedron(m, vc, pauldron, 0.24F, 0.42F, yawRad + side * 0.36F,
                    1.000F, 0.894F, 0.518F, 0.98F);
            renderOctahedron(m, vc, pauldron.add(0.0D, 0.14D, 0.0D), 0.16F, 0.32F, -yawRad,
                    0.447F, 0.965F, 1.000F, 0.95F);
        }

        // 6 colossal hard-light seraph wings (3 pairs: upper white, middle astral-gold, lower prismatic-cyan)!
        float[][] wingRgb = {
                {0.447F, 0.965F, 1.000F}, // Lower pair: #72F6FF Ascendant Prismatic Cyan
                {1.000F, 0.894F, 0.518F}, // Middle pair: #FFE484 Celestial Astral Gold
                {1.000F, 1.000F, 1.000F}  // Upper pair: #FFFFFF Supernova Brilliant White
        };
        for (int pair = 0; pair < 3; pair++) {
            float[] wc = wingRgb[pair];
            double baseH = 1.06D + pair * 0.32D;
            double spanOut = 1.02D + pair * 0.24D + 0.08D * Mth.sin(age * 0.22F + pair);
            double lift = 0.16D + pair * 0.18D + 0.06D * Mth.cos(age * 0.22F + pair);
            for (int side = -1; side <= 1; side += 2) {
                Vec3 root = new Vec3(0.0D, baseH, 0.0D).add(backDir.scale(0.24D)).add(rightDir.scale(side * 0.16D));
                Vec3 tip = root.add(rightDir.scale(side * spanOut)).add(backDir.scale(0.36D)).add(0.0D, lift, 0.0D);
                Vec3 featherLow = root.add(rightDir.scale(side * spanOut * 0.78D)).add(backDir.scale(0.30D)).subtract(0.0D, 0.18D, 0.0D);
                emitDoubleSidedQuad(m, vc, root, tip, featherLow, root, wc[0], wc[1], wc[2], 0.95F);
            }
        }

        // 4 sweeping divine golden-white cleave blades orbiting the Seraph-Titan!
        for (int b = 0; b < 4; b++) {
            float bAng = age * 0.34F + (float) (Math.PI * 0.5D * b);
            boolean white = (b % 2 == 0);
            renderSweptArcBlade(m, vc, new Vec3(0.0D, 1.14D + 0.08D * (b % 2), 0.0D), 0.94F, bAng, 1.18F, 0.12F,
                    1.000F,
                    white ? 1.000F : 0.894F,
                    white ? 1.000F : 0.518F,
                    0.96F);
        }
    }

    /**
     * Section 6.59 Avatar of Entropica Agent (#E0E5EC / #1A153B / #FAD02C):
     * Colossal wall-phasing Demiurge Sovereign crowned with a Chrono-Silver & Genesis-Gold equilibrium halo,
     * Event-Horizon Indigo parallax chest core, 6 balanced Void/Radiant pinions, and 4 alternating Void/Radiant orbs + dual-elemental cleave blades!
     */
    private static void renderAvatarOfEntropicaAgentOrnaments(
            Matrix4f m, VertexConsumer vc, float age, float bodyYawDeg
    ) {
        Vec3 flatX = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 flatZ = new Vec3(0.0D, 0.0D, 1.0D);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.050D, 0.0D), flatX, flatZ, 0.68F, 1.24F, 28,
                0.878F, 0.898F, 0.925F, 0.96F);
        renderLocalRing(m, vc, new Vec3(0.0D, 0.068D, 0.0D), flatX, flatZ, 0.90F, 1.10F, 28,
                0.980F, 0.816F, 0.173F, 0.94F);

        float yawRad = (180.0F - bodyYawDeg) * Mth.DEG_TO_RAD;
        Vec3 fwdDir = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
        Vec3 backDir = fwdDir.scale(-1.0D);
        Vec3 rightDir = new Vec3(Mth.cos(yawRad), 0.0D, Mth.sin(yawRad));

        // Event-Horizon Indigo (#1A153B) chest core + Genesis Gold (#FAD02C) & Chrono-Silver (#E0E5EC) halo crown
        Vec3 chestCore = new Vec3(0.0D, 1.26D, 0.0D).add(fwdDir.scale(0.24D));
        renderOctahedron(m, vc, chestCore, 0.19F, 0.28F, age * 0.34F, 0.102F, 0.082F, 0.231F, 0.99F);
        renderOctahedron(m, vc, chestCore, 0.25F, 0.18F, -age * 0.28F, 0.980F, 0.816F, 0.173F, 0.92F);
        renderLocalRing(m, vc, new Vec3(0.0D, 2.08D, 0.0D), flatX, flatZ, 0.38F, 0.62F, 24,
                0.980F, 0.816F, 0.173F, 0.98F);
        renderOctahedron(m, vc, new Vec3(0.0D, 2.12D, 0.0D), 0.15F, 0.28F, age * 0.26F,
                0.878F, 0.898F, 0.925F, 0.99F);

        // Chrono-Silver & Event-Horizon Indigo shoulder pauldrons
        for (int side = -1; side <= 1; side += 2) {
            Vec3 pauldron = new Vec3(0.0D, 1.50D, 0.0D).add(rightDir.scale(side * 0.56D));
            renderOctahedron(m, vc, pauldron, 0.25F, 0.44F, yawRad + side * 0.36F,
                    0.878F, 0.898F, 0.925F, 0.98F);
            renderOctahedron(m, vc, pauldron.add(0.0D, 0.14D, 0.0D), 0.17F, 0.32F, -yawRad,
                    0.102F, 0.082F, 0.231F, 0.96F);
        }

        // 6 Demiurge Equilibrium Pinions (left side Event-Horizon Indigo/Silver, right side Genesis Gold/Silver)!
        for (int pair = 0; pair < 3; pair++) {
            double baseH = 1.06D + pair * 0.32D;
            double spanOut = 1.06D + pair * 0.24D + 0.08D * Mth.sin(age * 0.22F + pair);
            double lift = 0.18D + pair * 0.18D + 0.06D * Mth.cos(age * 0.22F + pair);
            for (int side = -1; side <= 1; side += 2) {
                Vec3 root = new Vec3(0.0D, baseH, 0.0D).add(backDir.scale(0.24D)).add(rightDir.scale(side * 0.16D));
                Vec3 tip = root.add(rightDir.scale(side * spanOut)).add(backDir.scale(0.36D)).add(0.0D, lift, 0.0D);
                Vec3 featherLow = root.add(rightDir.scale(side * spanOut * 0.78D)).add(backDir.scale(0.30D)).subtract(0.0D, 0.18D, 0.0D);
                float wr = (pair == 1) ? 0.878F : (side > 0 ? 0.980F : 0.102F);
                float wg = (pair == 1) ? 0.898F : (side > 0 ? 0.816F : 0.082F);
                float wb = (pair == 1) ? 0.925F : (side > 0 ? 0.173F : 0.231F);
                emitDoubleSidedQuad(m, vc, root, tip, featherLow, root, wr, wg, wb, 0.95F);
            }
        }

        // 4 orbiting alternating Void/Radiant orbs + dual-elemental sweeping cleave blades!
        for (int b = 0; b < 4; b++) {
            float bAng = age * 0.36F + (float) (Math.PI * 0.5D * b);
            boolean gold = (b % 2 == 0);
            renderSweptArcBlade(m, vc, new Vec3(0.0D, 1.14D + 0.08D * (b % 2), 0.0D), 0.98F, bAng, 1.20F, 0.12F,
                    gold ? 0.980F : 0.878F,
                    gold ? 0.816F : 0.898F,
                    gold ? 0.173F : 0.925F,
                    0.96F);
            Vec3 orbPos = new Vec3(Mth.cos(-bAng) * 1.08D, 1.42D + 0.12D * Mth.sin(age * 0.28F + b), Mth.sin(-bAng) * 1.08D);
            renderOctahedron(m, vc, orbPos, 0.12F, 0.18F, bAng,
                    gold ? 0.980F : 0.102F,
                    gold ? 0.816F : 0.082F,
                    gold ? 0.173F : 0.231F,
                    0.98F);
        }
    }

    private static void renderOpenFrustumWall(
            Matrix4f mat, VertexConsumer vc,
            float y0, float y1, float r0, float r1,
            int sides, float rotOffset,
            float r, float g, float b, float a
    ) {
        for (int i = 0; i < sides; i++) {
            float a0 = rotOffset + (float) (Math.PI * 2.0D * i / sides);
            float a1 = rotOffset + (float) (Math.PI * 2.0D * (i + 1) / sides);
            Vec3 b0 = new Vec3(Mth.cos(a0) * r0, y0, Mth.sin(a0) * r0);
            Vec3 b1 = new Vec3(Mth.cos(a1) * r0, y0, Mth.sin(a1) * r0);
            Vec3 t1 = new Vec3(Mth.cos(a1) * r1, y1, Mth.sin(a1) * r1);
            Vec3 t0 = new Vec3(Mth.cos(a0) * r1, y1, Mth.sin(a0) * r1);
            emitDoubleSidedQuad(mat, vc, b0, b1, t1, t0, r, g, b, a);
        }
    }

    private static void renderSweptArcBlade(
            Matrix4f mat, VertexConsumer vc,
            Vec3 center, float radius, float startAng, float arcSpan, float maxThick,
            float r, float g, float b, float a
    ) {
        int segs = 10;
        for (int i = 0; i < segs; i++) {
            float t0 = i / (float) segs;
            float t1 = (i + 1) / (float) segs;
            float a0 = startAng + t0 * arcSpan;
            float a1 = startAng + t1 * arcSpan;
            float w0 = maxThick * Mth.sin(t0 * (float) Math.PI);
            float w1 = maxThick * Mth.sin(t1 * (float) Math.PI);
            Vec3 in0 = center.add(Mth.cos(a0) * (radius - w0), 0.0D, Mth.sin(a0) * (radius - w0));
            Vec3 out0 = center.add(Mth.cos(a0) * (radius + w0 * 0.35F), 0.0D, Mth.sin(a0) * (radius + w0 * 0.35F));
            Vec3 out1 = center.add(Mth.cos(a1) * (radius + w1 * 0.35F), 0.0D, Mth.sin(a1) * (radius + w1 * 0.35F));
            Vec3 in1 = center.add(Mth.cos(a1) * (radius - w1), 0.0D, Mth.sin(a1) * (radius - w1));
            emitDoubleSidedQuad(mat, vc, in0, out0, out1, in1, r, g, b, a);
        }
    }

    private static void renderOctahedron(
            Matrix4f mat, VertexConsumer vc, Vec3 center,
            float radius, float halfHeight, float rotY,
            float r, float g, float b, float a
    ) {
        Vec3 top = center.add(0.0D, halfHeight, 0.0D);
        Vec3 bot = center.subtract(0.0D, halfHeight, 0.0D);
        int sides = 6;
        for (int i = 0; i < sides; i++) {
            float a0 = rotY + (float) (Math.PI * 2.0D * i / sides);
            float a1 = rotY + (float) (Math.PI * 2.0D * (i + 1) / sides);
            Vec3 w0 = center.add(Mth.cos(a0) * radius, 0.0D, Mth.sin(a0) * radius);
            Vec3 w1 = center.add(Mth.cos(a1) * radius, 0.0D, Mth.sin(a1) * radius);
            emitDoubleSidedQuad(mat, vc, top, w0, w1, top, r, g, b, a);
            emitDoubleSidedQuad(mat, vc, bot, w1, w0, bot, r * 0.88F, g * 0.88F, b * 0.88F, a);
        }
    }

    private static void renderCylinderFrustum(
            Matrix4f mat, VertexConsumer vc,
            float y0, float y1, float r0, float r1,
            int sides, float rotOffset,
            float r, float g, float b, float a
    ) {
        for (int i = 0; i < sides; i++) {
            float a0 = rotOffset + (float) (Math.PI * 2.0D * i / sides);
            float a1 = rotOffset + (float) (Math.PI * 2.0D * (i + 1) / sides);
            Vec3 b0 = new Vec3(Mth.cos(a0) * r0, y0, Mth.sin(a0) * r0);
            Vec3 b1 = new Vec3(Mth.cos(a1) * r0, y0, Mth.sin(a1) * r0);
            Vec3 t1 = new Vec3(Mth.cos(a1) * r1, y1, Mth.sin(a1) * r1);
            Vec3 t0 = new Vec3(Mth.cos(a0) * r1, y1, Mth.sin(a0) * r1);
            emitDoubleSidedQuad(mat, vc, b0, b1, t1, t0, r, g, b, a);
        }
        // Top cap
        Vec3 topCenter = new Vec3(0.0D, y1, 0.0D);
        for (int i = 0; i < sides; i++) {
            float a0 = rotOffset + (float) (Math.PI * 2.0D * i / sides);
            float a1 = rotOffset + (float) (Math.PI * 2.0D * (i + 1) / sides);
            Vec3 t0 = new Vec3(Mth.cos(a0) * r1, y1, Mth.sin(a0) * r1);
            Vec3 t1 = new Vec3(Mth.cos(a1) * r1, y1, Mth.sin(a1) * r1);
            emitDoubleSidedQuad(mat, vc, topCenter, t0, t1, topCenter, r * 1.1F, g * 1.1F, b * 1.1F, a);
        }
    }

    private static void renderLocalRing(
            Matrix4f mat, VertexConsumer vc,
            Vec3 center, Vec3 u, Vec3 v,
            float rIn, float rOut, int segs,
            float r, float g, float b, float a
    ) {
        for (int i = 0; i < segs; i++) {
            float a0 = (float) (Math.PI * 2.0D * i / segs);
            float a1 = (float) (Math.PI * 2.0D * (i + 1) / segs);
            Vec3 d0 = u.scale(Mth.cos(a0)).add(v.scale(Mth.sin(a0)));
            Vec3 d1 = u.scale(Mth.cos(a1)).add(v.scale(Mth.sin(a1)));
            emitDoubleSidedQuad(mat, vc,
                    center.add(d0.scale(rIn)), center.add(d0.scale(rOut)),
                    center.add(d1.scale(rOut)), center.add(d1.scale(rIn)),
                    r, g, b, a);
        }
    }

    private static void emitDoubleSidedQuad(
            Matrix4f mat, VertexConsumer vc,
            Vec3 v0, Vec3 v1, Vec3 v2, Vec3 v3,
            float r, float g, float b, float a
    ) {
        float cr = Mth.clamp(r, 0.0F, 1.0F);
        float cg = Mth.clamp(g, 0.0F, 1.0F);
        float cb = Mth.clamp(b, 0.0F, 1.0F);
        float ca = Mth.clamp(a, 0.0F, 1.0F);

        vc.addVertex(mat, (float) v0.x, (float) v0.y, (float) v0.z)
                .setColor(cr, cg, cb, ca).setUv(0.0F, 0.0F).setOverlay(NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0.0F, 1.0F, 0.0F);
        vc.addVertex(mat, (float) v1.x, (float) v1.y, (float) v1.z)
                .setColor(cr, cg, cb, ca).setUv(0.0F, 1.0F).setOverlay(NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0.0F, 1.0F, 0.0F);
        vc.addVertex(mat, (float) v2.x, (float) v2.y, (float) v2.z)
                .setColor(cr, cg, cb, ca).setUv(1.0F, 1.0F).setOverlay(NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0.0F, 1.0F, 0.0F);
        vc.addVertex(mat, (float) v3.x, (float) v3.y, (float) v3.z)
                .setColor(cr, cg, cb, ca).setUv(1.0F, 0.0F).setOverlay(NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0.0F, 1.0F, 0.0F);

        vc.addVertex(mat, (float) v3.x, (float) v3.y, (float) v3.z)
                .setColor(cr, cg, cb, ca).setUv(1.0F, 0.0F).setOverlay(NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0.0F, 1.0F, 0.0F);
        vc.addVertex(mat, (float) v2.x, (float) v2.y, (float) v2.z)
                .setColor(cr, cg, cb, ca).setUv(1.0F, 1.0F).setOverlay(NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0.0F, 1.0F, 0.0F);
        vc.addVertex(mat, (float) v1.x, (float) v1.y, (float) v1.z)
                .setColor(cr, cg, cb, ca).setUv(0.0F, 1.0F).setOverlay(NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0.0F, 1.0F, 0.0F);
        vc.addVertex(mat, (float) v0.x, (float) v0.y, (float) v0.z)
                .setColor(cr, cg, cb, ca).setUv(0.0F, 0.0F).setOverlay(NO_OVERLAY).setLight(FULL_LIGHT).setNormal(0.0F, 1.0F, 0.0F);
    }
}
