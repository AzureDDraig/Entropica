package ddraig.net.entropica.client;

import com.mojang.blaze3d.platform.InputConstants;
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
import ddraig.net.entropica.registry.ModMenuTypes;
import ddraig.net.entropica.block.entity.CreativeVisFumeGeneratorBlockEntity;
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
        event.registerBlockEntityRenderer(ModBlockEntities.MANA_READOUT_BE.get(), VisReadoutRenderer::new);

        event.registerBlockEntityRenderer(ModBlockEntities.VIS_FUME_PIPE_BE.get(), VisFumePipeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.VIS_FUME_VALVE_BE.get(), VisFumePipeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.VIS_FUME_ONE_WAY_VALVE_BE.get(), VisFumePipeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.VIS_FUME_DIVERTER_BE.get(), VisFumePipeRenderer::new);

        event.registerBlockEntityRenderer(ModBlockEntities.VIS_FUME_VESSEL_CONTROLLER_BE.get(), VisFumeVesselControllerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MANA_ENRICHED_GLASS_BE.get(), ManaEnrichedGlassRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ENRICHMENT_TABLE_BE.get(), EnrichmentTableRenderer::new);

        event.registerBlockEntityRenderer(ModBlockEntities.AETHERIC_SYNTHESIZER_BE.get(), AethericSynthesizerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.AETHERIC_AUTOMATOR_BE.get(), AethericAutomatorRenderer::new);

        event.registerBlockEntityRenderer(ModBlockEntities.EIDOLIC_FOCAL_PEDESTAL_BE.get(), EidolicFocalPedestalRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ATTUNEMENT_PEDESTAL_BE.get(), AttunementPedestalRenderer::new);

        event.registerBlockEntityRenderer(ModBlockEntities.CREATIVE_VIS_FUME_GENERATOR_BE.get(), CreativeVisFumeGeneratorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CREATIVE_PARTICLE_GENERATOR_BE.get(), CreativeParticleGeneratorRenderer::new);

        event.registerBlockEntityRenderer(ModBlockEntities.VOID_RIFT_BE.get(), context -> new VoidRiftRenderer(context));

        event.registerEntityRenderer(ModEntityTypes.ESSENCE_ORB.get(), EssenceOrbRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.ESSENCE_NODE.get(), EssenceNodeRenderer::new);
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.FUME_PARTICLE.get(), FumeParticle.Provider::new);
    }

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tintIndex) -> {
            if (level != null && pos != null && tintIndex == 0) {
                if (level.getBlockEntity(pos) instanceof CreativeVisFumeGeneratorBlockEntity generator) {
                    return generator.getCurrentType().getColorInt();
                }
            }
            return 0xFFFFFF;
        }, ModBlocks.CREATIVE_VIS_FUME_GENERATOR.get());

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
                ModBlocks.VIS_ICHOR_ENRICHED_GLASS.get(),
                ModBlocks.VIS_FUME_STRENGTHENED_GLASS.get(),
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

        }, ModFluids.DILUTED_ESSENCE_FLUID_TYPE.get());
    }

    public record AmpouleTint() implements ItemTintSource {
        public static final MapCodec<AmpouleTint> MAP_CODEC = MapCodec.unit(new AmpouleTint());

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
            EssenceType type = null;
            if (stack.getItem() instanceof VisFumeAmpouleItem) {
                type = VisFumeAmpouleItem.getEssenceType(stack);
            }
            if (type != null) {
                int[] rgb = type.getCurrentRGB(System.currentTimeMillis() / 50);
                return (0xFF << 24) | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
            }
            return 0xFFFFFFFF;
        }

        @Override
        public MapCodec<? extends ItemTintSource> type() { return MAP_CODEC; }
    }

    public record EssenceTint() implements ItemTintSource {
        public static final MapCodec<EssenceTint> MAP_CODEC = MapCodec.unit(new EssenceTint());

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
            EssenceType type = null;
            if (stack.getItem() instanceof EssenceItem) {
                type = EssenceItem.getEssenceType(stack);
            } else if (stack.getItem() instanceof EssenceAmpouleItem) {
                type = EssenceAmpouleItem.getEssenceType(stack);
            }
            if (type != null) {
                int[] rgb = type.getCurrentRGB(System.currentTimeMillis() / 50);
                return (0xFF << 24) | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
            }
            return 0xFFFFFFFF;
        }

        @Override
        public MapCodec<? extends ItemTintSource> type() { return MAP_CODEC; }
    }

    public record GeneratorTint() implements ItemTintSource {
        public static final MapCodec<GeneratorTint> MAP_CODEC = MapCodec.unit(new GeneratorTint());

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
            EssenceType[] types = EssenceType.values();
            int index = (int) ((System.currentTimeMillis() / 1000) % types.length);
            return 0xFF000000 | types[index].getColorInt();
        }

        @Override
        public MapCodec<? extends ItemTintSource> type() { return MAP_CODEC; }
    }

    public record FumeGlassTint() implements ItemTintSource {
        public static final MapCodec<FumeGlassTint> MAP_CODEC = MapCodec.unit(new FumeGlassTint());

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
            return 0xFFFFFFFF;
        }

        @Override
        public MapCodec<? extends ItemTintSource> type() { return MAP_CODEC; }
    }

    @SubscribeEvent
    public static void registerItemTintSources(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(ResourceLocation.fromNamespaceAndPath("entropica", "ampoule_tint"), AmpouleTint.MAP_CODEC);
        event.register(ResourceLocation.fromNamespaceAndPath("entropica", "generator_tint"), GeneratorTint.MAP_CODEC);
        event.register(ResourceLocation.fromNamespaceAndPath("entropica", "fume_glass_tint"), FumeGlassTint.MAP_CODEC);
        event.register(ResourceLocation.fromNamespaceAndPath("entropica", "essence_tint"), EssenceTint.MAP_CODEC);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        // Registers the Hulking Beast blockbench model layout into the game!
        event.registerLayerDefinition(EidolicShadowModel.LAYER_LOCATION, EidolicShadowModel::createBodyLayer);
    }
}