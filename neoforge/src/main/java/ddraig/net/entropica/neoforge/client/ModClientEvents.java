package ddraig.net.entropica.neoforge.client;

import net.minecraft.client.renderer.entity.ThrownItemRenderer;

import com.mojang.blaze3d.platform.InputConstants;
import ddraig.net.entropica.client.HazeShaderManager;
import ddraig.net.entropica.client.ModItemTintSources;
import ddraig.net.entropica.client.model.GrotModel;
import ddraig.net.entropica.client.model.ModModelLayers;
import ddraig.net.entropica.client.model.VeilFoxModel;
import ddraig.net.entropica.client.particle.FumeParticle;
import ddraig.net.entropica.client.renderer.*;
import ddraig.net.entropica.client.renderer.item.DynamicWeaponRenderer;
import ddraig.net.entropica.registry.ModParticles;
import net.minecraft.client.KeyMapping;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.item.EssenceAmpouleItem;
import ddraig.net.entropica.item.EssenceItem;
import ddraig.net.entropica.item.VisFumeAmpouleItem;
import ddraig.net.entropica.registry.ModBlockEntities;
import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModEntityTypes;
import ddraig.net.entropica.registry.ModFluids;
import ddraig.net.entropica.registry.neoforge.ModFluidsNeoForge;
import ddraig.net.entropica.registry.ModMenuTypes;
import ddraig.net.entropica.block.entity.CreativeMateriaGeneratorBlockEntity;
import ddraig.net.entropica.block.entity.MateriaEnrichedGlassBlockEntity;
import ddraig.net.entropica.block.entity.DilutedEssenceFluidBlockEntity;
import ddraig.net.entropica.client.gui.SynthesizerUserInterfaceScreen;
import ddraig.net.entropica.client.gui.WeaponNamingScreen;
import ddraig.net.entropica.Entropica;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.gui.Font;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = "entropica", value = Dist.CLIENT)
public class ModClientEvents {

    public static boolean debugMode = false;
    public static final KeyMapping DEBUG_KEY = new KeyMapping(
            "key.entropica.debug",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_L,
            KeyMapping.Category.MISC
    );
    public static final KeyMapping SWAP_LENS_KEY = new KeyMapping(
            "key.entropica.swap_lens",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            KeyMapping.Category.MISC
    );

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(DEBUG_KEY);
        event.register(SWAP_LENS_KEY);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (DEBUG_KEY.consumeClick()) {
            debugMode = !debugMode;
            Minecraft.getInstance().gui.setOverlayMessage(
                    Component.literal("Entropica Debug: " + (debugMode ? "§aON" : "§cOFF")),
                    false);
        }
        if (SWAP_LENS_KEY.consumeClick()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                ItemStack head = mc.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD);
                if (head.is(ddraig.net.entropica.registry.ModItems.ARKANIST_MONOCLE.get())) {
                    dev.architectury.networking.NetworkManager.sendToServer(new ddraig.net.entropica.network.MonocleLensSwapPayload(false));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        
        // Custom Haze shader tick manager
        HazeShaderManager.clientTick(mc);
        ddraig.net.entropica.client.ParalyzedParticleHandler.clientTick(mc);

        if (mc.player != null && mc.level != null && mc.level.getGameTime() % 4 == 0) {
            BlockPos p = mc.player.blockPosition();
            int r = 16;
            mc.levelRenderer.setBlocksDirty(p.getX() - r, p.getY() - r, p.getZ() - r, p.getX() + r, p.getY() + r, p.getZ() + r);
        }

        if (mc.player != null && mc.level != null && mc.screen == null && mc.level.getGameTime() % 10 == 0) {
            for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
                ItemStack stack = mc.player.getInventory().getItem(i);
                if (stack.getItem() instanceof ddraig.net.entropica.item.DynamicVisWeaponItem) {
                    ddraig.net.entropica.component.VisWeaponState state = stack.get(ddraig.net.entropica.registry.ModDataComponents.VIS_WEAPON_STATE.get());
                    if (state != null && !state.hasInitializedName()) {
                        mc.setScreen(new WeaponNamingScreen());
                        break;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.SYNTHESIZER_USER_INTERFACE_MENU.get(), SynthesizerUserInterfaceScreen::new);
    }

    @SubscribeEvent
    public static void registerSpecialModels(RegisterSpecialModelRendererEvent event) {
        event.register(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "dynamic_weapon"), DynamicWeaponRenderer.Unbaked.MAP_CODEC);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntityTypes.SPORE_PROJECTILE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MAGMA_THORN.get(), ThrownItemRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MATERIA_FURNACE_BE.get(), MateriaFurnaceRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ENTROPIC_CORE_BE.get(), EntropicCoreRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ESSENCE_READOUT_BE.get(), EssenceReadoutRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MANA_READOUT_BE.get(), MateriaReadoutRenderer::new);

        event.registerBlockEntityRenderer(ModBlockEntities.VAPOR_PNEUMATIC_PIPE_BE.get(), VaporPneumaticPipeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.VAPOR_PNEUMATIC_VALVE_BE.get(), VaporPneumaticPipeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.VAPOR_PNEUMATIC_ONE_WAY_VALVE_BE.get(), VaporPneumaticPipeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.VAPOR_PNEUMATIC_DIVERTER_BE.get(), VaporPneumaticPipeRenderer::new);

        event.registerBlockEntityRenderer(ModBlockEntities.HYDRAULIC_PIPELINE_BE.get(), VaporPneumaticPipeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.VOLTAIC_CONDUIT_BE.get(), VaporPneumaticPipeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.VISCOUS_AGITATOR_BE.get(), VaporPneumaticPipeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SANGUINE_CONDUIT_BE.get(), VaporPneumaticPipeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.EQUILIBRIUM_CONDUIT_BE.get(), VaporPneumaticPipeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.PRISTINE_CONDUIT_BE.get(), VaporPneumaticPipeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ATHANOR_CONDUIT_BE.get(), VaporPneumaticPipeRenderer::new);

        event.registerBlockEntityRenderer(ModBlockEntities.MATERIA_VESSEL_CONTROLLER_BE.get(), MateriaVesselControllerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MATERIA_ENRICHED_GLASS_BE.get(), MateriaEnrichedGlassRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ENRICHMENT_TABLE_BE.get(), EnrichmentTableRenderer::new);

        event.registerBlockEntityRenderer(ModBlockEntities.AETHERIC_SYNTHESIZER_BE.get(), AethericSynthesizerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.AETHERIC_AUTOMATOR_BE.get(), AethericAutomatorRenderer::new);

        event.registerBlockEntityRenderer(ModBlockEntities.EIDOLIC_FOCAL_PEDESTAL_BE.get(), EidolicFocalPedestalRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ATTUNEMENT_PEDESTAL_BE.get(), AttunementPedestalRenderer::new);

        event.registerBlockEntityRenderer(ModBlockEntities.CREATIVE_MATERIA_GENERATOR_BE.get(), CreativeMateriaGeneratorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CREATIVE_PARTICLE_GENERATOR_BE.get(), CreativeParticleGeneratorRenderer::new);

        event.registerBlockEntityRenderer(ModBlockEntities.VOID_RIFT_BE.get(), context -> new VoidRiftRenderer(context));

        // --- NEW: Crucible Renderer ---
        event.registerBlockEntityRenderer(ModBlockEntities.CRUCIBLE_BE.get(), CrucibleRenderer::new);

        // --- NEW: Chalk & Scribing Engine ---
        event.registerBlockEntityRenderer(ModBlockEntities.SCRIBED_CHALK_BE.get(), ScribedChalkRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SCRIBING_CONTROLLER_BE.get(), ScribingControllerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.VISCANITE_PISTON_PRESS_BE.get(), ViscanitePistonPressRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MATERIA_BLESSING_BE.get(), MateriaBlessingRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RUBBER_LOG_BE.get(), ddraig.net.entropica.client.renderer.RubberLogBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MOD_CHEST.get(), ddraig.net.entropica.client.renderer.ModChestRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.STATIONARY_BRASS_TELESCOPE_BE.get(), ddraig.net.entropica.client.renderer.StationaryBrassTelescopeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.REFRACTIVE_ASTRAL_LENS_BE.get(), ddraig.net.entropica.client.renderer.RefractiveAstralLensRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SECONDARY_ASTRAL_LENS_BE.get(), ddraig.net.entropica.client.renderer.SecondaryAstralLensRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.BEAM_SPLITTER_PRISM_BE.get(), ddraig.net.entropica.client.renderer.BeamSplitterPrismRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ASTRAL_COLLECTOR_BE.get(), ddraig.net.entropica.client.renderer.AstralCollectorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ASTRAL_INFUSION_PEDESTAL_BE.get(), ddraig.net.entropica.client.renderer.AstralInfusionPedestalRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.OPTICAL_RECEIVER_PORT_BE.get(), ddraig.net.entropica.client.renderer.OpticalReceiverPortRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ASTRAL_MIRROR_BE.get(), ddraig.net.entropica.client.renderer.AstralMirrorRenderer::new);

        event.registerEntityRenderer(ModEntityTypes.ESSENCE_ORB.get(), EssenceOrbRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.ESSENCE_NODE.get(), EssenceNodeRenderer::new);

        event.registerEntityRenderer(ModEntityTypes.EIDOLIC_SHADOW.get(), EidolicShadowRenderer::new);

        event.registerEntityRenderer(ModEntityTypes.GROT.get(), GrotRenderer::new);

        event.registerEntityRenderer(ModEntityTypes.VEIL_FOX.get(), VeilFoxRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.VEIL_FOX_AFTERIMAGE.get(), VeilFoxAfterimageRenderer::new);

        event.registerEntityRenderer(ModEntityTypes.ASHEN_STALKER.get(), ddraig.net.entropica.client.renderer.AshenStalkerRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.SPORE_DRIFTER.get(), SporeDrifterRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.SPORE_CLOUD.get(), SporeCloudRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.RIME_BACK_OVIS.get(), RimeBackOvisRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.OVERGROWTH_OVIS.get(), OvergrowthOvisRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.RIME_SHEPHERD.get(), RimeShepherdRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.BLOOM_CRAWLER.get(), BloomCrawlerRenderer::new);
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.FUME_PARTICLE.get(), FumeParticle.Provider::new);
        event.registerSpriteSet(ModParticles.SPECTRUM_SPARKLE.get(), ddraig.net.entropica.client.particle.SpectrumSparkleParticle.Provider::new);
        event.registerSpriteSet(ModParticles.GALE_SWIRL_PUFF.get(), ddraig.net.entropica.client.particle.GaleSwirlPuffParticle.Provider::new);
    }



    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tintIndex) -> {
            if (tintIndex == 0) {
                return ddraig.net.entropica.client.AmbientEssenceTintRegistry.getAmbientEssenceColor(level, pos);
            }
            return 0xFFFFFF;
        }, ModBlocks.SHIMMERPETAL.get());

        event.register((state, level, pos, tintIndex) -> {
            if (level != null && pos != null && tintIndex == 0) {
                if (level.getBlockEntity(pos) instanceof CreativeMateriaGeneratorBlockEntity generator) {
                    return generator.getCurrentType().getColorInt();
                }
            }
            return 0xFFFFFF;
        }, ModBlocks.CREATIVE_MATERIA_GENERATOR.get());

        event.register((state, level, pos, tintIndex) -> {
                    if (tintIndex == 0) {
                        return ddraig.net.entropica.registry.AestheticGlassRegistry.getGlassColor(state.getBlock());
                    }
                    return 0xFFFFFF;
                },
                ddraig.net.entropica.registry.AestheticGlassRegistry.ALL_GLASS_BLOCKS.stream().map(dev.architectury.registry.registries.RegistrySupplier::get).toArray(net.minecraft.world.level.block.Block[]::new));
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new IClientFluidTypeExtensions() {
            private static final ResourceLocation STILL = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "block/diluted_essence_still");
            private static final ResourceLocation FLOW = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "block/diluted_essence_flow");

            @Override
            public ResourceLocation getStillTexture() { return STILL; }
            @Override
            public ResourceLocation getFlowingTexture() { return FLOW; }

            @Override
            public int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
                if (getter != null && pos != null) {
                    if (getter.getBlockEntity(pos) instanceof DilutedEssenceFluidBlockEntity be) {
                        return be.getTintColor();
                    }
                    for (int x = -1; x <= 1; x++) {
                        for (int y = 0; y <= 1; y++) {
                            for (int z = -1; z <= 1; z++) {
                                if (getter.getBlockEntity(pos.offset(x, y, z)) instanceof DilutedEssenceFluidBlockEntity be) {
                                    return be.getTintColor();
                                }
                            }
                        }
                    }
                }
                return 0xFFB200FF;
            }

            @Override
            public int getTintColor() {
                return 0xFFB200FF;
            }

        }, ModFluidsNeoForge.DILUTED_ESSENCE_FLUID_TYPE.get());
    }

    @SubscribeEvent
    public static void registerItemTintSources(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(ResourceLocation.fromNamespaceAndPath("entropica", "ampoule_tint"), ModItemTintSources.AmpouleTint.MAP_CODEC);
        event.register(ResourceLocation.fromNamespaceAndPath("entropica", "generator_tint"), ModItemTintSources.GeneratorTint.MAP_CODEC);
        event.register(ResourceLocation.fromNamespaceAndPath("entropica", "fume_glass_tint"), ModItemTintSources.FumeGlassTint.MAP_CODEC);
        event.register(ResourceLocation.fromNamespaceAndPath("entropica", "essence_tint"), ModItemTintSources.EssenceTint.MAP_CODEC);
        event.register(ResourceLocation.fromNamespaceAndPath("entropica", "shard_tint"), ModItemTintSources.ShardTint.MAP_CODEC);
        event.register(ResourceLocation.fromNamespaceAndPath("entropica", "aesthetic_glass_tint"), ModItemTintSources.AestheticGlassTint.MAP_CODEC);
        event.register(ResourceLocation.fromNamespaceAndPath("entropica", "spectral_dye_tint"), ModItemTintSources.SpectralDyeTint.MAP_CODEC);
        event.register(ResourceLocation.fromNamespaceAndPath("entropica", "ambient_essence_tint"), ModItemTintSources.AmbientEssenceTint.MAP_CODEC);
        event.register(ResourceLocation.fromNamespaceAndPath("entropica", "echo_fruit_tint"), ModItemTintSources.EchoFruitTint.MAP_CODEC);
    }



    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(EidolicShadowModel.LAYER_LOCATION, EidolicShadowModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.GROT, GrotModel::createBodyLayer);
        event.registerLayerDefinition(VeilFoxModel.LAYER_LOCATION, VeilFoxModel::createBodyLayer);
        event.registerLayerDefinition(ddraig.net.entropica.client.model.AshenStalkerModel.LAYER_LOCATION, ddraig.net.entropica.client.model.AshenStalkerModel::createBodyLayer);
        event.registerLayerDefinition(RimeBackOvisModel.LAYER_LOCATION, RimeBackOvisModel::createBodyLayer);
        event.registerLayerDefinition(RimeShepherdModel.LAYER_LOCATION, RimeShepherdModel::createBodyLayer);
        event.registerLayerDefinition(SporeDrifterModel.LAYER_LOCATION, SporeDrifterModel::createBodyLayer);
        event.registerLayerDefinition(BloomCrawlerModel.LAYER_LOCATION, BloomCrawlerModel::createBodyLayer);
    }



    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        Player player = event.getEntity();
        if (player == null) return;
        if (!ddraig.net.entropica.client.LensOverlayRenderer.isMateriaVisionActive(player)) return;
        
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;
        
        java.util.List<ddraig.net.entropica.api.ItemEssenceMap.EssenceValue> values = ddraig.net.entropica.api.ItemEssenceMap.getEssenceFor(stack);
        if (values != null && !values.isEmpty()) {
            event.getToolTip().add(Component.literal("§5Materia Yield:"));
            for (ddraig.net.entropica.api.ItemEssenceMap.EssenceValue val : values) {
                String name = val.type().getColorCode() + val.type().getDisplayName();
                event.getToolTip().add(Component.literal(String.format("  §7- %.2f %s", val.amount(), name)));
            }
        }
    }

    @SubscribeEvent
    public static void onRenderLevelStage(net.neoforged.neoforge.client.event.RenderLevelStageEvent.AfterSky event) {
        PoseStack poseStack = event.getPoseStack();
        if (poseStack != null) {
            Minecraft mc = Minecraft.getInstance();
            ddraig.net.entropica.client.renderer.CelestialSkyRenderer.renderSky(
                poseStack,
                event.getModelViewMatrix(),
                mc.gameRenderer.getMainCamera(),
                mc.getDeltaTracker().getGameTimeDeltaTicks()
            );
        }
    }

    @SubscribeEvent
    public static void onRenderLevelStageAfterTranslucent(net.neoforged.neoforge.client.event.RenderLevelStageEvent.AfterTranslucentBlocks event) {
        PoseStack poseStack = event.getPoseStack();
        if (poseStack != null) {
            Minecraft mc = Minecraft.getInstance();
            ddraig.net.entropica.client.renderer.AstrolabeGhostRenderer.renderGhost(
                poseStack,
                event.getModelViewMatrix(),
                mc.gameRenderer.getMainCamera(),
                mc.getDeltaTracker().getGameTimeDeltaTicks()
            );
            ddraig.net.entropica.client.renderer.AstralLinkingWandGuideRenderer.renderGuide(
                poseStack,
                event.getModelViewMatrix(),
                mc.gameRenderer.getMainCamera(),
                mc.getDeltaTracker().getGameTimeDeltaTicks()
            );
        }
    }

    @SubscribeEvent
    public static void onRenderGui(net.neoforged.neoforge.client.event.RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            float partialTick = mc.getDeltaTracker().getGameTimeDeltaTicks();
            ddraig.net.entropica.client.LookingGlassOverlayRenderer.render(event.getGuiGraphics(), partialTick);
            ddraig.net.entropica.client.gui.OpticalInspectionHudOverlay.render(event.getGuiGraphics(), partialTick);
        }
    }
}