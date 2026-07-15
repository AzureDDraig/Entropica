package ddraig.net.entropica.client;

import com.mojang.blaze3d.platform.InputConstants;
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
import ddraig.net.entropica.block.entity.ManaEnrichedGlassBlockEntity;
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
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
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

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(DEBUG_KEY);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (DEBUG_KEY.consumeClick()) {
            debugMode = !debugMode;
            Minecraft.getInstance().gui.setOverlayMessage(
                    Component.literal("Entropica Debug: " + (debugMode ? "§aON" : "§cOFF")),
                    false);
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
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
        event.registerBlockEntityRenderer(ModBlockEntities.MANA_FURNACE_BE.get(), ManaFurnaceRenderer::new);
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
        event.registerBlockEntityRenderer(ModBlockEntities.MANA_ENRICHED_GLASS_BE.get(), ManaEnrichedGlassRenderer::new);
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

        event.registerEntityRenderer(ModEntityTypes.ESSENCE_ORB.get(), EssenceOrbRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.ESSENCE_NODE.get(), EssenceNodeRenderer::new);

        event.registerEntityRenderer(ModEntityTypes.EIDOLIC_SHADOW.get(), EidolicShadowRenderer::new);

        event.registerEntityRenderer(ModEntityTypes.GROT.get(), GrotRenderer::new);

        event.registerEntityRenderer(ModEntityTypes.VEIL_FOX.get(), VeilFoxRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.VEIL_FOX_AFTERIMAGE.get(), VeilFoxAfterimageRenderer::new);

        event.registerEntityRenderer(ModEntityTypes.ASHEN_STALKER.get(), ddraig.net.entropica.client.renderer.AshenStalkerRenderer::new);
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.FUME_PARTICLE.get(), FumeParticle.Provider::new);
    }

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tintIndex) -> {
            if (level != null && pos != null && tintIndex == 0) {
                if (level.getBlockEntity(pos) instanceof CreativeMateriaGeneratorBlockEntity generator) {
                    return generator.getCurrentType().getColorInt();
                }
            }
            return 0xFFFFFF;
        }, ModBlocks.CREATIVE_MATERIA_GENERATOR.get());

        event.register((state, level, pos, tintIndex) -> {
                    if (level != null && pos != null && tintIndex == 0) {
                        if (level.getBlockEntity(pos) instanceof ManaEnrichedGlassBlockEntity glassBE) {
                            EssenceType type = glassBE.getEssenceType();
                            if (type != null) {
                                return type.getColorInt();
                            }
                        }
                    }
                    return 0xFFFFFF;
                },
                ModBlocks.FRAGMENT_LATTICE_GLASS.get(),
                ModBlocks.MATERIA_LIQUIDA_ENRICHED_GLASS.get(),
                ModBlocks.MATERIA_FUMUS_STRENGTHENED_GLASS.get(),
                ModBlocks.ESSENCE_ENRICHED_GLASS.get());
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
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(EidolicShadowModel.LAYER_LOCATION, EidolicShadowModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.GROT, GrotModel::createBodyLayer);
        event.registerLayerDefinition(VeilFoxModel.LAYER_LOCATION, VeilFoxModel::createBodyLayer);
        event.registerLayerDefinition(ddraig.net.entropica.client.model.AshenStalkerModel.LAYER_LOCATION, ddraig.net.entropica.client.model.AshenStalkerModel::createBodyLayer);
    }
}