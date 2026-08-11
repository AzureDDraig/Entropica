package ddraig.net.entropica.neoforge;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.registry.ModBlocks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = Entropica.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Entropica.MODID, value = Dist.CLIENT)
public class EntropicaClient {
    public EntropicaClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.AEGIS_ROSE.get(), ChunkSectionLayer.CUTOUT);
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.TALL_AEGIS_ROSE.get(), ChunkSectionLayer.CUTOUT);
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.NECROTIC_ROSE_OF_JERICHO.get(), ChunkSectionLayer.CUTOUT);
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.TALL_NECROTIC_ROSE_OF_JERICHO.get(), ChunkSectionLayer.CUTOUT);
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.SANGUINE_LILY.get(), ChunkSectionLayer.CUTOUT);
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.AURORAL_LILY_PAD.get(), ChunkSectionLayer.CUTOUT);
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.CINDER_GRIP_LICHEN.get(), ChunkSectionLayer.CUTOUT);
            
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.SPORE_CANNON_PUFFBALL.get(), ChunkSectionLayer.CUTOUT);
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.PYRE_THORN_LAUNCHER.get(), ChunkSectionLayer.CUTOUT);
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.SPORE_BEARING_PITCHER_PLUMP.get(), ChunkSectionLayer.CUTOUT);
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.BLOOD_TENDRIL_BRAMBLE.get(), ChunkSectionLayer.CUTOUT);
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.SOOT_VEIL_BLIGHT_CAP.get(), ChunkSectionLayer.CUTOUT);
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.MAGMA_GRIP_TENDRILS.get(), ChunkSectionLayer.CUTOUT);
            
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.SOOT_SHROUD_FUNGI.get(), ChunkSectionLayer.CUTOUT);
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.PYROCYST_ALGAE.get(), ChunkSectionLayer.CUTOUT);
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.BARROW_FUNGAL_SHELF_CAP.get(), ChunkSectionLayer.CUTOUT);
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.SPORE_FUNGAL_SHELF_CAP.get(), ChunkSectionLayer.CUTOUT);
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.BLIGHT_FUNGAL_SHELF_CAP.get(), ChunkSectionLayer.CUTOUT);
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.FROST_FUNGAL_SHELF_CAP.get(), ChunkSectionLayer.CUTOUT);
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.CINDER_FUNGAL_SHELF_CAP.get(), ChunkSectionLayer.CUTOUT);
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.ASTRAL_FUNGAL_SHELF_CAP.get(), ChunkSectionLayer.CUTOUT);
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.DAWN_FUNGAL_SHELF_CAP.get(), ChunkSectionLayer.CUTOUT);
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.SANGUINE_FUNGAL_SHELF_CAP.get(), ChunkSectionLayer.CUTOUT);
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.STATIC_FUNGAL_SHELF_CAP.get(), ChunkSectionLayer.CUTOUT);
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.AEGIS_SPIRE_ORCHID.get(), ChunkSectionLayer.CUTOUT);
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.TALL_AEGIS_SPIRE_ORCHID.get(), ChunkSectionLayer.CUTOUT);
            
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.ASTRAL_VEIL_WILLOW_LEAVES.get(), ChunkSectionLayer.CUTOUT);
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.ASTRAL_VEIL_WILLOW_SAPLING.get(), ChunkSectionLayer.CUTOUT);
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.ASTRAL_VEIL_WILLOW_VINES.get(), ChunkSectionLayer.CUTOUT);
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.VOID_BLIGHT_MANGROVE_LEAVES.get(), ChunkSectionLayer.CUTOUT);
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.VOID_BLIGHT_MANGROVE_SAPLING.get(), ChunkSectionLayer.CUTOUT);
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.VOID_BLIGHT_MANGROVE_ROOT.get(), ChunkSectionLayer.CUTOUT);
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.PYRE_ASH_CEDAR_LEAVES.get(), ChunkSectionLayer.CUTOUT);
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.PYRE_ASH_CEDAR_SAPLING.get(), ChunkSectionLayer.CUTOUT);
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.ABYSSAL_SPORE_CYPRESS_LEAVES.get(), ChunkSectionLayer.CUTOUT);
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.ABYSSAL_SPORE_CYPRESS_SAPLING.get(), ChunkSectionLayer.CUTOUT);
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.STARLIGHT_AETHER_BIRCH_LEAVES.get(), ChunkSectionLayer.CUTOUT);
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.STARLIGHT_AETHER_BIRCH_SAPLING.get(), ChunkSectionLayer.CUTOUT);
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.BLOOD_ROOT_IRON_OAK_LEAVES.get(), ChunkSectionLayer.CUTOUT);
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.BLOOD_ROOT_IRON_OAK_SAPLING.get(), ChunkSectionLayer.CUTOUT);
        });
    }

    @SubscribeEvent
    static void onRegisterGuiLayers(net.neoforged.neoforge.client.event.RegisterGuiLayersEvent event) {
        event.registerAboveAll(
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "haze_overlay"),
            (guiGraphics, deltaTracker) -> {
                ddraig.net.entropica.client.HazeOverlayRenderer.render(guiGraphics, deltaTracker.getGameTimeDeltaTicks());
            }
        );
        event.registerAboveAll(
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "paralyzed_overlay"),
            (guiGraphics, deltaTracker) -> {
                ddraig.net.entropica.client.ParalyzedOverlayRenderer.render(guiGraphics, deltaTracker.getGameTimeDeltaTicks());
            }
        );
        event.registerAboveAll(
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "lens_overlay"),
            (guiGraphics, deltaTracker) -> {
                ddraig.net.entropica.client.LensOverlayRenderer.render(guiGraphics, deltaTracker.getGameTimeDeltaTicks());
            }
        );
    }
}
