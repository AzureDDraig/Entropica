package ddraig.net.entropica.client;

import com.mojang.blaze3d.platform.InputConstants;
import ddraig.net.entropica.client.particle.FumeParticle;
import ddraig.net.entropica.client.renderer.*;
import ddraig.net.entropica.registry.ModParticles;
import net.minecraft.client.KeyMapping;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.item.ManaAmpouleItem;
import ddraig.net.entropica.registry.ModBlockEntities;
import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModEntityTypes;
import ddraig.net.entropica.block.entity.CreativeVisFumeGeneratorBlockEntity;
import ddraig.net.entropica.block.entity.ManaEnrichedGlassBlockEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

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

    // --- DEBUG TOGGLE ---
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

    // Note: Key input usually needs to be on the FORGE bus, not the MOD bus,
    // but leaving it here as it was in your original file.
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (DEBUG_KEY.consumeClick()) {
            debugMode = !debugMode;
            Minecraft.getInstance().gui.setOverlayMessage(
                    Component.literal("Entropica Debug: " + (debugMode ? "§aON" : "§cOFF")),
                    false);
        }
    }

    // --- RENDERER REGISTRATION ---
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.MANA_FURNACE_BE.get(), ManaFurnaceRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ENTROPIC_CORE_BE.get(), EntropicCoreRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ESSENCE_READOUT_BE.get(), EssenceReadoutRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MANA_READOUT_BE.get(), ManaReadoutRenderer::new);

        // --- VIS FUME NETWORK RENDERERS ---
        event.registerBlockEntityRenderer(ModBlockEntities.VIS_FUME_PIPE_BE.get(), VisFumePipeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.VIS_FUME_VALVE_BE.get(), VisFumePipeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.VIS_FUME_ONE_WAY_VALVE_BE.get(), VisFumePipeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.VIS_FUME_DIVERTER_BE.get(), VisFumePipeRenderer::new);

        // --- VESSEL RENDERERS ---
        event.registerBlockEntityRenderer(ModBlockEntities.VIS_FUME_VESSEL_CONTROLLER_BE.get(), VisFumeVesselControllerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MANA_ENRICHED_GLASS_BE.get(), ManaEnrichedGlassRenderer::new);

        // --- CHAMBER RENDERERS ---
        event.registerBlockEntityRenderer(ModBlockEntities.ENRICHMENT_TABLE_BE.get(), EnrichmentTableRenderer::new);

        event.registerEntityRenderer(ModEntityTypes.ESSENCE_ORB.get(), EssenceOrbRenderer::new);
    }

    // --- CUSTOM PARTICLE REGISTRATION ---
    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.FUME_PARTICLE.get(), FumeParticle.Provider::new);
    }

    // --- BLOCK COLORS ---
    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        // Generator Tint
        event.register((state, level, pos, tintIndex) -> {
            if (level != null && pos != null && tintIndex == 0) {
                if (level.getBlockEntity(pos) instanceof CreativeVisFumeGeneratorBlockEntity generator) {
                    return generator.getCurrentType().getColorInt();
                }
            }
            return 0xFFFFFF;
        }, ModBlocks.CREATIVE_VIS_FUME_GENERATOR.get());

        // Glass Frame Tint
        event.register((state, level, pos, tintIndex) -> {
                    if (level != null && pos != null && tintIndex == 0) {
                        if (level.getBlockEntity(pos) instanceof ManaEnrichedGlassBlockEntity glassBE) {
                            EssenceType type = glassBE.getEssenceType();
                            if (type != null) {
                                return type.getColorInt();
                            }
                        }
                    }
                    return 0xFFFFFF; // Default white if empty
                },
                ModBlocks.FRAGMENT_LATTICE_GLASS.get(),
                ModBlocks.VIS_ICHOR_ENRICHED_GLASS.get(),
                ModBlocks.VIS_FUME_STRENGTHENED_GLASS.get(),
                ModBlocks.ESSENCE_ENRICHED_GLASS.get());
    }

    // --- 1.21.2+ DATA-DRIVEN ITEM TINTING SYSTEM ---

    public record AmpouleTint() implements ItemTintSource {
        public static final MapCodec<AmpouleTint> MAP_CODEC = MapCodec.unit(new AmpouleTint());

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
            EssenceType type = null;
            if (stack.getItem() instanceof ManaAmpouleItem) {
                type = ManaAmpouleItem.getEssenceType(stack);
            }
            if (type == null) {
                String itemName = net.minecraft.core.registries.BuiltInRegistries.ITEM
                        .getKey(stack.getItem()).getPath();
                for (EssenceType essence : EssenceType.values()) {
                    if (itemName.contains(essence.name().toLowerCase())) {
                        type = essence;
                        break;
                    }
                }
            }
            return type != null ? (0xFF000000 | type.getColorInt()) : 0xFFFFFFFF;
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

    // NEW: Glass Item Tint Source
    public record FumeGlassTint() implements ItemTintSource {
        public static final MapCodec<FumeGlassTint> MAP_CODEC = MapCodec.unit(new FumeGlassTint());

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
            String itemName = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
            for (EssenceType essence : EssenceType.values()) {
                if (itemName.contains(essence.name().toLowerCase())) {
                    return 0xFF000000 | essence.getColorInt();
                }
            }
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
    }
}