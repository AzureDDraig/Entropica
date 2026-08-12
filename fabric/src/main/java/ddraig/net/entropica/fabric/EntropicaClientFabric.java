package ddraig.net.entropica.fabric;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.block.entity.CreativeMateriaGeneratorBlockEntity;
import ddraig.net.entropica.block.entity.DilutedEssenceFluidBlockEntity;
import ddraig.net.entropica.block.entity.MateriaEnrichedGlassBlockEntity;
import ddraig.net.entropica.client.ModItemTintSources;
import ddraig.net.entropica.client.gui.SynthesizerUserInterfaceScreen;
import ddraig.net.entropica.client.model.AshenStalkerModel;
import ddraig.net.entropica.client.model.GrotModel;
import ddraig.net.entropica.client.model.ModModelLayers;
import ddraig.net.entropica.client.model.VeilFoxModel;
import ddraig.net.entropica.client.renderer.*;
import ddraig.net.entropica.client.renderer.item.DynamicWeaponRenderer;
import ddraig.net.entropica.registry.ModBlockEntities;
import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModEntityTypes;
import ddraig.net.entropica.registry.ModItems;
import ddraig.net.entropica.registry.ModMenuTypes;
import ddraig.net.entropica.registry.ModParticles;
import ddraig.net.entropica.registry.fabric.ModFluidsFabric;

import ddraig.net.entropica.client.particle.TimedTintableParticle;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

public class EntropicaClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // --- 1. Block Entity Renderers ---
        BlockEntityRenderers.register(ModBlockEntities.MATERIA_FURNACE_BE.get(), MateriaFurnaceRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.ENTROPIC_CORE_BE.get(), EntropicCoreRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.ESSENCE_READOUT_BE.get(), EssenceReadoutRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.MANA_READOUT_BE.get(), MateriaReadoutRenderer::new);

        BlockEntityRenderers.register(ModBlockEntities.VAPOR_PNEUMATIC_PIPE_BE.get(), VaporPneumaticPipeRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.VAPOR_PNEUMATIC_VALVE_BE.get(), VaporPneumaticPipeRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.VAPOR_PNEUMATIC_ONE_WAY_VALVE_BE.get(), VaporPneumaticPipeRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.VAPOR_PNEUMATIC_DIVERTER_BE.get(), VaporPneumaticPipeRenderer::new);

        BlockEntityRenderers.register(ModBlockEntities.HYDRAULIC_PIPELINE_BE.get(), VaporPneumaticPipeRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.VOLTAIC_CONDUIT_BE.get(), VaporPneumaticPipeRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.VISCOUS_AGITATOR_BE.get(), VaporPneumaticPipeRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.SANGUINE_CONDUIT_BE.get(), VaporPneumaticPipeRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.EQUILIBRIUM_CONDUIT_BE.get(), VaporPneumaticPipeRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.PRISTINE_CONDUIT_BE.get(), VaporPneumaticPipeRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.ATHANOR_CONDUIT_BE.get(), VaporPneumaticPipeRenderer::new);

        BlockEntityRenderers.register(ModBlockEntities.MATERIA_VESSEL_CONTROLLER_BE.get(), MateriaVesselControllerRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.MATERIA_ENRICHED_GLASS_BE.get(), MateriaEnrichedGlassRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.ENRICHMENT_TABLE_BE.get(), EnrichmentTableRenderer::new);

        BlockEntityRenderers.register(ModBlockEntities.AETHERIC_SYNTHESIZER_BE.get(), AethericSynthesizerRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.AETHERIC_AUTOMATOR_BE.get(), AethericAutomatorRenderer::new);

        BlockEntityRenderers.register(ModBlockEntities.EIDOLIC_FOCAL_PEDESTAL_BE.get(), EidolicFocalPedestalRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.ATTUNEMENT_PEDESTAL_BE.get(), AttunementPedestalRenderer::new);

        BlockEntityRenderers.register(ModBlockEntities.CREATIVE_MATERIA_GENERATOR_BE.get(), CreativeMateriaGeneratorRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.CREATIVE_PARTICLE_GENERATOR_BE.get(), CreativeParticleGeneratorRenderer::new);

        BlockEntityRenderers.register(ModBlockEntities.VOID_RIFT_BE.get(), VoidRiftRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.CRUCIBLE_BE.get(), CrucibleRenderer::new);

        // --- NEW: Chalk & Scribing Engine ---
        BlockEntityRenderers.register(ModBlockEntities.SCRIBED_CHALK_BE.get(), ScribedChalkRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.SCRIBING_CONTROLLER_BE.get(), ScribingControllerRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.VISCANITE_PISTON_PRESS_BE.get(), ViscanitePistonPressRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.MATERIA_BLESSING_BE.get(), MateriaBlessingRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.RUBBER_LOG_BE.get(), ddraig.net.entropica.client.renderer.RubberLogBlockEntityRenderer::new);

        // --- 2. Entity Renderers ---
        EntityRendererRegistry.register(ModEntityTypes.ESSENCE_ORB.get(), EssenceOrbRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.ESSENCE_NODE.get(), EssenceNodeRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.EIDOLIC_SHADOW.get(), EidolicShadowRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.GROT.get(), GrotRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.VEIL_FOX.get(), VeilFoxRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.VEIL_FOX_AFTERIMAGE.get(), VeilFoxAfterimageRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.ASHEN_STALKER.get(), AshenStalkerRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.SPORE_DRIFTER.get(), SporeDrifterRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.SPORE_CLOUD.get(), SporeCloudRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.RIME_BACK_OVIS.get(), RimeBackOvisRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.OVERGROWTH_OVIS.get(), OvergrowthOvisRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.RIME_SHEPHERD.get(), RimeShepherdRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.BLOOM_CRAWLER.get(), BloomCrawlerRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.SPORE_PROJECTILE.get(), ThrownItemRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.MAGMA_THORN.get(), ThrownItemRenderer::new);

        dev.architectury.registry.client.particle.ParticleProviderRegistry.register(
            ModParticles.SPECTRUM_SPARKLE.get(),
            ddraig.net.entropica.client.particle.SpectrumSparkleParticle.Provider::new
        );
        dev.architectury.registry.client.particle.ParticleProviderRegistry.register(
            ModParticles.GALE_SWIRL_PUFF.get(),
            ddraig.net.entropica.client.particle.GaleSwirlPuffParticle.Provider::new
        );



        // --- 3. Entity Layer Definitions ---
        EntityModelLayerRegistry.registerModelLayer(EidolicShadowModel.LAYER_LOCATION, EidolicShadowModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(ModModelLayers.GROT, GrotModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(VeilFoxModel.LAYER_LOCATION, VeilFoxModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(AshenStalkerModel.LAYER_LOCATION, AshenStalkerModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(RimeBackOvisModel.LAYER_LOCATION, RimeBackOvisModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(RimeShepherdModel.LAYER_LOCATION, RimeShepherdModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(SporeDrifterModel.LAYER_LOCATION, SporeDrifterModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(BloomCrawlerModel.LAYER_LOCATION, BloomCrawlerModel::createBodyLayer);

        // --- 4. Menu Screens ---
        MenuScreens.register(ModMenuTypes.SYNTHESIZER_USER_INTERFACE_MENU.get(), SynthesizerUserInterfaceScreen::new);

        // --- 5. Block Colors ---
        ColorProviderRegistry.BLOCK.register((state, level, pos, tintIndex) -> {
            if (level instanceof net.minecraft.world.level.Level world && pos != null) {
                return ddraig.net.entropica.block.MateriaEchoFruitBlock.getAspectForBiome(world, pos).getColorInt();
            }
            return 0xFFFFFF;
        }, ModBlocks.MATERIA_ECHO_FRUIT.get());


        ColorProviderRegistry.BLOCK.register((state, level, pos, tintIndex) -> {
            if (tintIndex == 0) {
                return ddraig.net.entropica.client.AmbientEssenceTintRegistry.getAmbientEssenceColor(level, pos);
            }
            return 0xFFFFFF;
        }, ModBlocks.SHIMMERPETAL.get());

        ColorProviderRegistry.BLOCK.register((state, level, pos, tintIndex) -> {
            if (level != null && pos != null && tintIndex == 0) {
                if (level.getBlockEntity(pos) instanceof CreativeMateriaGeneratorBlockEntity generator) {
                    return generator.getCurrentType().getColorInt();
                }
            }
            return 0xFFFFFF;
        }, ModBlocks.CREATIVE_MATERIA_GENERATOR.get());

        ColorProviderRegistry.BLOCK.register((state, level, pos, tintIndex) -> {
            if (tintIndex == 0) {
                return ddraig.net.entropica.registry.AestheticGlassRegistry.getGlassColor(state.getBlock());
            }
            return 0xFFFFFF;
        }, ddraig.net.entropica.registry.AestheticGlassRegistry.ALL_GLASS_BLOCKS.stream().map(dev.architectury.registry.registries.RegistrySupplier::get).toArray(net.minecraft.world.level.block.Block[]::new));




        // --- 6. Custom Fluid Render Handler & Render Layer ---
        FluidRenderHandlerRegistry.INSTANCE.register(
            ModFluidsFabric.STILL,
            ModFluidsFabric.FLOWING,
            new FluidRenderHandler() {
                private final TextureAtlasSprite[] sprites = new TextureAtlasSprite[2];

                @Override
                public TextureAtlasSprite[] getFluidSprites(@Nullable BlockAndTintGetter view, @Nullable BlockPos pos, FluidState state) {
                    if (sprites[0] == null) {
                        var atlas = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(TextureAtlas.LOCATION_BLOCKS);
                        sprites[0] = atlas.getSprite(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "block/diluted_essence_still"));
                        sprites[1] = atlas.getSprite(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "block/diluted_essence_flow"));
                    }
                    return sprites;
                }

                @Override
                public int getFluidColor(@Nullable BlockAndTintGetter view, @Nullable BlockPos pos, FluidState state) {
                    if (view != null && pos != null) {
                        if (view.getBlockEntity(pos) instanceof DilutedEssenceFluidBlockEntity be) {
                            return be.getTintColor();
                        }
                        for (int x = -1; x <= 1; x++) {
                            for (int y = 0; y <= 1; y++) {
                                for (int z = -1; z <= 1; z++) {
                                    if (view.getBlockEntity(pos.offset(x, y, z)) instanceof DilutedEssenceFluidBlockEntity be) {
                                        return be.getTintColor();
                                    }
                                }
                            }
                        }
                    }
                    return 0xFFB200FF;
                }
            }
        );

        BlockRenderLayerMap.INSTANCE.putFluids(
            net.minecraft.client.renderer.Sheets.translucentItemSheet(),
            ModFluidsFabric.STILL,
            ModFluidsFabric.FLOWING
        );

        BlockRenderLayerMap.INSTANCE.putBlocks(
            RenderType.translucentMovingBlock(),
            ddraig.net.entropica.registry.AestheticGlassRegistry.ALL_GLASS_BLOCKS.stream().map(dev.architectury.registry.registries.RegistrySupplier::get).toArray(net.minecraft.world.level.block.Block[]::new)
        );

        BlockRenderLayerMap.INSTANCE.putBlocks(
            RenderType.cutout(),
            ModBlocks.MATERIA_ECHO_LEAVES.get(),
            ModBlocks.MATERIA_ECHO_SAPLING.get(),
            ModBlocks.MATERIA_ECHO_FRUIT.get(),
            ModBlocks.PYRE_ASH_CEDAR_LEAVES.get(),
            ModBlocks.PYRE_ASH_CEDAR_SAPLING.get(),
            ModBlocks.ABYSSAL_SPORE_CYPRESS_LEAVES.get(),
            ModBlocks.ABYSSAL_SPORE_CYPRESS_SAPLING.get(),
            ModBlocks.STARLIGHT_AETHER_BIRCH_LEAVES.get(),
            ModBlocks.STARLIGHT_AETHER_BIRCH_SAPLING.get(),
            ModBlocks.BLOOD_ROOT_IRON_OAK_LEAVES.get(),
            ModBlocks.BLOOD_ROOT_IRON_OAK_SAPLING.get(),
            ModBlocks.AEGIS_ROSE.get(),
            ModBlocks.TALL_AEGIS_ROSE.get(),
            ModBlocks.NECROTIC_ROSE_OF_JERICHO.get(),
            ModBlocks.TALL_NECROTIC_ROSE_OF_JERICHO.get(),
            ModBlocks.SANGUINE_LILY.get(),
            ModBlocks.AURORAL_LILY_PAD.get(),
            ModBlocks.CINDER_GRIP_LICHEN.get(),
            ModBlocks.SPORE_CANNON_PUFFBALL.get(),
            ModBlocks.PYRE_THORN_LAUNCHER.get(),
            ModBlocks.SPORE_BEARING_PITCHER_PLUMP.get(),
            ModBlocks.BLOOD_TENDRIL_BRAMBLE.get(),
            ModBlocks.SOOT_VEIL_BLIGHT_CAP.get(),
            ModBlocks.MAGMA_GRIP_TENDRILS.get(),

            ModBlocks.SOOT_SHROUD_FUNGI.get(),
            ModBlocks.PYROCYST_ALGAE.get(),
            ModBlocks.BARROW_FUNGAL_SHELF_CAP.get(),
            ModBlocks.SPORE_FUNGAL_SHELF_CAP.get(),
            ModBlocks.BLIGHT_FUNGAL_SHELF_CAP.get(),
            ModBlocks.FROST_FUNGAL_SHELF_CAP.get(),
            ModBlocks.CINDER_FUNGAL_SHELF_CAP.get(),
            ModBlocks.ASTRAL_FUNGAL_SHELF_CAP.get(),
            ModBlocks.DAWN_FUNGAL_SHELF_CAP.get(),
            ModBlocks.SANGUINE_FUNGAL_SHELF_CAP.get(),
            ModBlocks.STATIC_FUNGAL_SHELF_CAP.get(),
            ModBlocks.AEGIS_SPIRE_ORCHID.get(),



            
            ModBlocks.ASTRAL_VEIL_WILLOW_LEAVES.get(),
            ModBlocks.ASTRAL_VEIL_WILLOW_SAPLING.get(),
            ModBlocks.ASTRAL_VEIL_WILLOW_VINES.get(),
            ModBlocks.VOID_BLIGHT_MANGROVE_LEAVES.get(),
            ModBlocks.VOID_BLIGHT_MANGROVE_SAPLING.get(),
            ModBlocks.VOID_BLIGHT_MANGROVE_ROOT.get(),
            ModBlocks.TALL_AEGIS_SPIRE_ORCHID.get(),

            ModBlocks.SOUL_FLAME_ORCHID.get(),
            ModBlocks.TALL_SOUL_FLAME_ORCHID.get(),
            ModBlocks.VITAE_ORCHID.get(),
            ModBlocks.TALL_VITAE_ORCHID.get(),
            ModBlocks.VOID_STALKER_ORCHID.get(),
            ModBlocks.TALL_VOID_STALKER_ORCHID.get(),
            ModBlocks.AURORAL_BUTTERCUP.get(),
            ModBlocks.STARDUST_BELL.get(),
            ModBlocks.FULGURITE_SWAMP_BLOOM.get(),
            ModBlocks.GALE_BLOOM_DANDELION.get(),
            ModBlocks.CRYO_STATIC_SHRUB.get(),
            ModBlocks.VITREOUS_CACTUS.get(),
            ModBlocks.BARROW_MOSS.get(),
            ModBlocks.BARROW_MOSS_CARPET.get(),
            ModBlocks.AMBER_NECTAR_BLOSSOM.get(),
            ModBlocks.RIMEBLOOM.get(),
            ModBlocks.SHIMMERPETAL.get(),
            ModBlocks.ABYSSAL_WEEPROOT.get(),
            ModBlocks.ABYSSAL_WEEPROOT_PLANT.get(),
            ModBlocks.BLOOD_ROOT_SUCCULENT.get(),
            ModBlocks.SPORE_BURST_PUFFBALL.get(),
            ModBlocks.FULGURITE_REED.get(),
            ModBlocks.GALE_THISTLE.get(),
            ModBlocks.MIST_VEIL_MARSHMALLOW.get(),
            ModBlocks.PYRE_SPROUT.get(),
            ModBlocks.AURA_DRIFT_SEDGE.get(),
            ModBlocks.SPECTRAL_LANTERN_FLOWER.get(),
            ModBlocks.CINDER_SPORE_MUSHROOM.get(),
            ModBlocks.STARDUST_ALOE.get(),
            ModBlocks.AMBER_SAPLING.get(),
            ModBlocks.AMBER_LEAVES.get(),
            ModBlocks.RUBBER_SAPLING.get(),
            ModBlocks.RUBBER_LEAVES.get(),
            ModBlocks.SILVER_PINE_SAPLING.get(),
            ModBlocks.SILVER_PINE_LEAVES.get(),
            ModBlocks.SMALL_AETERIUM_BUD.get(),
            ModBlocks.MEDIUM_AETERIUM_BUD.get(),
            ModBlocks.LARGE_AETERIUM_BUD.get(),
            ModBlocks.AETERIUM_CLUSTER.get(),
            ModBlocks.SMALL_IGNISITE_BUD.get(),
            ModBlocks.MEDIUM_IGNISITE_BUD.get(),
            ModBlocks.LARGE_IGNISITE_BUD.get(),
            ModBlocks.IGNISITE_CLUSTER.get(),
            ModBlocks.SMALL_MORTISITE_BUD.get(),
            ModBlocks.MEDIUM_MORTISITE_BUD.get(),
            ModBlocks.LARGE_MORTISITE_BUD.get(),
            ModBlocks.MORTISITE_CLUSTER.get()
        );


        BlockRenderLayerMap.INSTANCE.putBlocks(
            net.minecraft.client.renderer.Sheets.translucentItemSheet(),
            ModBlocks.CATALYST_RECEPTACLE.get(),
            ModBlocks.ESSENCE_RECEPTACLE.get(),
            ModBlocks.VAPOR_PNEUMATIC_PIPE_COPPER.get(),
            ModBlocks.VAPOR_PNEUMATIC_VALVE.get(),
            ModBlocks.VAPOR_PNEUMATIC_ONE_WAY_VALVE.get()
        );

        // --- 7. Item Tint Sources ---
        registerLoomTintSource(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "ampoule_tint"), ModItemTintSources.AmpouleTint.MAP_CODEC);
        registerLoomTintSource(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "generator_tint"), ModItemTintSources.GeneratorTint.MAP_CODEC);
        registerLoomTintSource(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "fume_glass_tint"), ModItemTintSources.FumeGlassTint.MAP_CODEC);
        registerLoomTintSource(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "essence_tint"), ModItemTintSources.EssenceTint.MAP_CODEC);
        registerLoomTintSource(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "shard_tint"), ModItemTintSources.ShardTint.MAP_CODEC);
        registerLoomTintSource(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "aesthetic_glass_tint"), ModItemTintSources.AestheticGlassTint.MAP_CODEC);
        registerLoomTintSource(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "echo_fruit_tint"), ModItemTintSources.EchoFruitTint.MAP_CODEC);


        // --- 8. Special Model Renderer ---
        registerLoomSpecialModelRenderer(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "dynamic_weapon"), DynamicWeaponRenderer.Unbaked.MAP_CODEC);

        // --- 9. Custom Client Haze Shader Tick ---
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ddraig.net.entropica.client.HazeShaderManager.clientTick(client);
            ddraig.net.entropica.client.ParalyzedParticleHandler.clientTick(client);
        });

        // Register GUI Overlay HUD Renderer
        net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback.EVENT.register((guiGraphics, tickCounter) -> {
            ddraig.net.entropica.client.HazeOverlayRenderer.render(guiGraphics, tickCounter.getGameTimeDeltaTicks());
            ddraig.net.entropica.client.ParalyzedOverlayRenderer.render(guiGraphics, tickCounter.getGameTimeDeltaTicks());
            ddraig.net.entropica.client.LensOverlayRenderer.render(guiGraphics, tickCounter.getGameTimeDeltaTicks());
        });

        // Materia Yield Tooltip
        net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;
            if (!ddraig.net.entropica.client.LensOverlayRenderer.isMateriaVisionActive(mc.player)) return;
            if (stack.isEmpty()) return;

            java.util.List<ddraig.net.entropica.api.ItemEssenceMap.EssenceValue> values = ddraig.net.entropica.api.ItemEssenceMap.getEssenceFor(stack);
            if (values != null && !values.isEmpty()) {
                lines.add(net.minecraft.network.chat.Component.literal("§5Materia Yield:"));
                for (ddraig.net.entropica.api.ItemEssenceMap.EssenceValue val : values) {
                    String name = val.type().getColorCode() + val.type().getDisplayName();
                    lines.add(net.minecraft.network.chat.Component.literal(String.format("  §7- %.2f %s", val.amount(), name)));
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static void registerLoomTintSource(ResourceLocation id, MapCodec<? extends ItemTintSource> codec) {
        try {
            for (java.lang.reflect.Field field : net.minecraft.client.color.item.ItemTintSources.class.getDeclaredFields()) {
                if (field.getType().equals(net.minecraft.util.ExtraCodecs.LateBoundIdMapper.class)) {
                    field.setAccessible(true);
                    net.minecraft.util.ExtraCodecs.LateBoundIdMapper<ResourceLocation, MapCodec<? extends ItemTintSource>> mapper = 
                        (net.minecraft.util.ExtraCodecs.LateBoundIdMapper<ResourceLocation, MapCodec<? extends ItemTintSource>>) field.get(null);
                    mapper.put(id, codec);
                    return;
                }
            }
            Entropica.LOGGER.error("Failed to find ID_MAPPER field in ItemTintSources");
        } catch (Exception e) {
            Entropica.LOGGER.error("Failed to register item tint source via reflection", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static void registerLoomSpecialModelRenderer(ResourceLocation id, MapCodec<? extends net.minecraft.client.renderer.special.SpecialModelRenderer.Unbaked> codec) {
        try {
            for (java.lang.reflect.Field field : net.minecraft.client.renderer.special.SpecialModelRenderers.class.getDeclaredFields()) {
                if (field.getType().equals(net.minecraft.util.ExtraCodecs.LateBoundIdMapper.class)) {
                    field.setAccessible(true);
                    net.minecraft.util.ExtraCodecs.LateBoundIdMapper<ResourceLocation, MapCodec<? extends net.minecraft.client.renderer.special.SpecialModelRenderer.Unbaked>> mapper = 
                        (net.minecraft.util.ExtraCodecs.LateBoundIdMapper<ResourceLocation, MapCodec<? extends net.minecraft.client.renderer.special.SpecialModelRenderer.Unbaked>>) field.get(null);
                    mapper.put(id, codec);
                    return;
                }
            }
            Entropica.LOGGER.error("Failed to find ID_MAPPER field in SpecialModelRenderers");
        } catch (Exception e) {
            Entropica.LOGGER.error("Failed to register special model renderer via reflection", e);
        }
    }
}
