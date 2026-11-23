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
import ddraig.net.entropica.client.gui.BarrierConfigScreen;
import ddraig.net.entropica.client.gui.SynthesizerUserInterfaceScreen;
import ddraig.net.entropica.client.gui.WeaponNamingScreen;
import ddraig.net.entropica.Entropica;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
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
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (ddraig.net.entropica.client.input.FirmamentWeaverClientHandler.handleMouseScroll(event.getScrollDeltaY())
                || ddraig.net.entropica.client.input.SpellApparatusClientHandler.handleMouseScroll(event.getScrollDeltaY())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        float partialTick = (float) event.getPartialTick();
        float roll = ddraig.net.entropica.client.camera.GravityCameraHandler.getRoll(partialTick);
        float yawSlant = ddraig.net.entropica.client.camera.GravityCameraHandler.getYawSlant(partialTick);
        float pitchSlant = ddraig.net.entropica.client.camera.GravityCameraHandler.getPitchSlant(partialTick);

        if (Math.abs(roll) > 0.001f) {
            event.setRoll(event.getRoll() + roll);
        }
        if (Math.abs(yawSlant) > 0.001f) {
            event.setYaw(event.getYaw() + yawSlant);
        }
        if (Math.abs(pitchSlant) > 0.001f) {
            event.setPitch(event.getPitch() + pitchSlant);
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        
        // Custom Haze shader tick manager
        HazeShaderManager.clientTick(mc);
        ddraig.net.entropica.client.ParalyzedParticleHandler.clientTick(mc);
        ddraig.net.entropica.client.camera.GravityCameraHandler.clientTick(mc);
        ddraig.net.entropica.client.gravity.GravitonSolesClientHandler.clientTick(mc);
        ddraig.net.entropica.spell.vfx.SpellWorldVfxManager.tickClient(mc.level != null);
        ddraig.net.entropica.spell.vfx.ArcaneWingsClientManager.tickClient(mc.level != null);
        ddraig.net.entropica.client.worldgen.BiomeAtmosphereClientManager.tickClient(mc);

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
        event.register(ModMenuTypes.BARRIER_CONFIG_MENU.get(), BarrierConfigScreen::new);
        event.register(ModMenuTypes.ARCANE_SCRIBING_TABLE_MENU.get(), ddraig.net.entropica.client.gui.ArcaneScribingTableScreen::new);
        event.register(ModMenuTypes.AUTOCAST_PEDESTAL_MENU.get(), ddraig.net.entropica.client.gui.AutocastPedestalScreen::new);
    }

    @SubscribeEvent
    public static void registerSpecialModels(RegisterSpecialModelRendererEvent event) {
        event.register(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "dynamic_weapon"), DynamicWeaponRenderer.Unbaked.MAP_CODEC);
        event.register(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "astral_crystal"), ddraig.net.entropica.client.renderer.item.AstralCrystalSpecialRenderer.Unbaked.MAP_CODEC);
        event.register(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "spell_custom_model"), ddraig.net.entropica.client.renderer.item.SpellCustomItemRenderer.Unbaked.MAP_CODEC);
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
        event.registerBlockEntityRenderer(ModBlockEntities.ASTRAL_ALTAR_CORE_BE.get(), ddraig.net.entropica.client.renderer.AstralAltarRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CELESTIAL_ARMILLARY_CONTROLLER_BE.get(), ddraig.net.entropica.client.renderer.CelestialArmillaryRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CELESTIAL_BEACON_CONTROLLER_BE.get(), ddraig.net.entropica.client.renderer.CelestialBeaconRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.OPTIC_TRANSMITTER_BE.get(), ddraig.net.entropica.client.renderer.OpticTransmitterRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CAGED_OPTIC_BULB_BE.get(), ddraig.net.entropica.client.renderer.CagedOpticBulbRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LAMP_POST_BE.get(), ddraig.net.entropica.client.renderer.LampPostRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.PURE_OPTIC_FIBER_BE.get(), ddraig.net.entropica.client.renderer.PureOpticFiberRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RESONANCE_STARLIGHT_FOUNTAIN_BE.get(), ddraig.net.entropica.client.renderer.ResonanceStarlightFountainRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ATTUNED_DISPLAY_VITRINE_BE.get(), ddraig.net.entropica.client.renderer.AttunedDisplayVitrineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CELESTIAL_GRANDFATHER_CLOCK_BE.get(), ddraig.net.entropica.client.renderer.CelestialGrandfatherClockRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ILLUMINATED_BALUSTRADE_BE.get(), ddraig.net.entropica.client.renderer.IlluminatedBalustradeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ASTRAL_CRYSTAL_BE.get(), ddraig.net.entropica.client.renderer.AstralCrystalRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.GRAVITATIONAL_ANCHOR_BE.get(), ddraig.net.entropica.client.renderer.GravitationalAnchorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.GRAV_LIFT_PROJECTOR_BE.get(), ddraig.net.entropica.client.renderer.GravLiftProjectorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.TIDAL_PULSE_RESONATOR_BE.get(), ddraig.net.entropica.client.renderer.TidalPulseResonatorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.GRAVITY_CENTER_BE.get(), ddraig.net.entropica.client.renderer.GravityCenterRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CREATIVE_GRAVITY_CENTER_BE.get(), ddraig.net.entropica.client.renderer.GravityCenterRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.GRAVITON_BOUNCEPAD_BE.get(), ddraig.net.entropica.client.renderer.GravitonBouncepadRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ARCANE_SCRIBING_TABLE_BE.get(), ddraig.net.entropica.client.renderer.SpellWorkstationBlockRenderer::createScribingTable);
        event.registerBlockEntityRenderer(ModBlockEntities.RUNIC_ANVIL_BE.get(), ddraig.net.entropica.client.renderer.SpellWorkstationBlockRenderer::createRunicAnvil);
        event.registerBlockEntityRenderer(ModBlockEntities.AUTOCAST_PEDESTAL_BE.get(), ddraig.net.entropica.client.renderer.SpellWorkstationBlockRenderer::createAutocastPedestal);

        event.registerEntityRenderer(ModEntityTypes.FORCEFIELD_BARRIER.get(), ddraig.net.entropica.client.renderer.forcefield.ForcefieldBarrierRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.SINGULARITY_GRENADE.get(), ddraig.net.entropica.client.renderer.SingularityGrenadeRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.ESSENCE_ORB.get(), EssenceOrbRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.ESSENCE_NODE.get(), EssenceNodeRenderer::new);

        event.registerEntityRenderer(ModEntityTypes.EIDOLIC_SHADOW.get(), EidolicShadowRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.SPELL_EIDOLON.get(), SpellEidolonRenderer::new);

        event.registerEntityRenderer(ModEntityTypes.GROT.get(), GrotRenderer::new);

        event.registerEntityRenderer(ModEntityTypes.VEIL_FOX.get(), VeilFoxRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.VEIL_FOX_AFTERIMAGE.get(), VeilFoxAfterimageRenderer::new);

        event.registerEntityRenderer(ModEntityTypes.ASHEN_STALKER.get(), ddraig.net.entropica.client.renderer.AshenStalkerRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.SPORE_DRIFTER.get(), SporeDrifterRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.SPORE_CLOUD.get(), SporeCloudRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.RIME_BACK_OVIS.get(), RimeBackOvisRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.OVERGROWTH_OVIS.get(), OvergrowthOvisRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.PATINA_OVIS.get(), PatinaOvisRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.RIME_SHEPHERD.get(), RimeShepherdRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.BLOOM_CRAWLER.get(), BloomCrawlerRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.AURORAFOWL.get(), AurorafowlRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.GEYSER_WIGGLE_WORM.get(), GeyserWiggleWormRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.CRYO_STALKER.get(), CryoStalkerRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.GEMINI_GOAT_MAGE.get(), ddraig.net.entropica.client.renderer.gemini_goat_mage.GeminiGoatMageRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.VOID_SEA_SERPENT.get(), ddraig.net.entropica.client.renderer.void_sea_serpent.VoidSeaSerpentRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.VOID_SEA_SERPENT_SEGMENT.get(), net.minecraft.client.renderer.entity.NoopRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.GLACIAL_HYDRA.get(), ddraig.net.entropica.client.renderer.glacial_hydra.GlacialHydraRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.LUMINOTH.get(), ddraig.net.entropica.client.renderer.luminoth.LuminothRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.AMBER_WEEPING_STAG.get(), ddraig.net.entropica.client.renderer.amber_weeping_stag.AmberWeepingStagRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.STORM_KITE.get(), ddraig.net.entropica.client.renderer.storm_kite.StormKiteRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.GLACIAL_FROST_CORE.get(), ddraig.net.entropica.client.renderer.frost_core.GlacialFrostCoreRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.FROST_TUSK_MAMMOT.get(), ddraig.net.entropica.client.renderer.mammot.FrostTuskMammotRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.AURORA_JELLY.get(), ddraig.net.entropica.client.renderer.aurora_jelly.AuroraJellyRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.VAPOR_BELLOWS_CRAB.get(), ddraig.net.entropica.client.renderer.vapor_bellows_crab.VaporBellowsCrabRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.CRYO_STATIC_HEDGEHOG.get(), ddraig.net.entropica.client.renderer.cryo_static_hedgehog.CryoStaticHedgehogRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.BARROW_HOUND.get(), ddraig.net.entropica.client.renderer.BarrowHoundRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.BARROW_CRYPT_GLOW_OWL.get(), ddraig.net.entropica.client.renderer.BarrowCryptGlowOwlRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.VOID_RIFT_SHADE.get(), ddraig.net.entropica.client.renderer.VoidRiftShadeRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.CRYSTALLINE_QILIN.get(), ddraig.net.entropica.client.renderer.CrystallineQilinRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.SANDSTONE_GOLEM.get(), ddraig.net.entropica.client.renderer.SandstoneGolemRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.PEAT_MUD_KELPIE.get(), ddraig.net.entropica.client.renderer.PeatMudKelpieRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.SILT_SPHINX.get(), ddraig.net.entropica.client.renderer.SiltSphinxRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.ZEPHYR_SIMURGH.get(), ddraig.net.entropica.client.renderer.ZephyrSimurghRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MIRAGE_FATA_SPRITE.get(), ddraig.net.entropica.client.renderer.MirageFataSpriteRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MIRAGE_FATA_AFTERIMAGE.get(), ddraig.net.entropica.client.renderer.MirageFataSpriteRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MAGMA_CHERUFE.get(), ddraig.net.entropica.client.renderer.MagmaCherufeRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.SOULFIRE_PHENEX.get(), ddraig.net.entropica.client.renderer.SoulfirePhenexRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.VOID_ARACHNE.get(), ddraig.net.entropica.client.renderer.VoidArachneRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.ASH_BARROW_GNOME.get(), ddraig.net.entropica.client.renderer.AshBarrowGnomeRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.ORTHRUS_MAGMA_BEAST.get(), ddraig.net.entropica.client.renderer.OrthrusMagmaBeastRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.ASH_CINDER_WISP.get(), ddraig.net.entropica.client.renderer.AshCinderWispRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.SOUL_SAND_HYDRA.get(), ddraig.net.entropica.client.renderer.SoulSandHydraRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.VOID_SHADOW_GARGOYLE.get(), ddraig.net.entropica.client.renderer.VoidShadowGargoyleRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.BARROW_WIGHT_GOLEM.get(), ddraig.net.entropica.client.renderer.BarrowWightGolemRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.OBSIDIAN_SCORPIOID.get(), ddraig.net.entropica.client.renderer.ObsidianScorpioidRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.BRIMSTONE_WYVERN.get(), ddraig.net.entropica.client.renderer.BrimstoneWyvernRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.OBSIDIAN_GOLEM.get(), ddraig.net.entropica.client.renderer.ObsidianGolemRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.SOULFIRE_DJINN.get(), ddraig.net.entropica.client.renderer.SoulfireDjinnRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.BRIMSTONE_CHIMERA.get(), ddraig.net.entropica.client.renderer.BrimstoneChimeraRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.OBSIDIAN_WENDIGO.get(), ddraig.net.entropica.client.renderer.ObsidianWendigoRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.RADIANT_FIRE_LANCE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.SPELL_PROJECTILE.get(), ddraig.net.entropica.client.renderer.SpellProjectileRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.UMBRAL_VORTEX.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.CRYO_GRENADE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.RADIANT_SNOWBALL.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MYCELIAL_SPORE_WEAVER.get(), ddraig.net.entropica.client.renderer.mycelial_spore_weaver.MycelialSporeWeaverRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.GLAZED_SPORE_BOMB.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.SILICA_GLASS_SCARAB.get(), ddraig.net.entropica.client.renderer.silica_glass_scarab.SilicaGlassScarabRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MIRAGE_DECOY_PRISM.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MUD_LURKER_SALAMANDER.get(), ddraig.net.entropica.client.renderer.mud_lurker_salamander.MudLurkerSalamanderRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.DUST_CORE_GOLEM.get(), ddraig.net.entropica.client.renderer.dust_core_golem.DustCoreGolemRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.SANGUINE_SEA_ANGEL.get(), ddraig.net.entropica.client.renderer.sanguine_sea_angel.SanguineSeaAngelRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.BLOOD_FROST_DART.get(), ddraig.net.entropica.client.renderer.projectile.BloodFrostDartRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.STARLIGHT_NAUTILUS.get(), ddraig.net.entropica.client.renderer.starlight_nautilus.StarlightNautilusRenderer::new);
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.FUME_PARTICLE.get(), FumeParticle.Provider::new);
        event.registerSpriteSet(ModParticles.SPECTRUM_SPARKLE.get(), ddraig.net.entropica.client.particle.SpectrumSparkleParticle.Provider::new);
        event.registerSpriteSet(ModParticles.GALE_SWIRL_PUFF.get(), ddraig.net.entropica.client.particle.GaleSwirlPuffParticle.Provider::new);
        event.registerSpriteSet(ModParticles.SHADOW_SLASH.get(), ddraig.net.entropica.client.particle.ShadowSlashParticle.Provider::new);
        for (var sup : ddraig.net.entropica.spell.vfx.SpellParticleHelper.getAllTintableParticles()) {
            event.registerSpriteSet(sup.get(), ddraig.net.entropica.client.particle.TimedTintableParticle.Provider::new);
        }
        for (var sup : ddraig.net.entropica.spell.vfx.SpellParticleHelper.getAllRuneParticles()) {
            event.registerSpriteSet(sup.get(), ddraig.net.entropica.client.particle.TimedTintableParticle.Provider::new);
        }
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
        event.registerLayerDefinition(AurorafowlModel.LAYER_LOCATION, AurorafowlModel::createBodyLayer);
        event.registerLayerDefinition(GeyserWiggleWormModel.LAYER_LOCATION, GeyserWiggleWormModel::createBodyLayer);
        event.registerLayerDefinition(CryoStalkerModel.LAYER_LOCATION, CryoStalkerModel::createBodyLayer);
        event.registerLayerDefinition(ddraig.net.entropica.client.renderer.gemini_goat_mage.GeminiGoatMageModel.LAYER_LOCATION, ddraig.net.entropica.client.renderer.gemini_goat_mage.GeminiGoatMageModel::createBodyLayer);
        event.registerLayerDefinition(ddraig.net.entropica.client.renderer.void_sea_serpent.VoidSeaSerpentModel.LAYER_LOCATION, ddraig.net.entropica.client.renderer.void_sea_serpent.VoidSeaSerpentModel::createBodyLayer);
        event.registerLayerDefinition(ddraig.net.entropica.client.renderer.glacial_hydra.GlacialHydraModel.LAYER_LOCATION, ddraig.net.entropica.client.renderer.glacial_hydra.GlacialHydraModel::createBodyLayer);
        event.registerLayerDefinition(ddraig.net.entropica.client.renderer.luminoth.LuminothModel.LAYER_LOCATION, ddraig.net.entropica.client.renderer.luminoth.LuminothModel::createBodyLayer);
        event.registerLayerDefinition(ddraig.net.entropica.client.renderer.amber_weeping_stag.AmberWeepingStagModel.LAYER_LOCATION, ddraig.net.entropica.client.renderer.amber_weeping_stag.AmberWeepingStagModel::createBodyLayer);
        event.registerLayerDefinition(ddraig.net.entropica.client.renderer.storm_kite.StormKiteModel.LAYER_LOCATION, ddraig.net.entropica.client.renderer.storm_kite.StormKiteModel::createBodyLayer);
        event.registerLayerDefinition(ddraig.net.entropica.client.renderer.frost_core.GlacialFrostCoreModel.LAYER_LOCATION, ddraig.net.entropica.client.renderer.frost_core.GlacialFrostCoreModel::createBodyLayer);
        event.registerLayerDefinition(ddraig.net.entropica.client.renderer.mammot.FrostTuskMammotModel.LAYER_LOCATION, ddraig.net.entropica.client.renderer.mammot.FrostTuskMammotModel::createBodyLayer);
        event.registerLayerDefinition(ddraig.net.entropica.client.renderer.aurora_jelly.AuroraJellyModel.LAYER_LOCATION, ddraig.net.entropica.client.renderer.aurora_jelly.AuroraJellyModel::createBodyLayer);
        event.registerLayerDefinition(ddraig.net.entropica.client.renderer.vapor_bellows_crab.VaporBellowsCrabModel.LAYER_LOCATION, ddraig.net.entropica.client.renderer.vapor_bellows_crab.VaporBellowsCrabModel::createBodyLayer);
        event.registerLayerDefinition(ddraig.net.entropica.client.renderer.cryo_static_hedgehog.CryoStaticHedgehogModel.LAYER_LOCATION, ddraig.net.entropica.client.renderer.cryo_static_hedgehog.CryoStaticHedgehogModel::createBodyLayer);
        event.registerLayerDefinition(ddraig.net.entropica.client.model.BarrowHoundModel.MODEL_LOCATION, ddraig.net.entropica.client.model.BarrowHoundModel::createBodyLayer);
        event.registerLayerDefinition(ddraig.net.entropica.client.model.BarrowCryptGlowOwlModel.MODEL_LOCATION, ddraig.net.entropica.client.model.BarrowCryptGlowOwlModel::createBodyLayer);
        event.registerLayerDefinition(ddraig.net.entropica.client.model.VoidRiftShadeModel.MODEL_LOCATION, ddraig.net.entropica.client.model.VoidRiftShadeModel::createBodyLayer);
        event.registerLayerDefinition(ddraig.net.entropica.client.model.CrystallineQilinModel.MODEL_LOCATION, ddraig.net.entropica.client.model.CrystallineQilinModel::createBodyLayer);
        event.registerLayerDefinition(ddraig.net.entropica.client.model.SandstoneGolemModel.MODEL_LOCATION, ddraig.net.entropica.client.model.SandstoneGolemModel::createBodyLayer);
        event.registerLayerDefinition(ddraig.net.entropica.client.model.PeatMudKelpieModel.MODEL_LOCATION, ddraig.net.entropica.client.model.PeatMudKelpieModel::createBodyLayer);
        event.registerLayerDefinition(ddraig.net.entropica.client.model.SiltSphinxModel.MODEL_LOCATION, ddraig.net.entropica.client.model.SiltSphinxModel::createBodyLayer);
        event.registerLayerDefinition(ddraig.net.entropica.client.model.ZephyrSimurghModel.MODEL_LOCATION, ddraig.net.entropica.client.model.ZephyrSimurghModel::createBodyLayer);
        event.registerLayerDefinition(ddraig.net.entropica.client.model.MirageFataSpriteModel.MODEL_LOCATION, ddraig.net.entropica.client.model.MirageFataSpriteModel::createBodyLayer);
        event.registerLayerDefinition(ddraig.net.entropica.client.model.MagmaCherufeModel.MODEL_LOCATION, ddraig.net.entropica.client.model.MagmaCherufeModel::createBodyLayer);
        event.registerLayerDefinition(ddraig.net.entropica.client.model.SoulfirePhenexModel.MODEL_LOCATION, ddraig.net.entropica.client.model.SoulfirePhenexModel::createBodyLayer);
        event.registerLayerDefinition(ddraig.net.entropica.client.model.VoidArachneModel.MODEL_LOCATION, ddraig.net.entropica.client.model.VoidArachneModel::createBodyLayer);
        event.registerLayerDefinition(ddraig.net.entropica.client.model.AshBarrowGnomeModel.MODEL_LOCATION, ddraig.net.entropica.client.model.AshBarrowGnomeModel::createBodyLayer);
        event.registerLayerDefinition(ddraig.net.entropica.client.model.OrthrusMagmaBeastModel.MODEL_LOCATION, ddraig.net.entropica.client.model.OrthrusMagmaBeastModel::createBodyLayer);
        event.registerLayerDefinition(ddraig.net.entropica.client.model.AshCinderWispModel.MODEL_LOCATION, ddraig.net.entropica.client.model.AshCinderWispModel::createBodyLayer);
        event.registerLayerDefinition(ddraig.net.entropica.client.model.SoulSandHydraModel.MODEL_LOCATION, ddraig.net.entropica.client.model.SoulSandHydraModel::createBodyLayer);
        event.registerLayerDefinition(ddraig.net.entropica.client.model.VoidShadowGargoyleModel.MODEL_LOCATION, ddraig.net.entropica.client.model.VoidShadowGargoyleModel::createBodyLayer);
        event.registerLayerDefinition(ddraig.net.entropica.client.model.BarrowWightGolemModel.MODEL_LOCATION, ddraig.net.entropica.client.model.BarrowWightGolemModel::createBodyLayer);
        event.registerLayerDefinition(ddraig.net.entropica.client.model.ObsidianScorpioidModel.MODEL_LOCATION, ddraig.net.entropica.client.model.ObsidianScorpioidModel::createBodyLayer);
        event.registerLayerDefinition(ddraig.net.entropica.client.model.BrimstoneWyvernModel.MODEL_LOCATION, ddraig.net.entropica.client.model.BrimstoneWyvernModel::createBodyLayer);
        event.registerLayerDefinition(ddraig.net.entropica.client.model.ObsidianGolemModel.MODEL_LOCATION, ddraig.net.entropica.client.model.ObsidianGolemModel::createBodyLayer);
        event.registerLayerDefinition(ddraig.net.entropica.client.model.SoulfireDjinnModel.MODEL_LOCATION, ddraig.net.entropica.client.model.SoulfireDjinnModel::createBodyLayer);
        event.registerLayerDefinition(ddraig.net.entropica.client.model.BrimstoneChimeraModel.MODEL_LOCATION, ddraig.net.entropica.client.model.BrimstoneChimeraModel::createBodyLayer);
        event.registerLayerDefinition(ddraig.net.entropica.client.model.ObsidianWendigoModel.MODEL_LOCATION, ddraig.net.entropica.client.model.ObsidianWendigoModel::createBodyLayer);
        event.registerLayerDefinition(ddraig.net.entropica.client.renderer.mycelial_spore_weaver.MycelialSporeWeaverModel.LAYER_LOCATION, ddraig.net.entropica.client.renderer.mycelial_spore_weaver.MycelialSporeWeaverModel::createBodyLayer);
        event.registerLayerDefinition(ddraig.net.entropica.client.renderer.silica_glass_scarab.SilicaGlassScarabModel.LAYER_LOCATION, ddraig.net.entropica.client.renderer.silica_glass_scarab.SilicaGlassScarabModel::createBodyLayer);
        event.registerLayerDefinition(ddraig.net.entropica.client.renderer.mud_lurker_salamander.MudLurkerSalamanderModel.LAYER_LOCATION, ddraig.net.entropica.client.renderer.mud_lurker_salamander.MudLurkerSalamanderModel::createBodyLayer);
        event.registerLayerDefinition(ddraig.net.entropica.client.renderer.dust_core_golem.DustCoreGolemModel.LAYER_LOCATION, ddraig.net.entropica.client.renderer.dust_core_golem.DustCoreGolemModel::createBodyLayer);
        event.registerLayerDefinition(ddraig.net.entropica.client.renderer.sanguine_sea_angel.SanguineSeaAngelModel.LAYER_LOCATION, ddraig.net.entropica.client.renderer.sanguine_sea_angel.SanguineSeaAngelModel::createBodyLayer);
        event.registerLayerDefinition(ddraig.net.entropica.client.renderer.starlight_nautilus.StarlightNautilusModel.LAYER_LOCATION, ddraig.net.entropica.client.renderer.starlight_nautilus.StarlightNautilusModel::createBodyLayer);
        event.registerLayerDefinition(ddraig.net.entropica.client.renderer.starlight_nautilus.StarlightNautilusModel.BABY_LAYER_LOCATION, ddraig.net.entropica.client.renderer.starlight_nautilus.StarlightNautilusModel::createBabyBodyLayer);
        event.registerLayerDefinition(ddraig.net.entropica.client.renderer.starlight_nautilus.StarlightNautilusModel.SADDLE_LAYER_LOCATION, ddraig.net.entropica.client.renderer.starlight_nautilus.StarlightNautilusModel::createSaddleLayer);
        event.registerLayerDefinition(ddraig.net.entropica.client.renderer.starlight_nautilus.StarlightNautilusModel.ARMOR_LAYER_LOCATION, ddraig.net.entropica.client.renderer.starlight_nautilus.StarlightNautilusModel::createArmorLayer);
    }

    @SubscribeEvent
    public static void onClientSetupBiome1RenderLayers(net.neoforged.fml.event.lifecycle.FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.AEGIS_CRYSTAL_CLUSTER.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT);
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.GLACIAL_HEART_ROOT.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT);
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.SHIELDING_BRAMBLE.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT);
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.THORNED_WIRE.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT);
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.RUNIC_BLUE_ICE.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.TRANSLUCENT);
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.GLACIAL_BARRICADE.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.TRANSLUCENT);

            // Biome 2 (2.2 Auroral Skerries) Cutout & Translucent Render Layers
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.AURORAL_LILY_PAD.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT);
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.AURORAL_FEATHER_MOSS.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT);
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.MIST_VEIL_FERN.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT);
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.POTTED_MIST_VEIL_FERN.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT);
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.AURORAL_LANTERN.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT);
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.AURORAL_MOTE_LIGHT.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT);
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.GLIMMER_ICE.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.TRANSLUCENT);
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.AURORAL_LENS_BLOCK.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.TRANSLUCENT);
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.GLOW_GEL_PATHING.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.TRANSLUCENT);

            // Biome 3 (2.3 Frost-Bite Floes) Cutout & Translucent Render Layers
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.NECROTIC_SLIME_PATCH.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT);
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.FROZEN_SPRUCE_BRANCH.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT);
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.ROTTEN_ICE.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.TRANSLUCENT);
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.NECROTIC_SLIME_BLOCK.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.TRANSLUCENT);

            // Biome 4 (2.4 Crystalline Pack-Ice) Cutout Render Layers
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.AEGIS_QUARTZ_CLUSTER.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT);
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.BISMUTH_KEY_PEDESTAL.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT);

            // Biome 5 (2.5 Silent Fjord) Cutout Render Layers
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.BLUE_FROZEN_MOSS_CARPET.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT);
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.SHADOWED_ECLIPSE_MOSS.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT);

            // Biome 6 (2.6 Steaming Skerries) Cutout & Translucent Render Layers
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.STEAMING_ICE.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.TRANSLUCENT);
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.VAPOR_WEAVER_LICHEN.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT);
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.VAPOR_CONDENSER.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT);
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.VAPOR_LIFT_MAT.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT);

            // Biome 7 (2.7 Static Ice-Plains) Cutout & Translucent Render Layers
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.FULGURITE_ICE.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.TRANSLUCENT);
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.FULGURITE_PINE_LEAVES.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT);
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.STATIC_CONDUCTIVE_MAT.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT);
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.BIOMASS_CAPACITOR.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT);

            // Biome 8 (2.8 Barrow-Mound Tundra) Cutout Render Layers
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.GRAVE_MOSS.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT);
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.CRYPT_GRAVE_ROOT.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT);
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.RIME_CAP_MUSHROOM.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT);
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.POTTED_RIME_CAP_MUSHROOM.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT);
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.RUNED_BRAZIER.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT);
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.CRYPT_SIPHON_LANTERN.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT);
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.BARROW_SENTINEL_TOTEM.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT);

            // Biome 9 (2.9 Null-Fringe Bay) Cutout Render Layers
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.VOID_CRYSTAL_BUD.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT);
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.VOID_CRYSTAL_CLUSTER.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT);
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.NULL_FRINGE_TUSSOCK.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT);
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.POTTED_NULL_FRINGE_TUSSOCK.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT);
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.SPATIAL_PHASE_ANCHOR.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT);

            // Biome 10 (2.10 Radiant Glaze-Plains) Translucent & Cutout Render Layers
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.RADIANT_GLAZE_ICE.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.TRANSLUCENT);
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.GLAZED_AMBER_LEAVES.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT);
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.GOLDEN_LICHEN.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT);
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.QILIN_LIGHT_REFRACTOR.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT);
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.SUN_GLAZED_LANTERN.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT);

            // Biome 11 (2.11 Storm-Swept Icebergs) Translucent & Cutout Render Layers
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.ZEPHYR_ICE.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.TRANSLUCENT);
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.ZEPHYR_GLASS_GRASS.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT);
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.POTTED_ZEPHYR_GLASS_GRASS.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT);
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.ZEPHYR_UPDRAFT_GRATE.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT);
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.SIMURGH_WIND_TURBINE.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT);

            // Biome 12 (2.12 Spore-Glazed Pack) Translucent & Cutout Render Layers
            net.minecraft.core.registries.BuiltInRegistries.BLOCK
                    .getOptional(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "spore_glazed_ice"))
                    .ifPresent(b -> net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(b, net.minecraft.client.renderer.chunk.ChunkSectionLayer.TRANSLUCENT));
            for (String cutoutId : new String[]{
                    "frozen_spore_leaves",
                    "frozen_spore_moss_carpet",
                    "spore_bellows",
                    "cryo_spore_censer",
                    "frozen_spore_sapling",
                    "potted_frozen_spore_sapling"
            }) {
                net.minecraft.core.registries.BuiltInRegistries.BLOCK
                        .getOptional(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, cutoutId))
                        .ifPresent(b -> net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(b, net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT));
            }

            // Biome 13 (2.13 Mirage Glacier) Translucent & Cutout Render Layers
            net.minecraft.core.registries.BuiltInRegistries.BLOCK
                    .getOptional(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "mirage_ice"))
                    .ifPresent(b -> net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(b, net.minecraft.client.renderer.chunk.ChunkSectionLayer.TRANSLUCENT));
            for (String cutoutId : new String[]{
                    "mirage_prism_lantern",
                    "mirage_glass_grass",
                    "potted_mirage_glass_grass",
                    "solar_mirage_flora"
            }) {
                net.minecraft.core.registries.BuiltInRegistries.BLOCK
                        .getOptional(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, cutoutId))
                        .ifPresent(b -> net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(b, net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT));
            }

            // Biome 14 (2.14 Oasis Floes) Translucent & Cutout Render Layers
            for (String translucentId : new String[]{
                    "amber_resin_block",
                    "oasis_dew_basin"
            }) {
                net.minecraft.core.registries.BuiltInRegistries.BLOCK
                        .getOptional(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, translucentId))
                        .ifPresent(b -> net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(b, net.minecraft.client.renderer.chunk.ChunkSectionLayer.TRANSLUCENT));
            }
            for (String cutoutId : new String[]{
                    "frost_willow_leaves",
                    "frost_willow_vines",
                    "amber_torch",
                    "wall_amber_torch"
            }) {
                net.minecraft.core.registries.BuiltInRegistries.BLOCK
                        .getOptional(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, cutoutId))
                        .ifPresent(b -> net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(b, net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT));
            }

            // Biome 15 (2.15 Dust-Crusted Bergs) Cutout Render Layers
            for (String cutoutId : new String[]{
                    "wind_blown_dust_scrub",
                    "tough_fiber_rope",
                    "wind_baffle_lantern"
            }) {
                net.minecraft.core.registries.BuiltInRegistries.BLOCK
                        .getOptional(ResourceLocation.fromNamespaceAndPath(Entropica.MODID, cutoutId))
                        .ifPresent(b -> net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(b, net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT));
            }

            // Biome 16 (2.16 Crimson Tide-Skerries) Translucent & Cutout Render Layers
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.SANGUINE_ICE.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.TRANSLUCENT);
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.RED_ALGAE.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT);
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.CRIMSON_TIDE_LANTERN.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT);

            // Biome 17 (2.17 Astral Skerries) Translucent & Cutout Render Layers
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.STARLIGHT_ICE.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.TRANSLUCENT);
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.STARDUST_CRYSTAL_CLUSTER.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT);
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.RIME_STAR_TUSSOCK.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT);
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.POTTED_RIME_STAR_TUSSOCK.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT);
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(ModBlocks.CELESTIAL_ORRERY_LANTERN.get(), net.minecraft.client.renderer.chunk.ChunkSectionLayer.CUTOUT);
        });
    }

    private static void unusedAnchor() {
    }



    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        Player player = event.getEntity();
        if (player == null) return;
        ddraig.net.entropica.spell.SpellTooltipFormatter.appendEngravedEquipmentTooltip(event.getItemStack(), event.getToolTip());
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
            ddraig.net.entropica.client.renderer.SpellWorldVfxRenderer.renderWorldVfx(
                poseStack,
                event.getModelViewMatrix(),
                mc.gameRenderer.getMainCamera(),
                mc.getDeltaTracker().getGameTimeDeltaTicks()
            );
            ddraig.net.entropica.client.worldgen.BiomeSkyPhenomenaRenderer.renderSkyPhenomena(
                poseStack,
                event.getModelViewMatrix(),
                mc.gameRenderer.getMainCamera(),
                mc.getDeltaTracker().getGameTimeDeltaTicks()
            );
            ddraig.net.entropica.client.worldgen.BiomeWaterSurfaceRenderer.renderWaterOptics(
                poseStack,
                event.getModelViewMatrix(),
                mc.gameRenderer.getMainCamera(),
                mc.getDeltaTracker().getGameTimeDeltaTicks()
            );
            ddraig.net.entropica.client.worldgen.BiomeVolumetricFogRenderer.renderVolumetricFog(
                poseStack,
                event.getModelViewMatrix(),
                mc.gameRenderer.getMainCamera(),
                mc.getDeltaTracker().getGameTimeDeltaTicks()
            );
            ddraig.net.entropica.client.renderer.DoubleVisionClientRenderer.renderWorldDoubleVision(
                poseStack,
                mc.gameRenderer.getMainCamera(),
                mc.getDeltaTracker().getGameTimeDeltaTicks()
            );
            ddraig.net.entropica.client.renderer.forcefield.FirmamentHologramPreviewRenderer.renderPreview(
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
            ddraig.net.entropica.client.gui.BarrierTelemetryHudOverlay.render(event.getGuiGraphics(), partialTick);
            ddraig.net.entropica.client.renderer.GravityScreenShimmerRenderer.render(event.getGuiGraphics(), partialTick);
            ddraig.net.entropica.client.renderer.GravitationalLensingScreenOverlay.render(event.getGuiGraphics(), partialTick);
            ddraig.net.entropica.client.renderer.DoubleVisionClientRenderer.renderHudDoubleVision(event.getGuiGraphics(), partialTick);
        }
    }
}