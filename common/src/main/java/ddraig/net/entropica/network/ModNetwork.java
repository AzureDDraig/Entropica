package ddraig.net.entropica.network;

import ddraig.net.entropica.block.entity.AethericSynthesizerBlockEntity;
import ddraig.net.entropica.block.entity.CreativeMateriaGeneratorBlockEntity;
import ddraig.net.entropica.block.entity.CreativeParticleGeneratorBlockEntity;
import ddraig.net.entropica.component.VisWeaponState;
import ddraig.net.entropica.item.DynamicVisWeaponItem;
import ddraig.net.entropica.registry.ModDataComponents;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ModNetwork {

    public static void register() {
        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                GeneratorScrollPayload.TYPE,
                GeneratorScrollPayload.STREAM_CODEC,
                ModNetwork::handleGeneratorScroll
        );

        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                ParticleGeneratorScrollPayload.TYPE,
                ParticleGeneratorScrollPayload.STREAM_CODEC,
                ModNetwork::handleParticleGeneratorScroll
        );

        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                TerminalCraftPayload.TYPE,
                TerminalCraftPayload.STREAM_CODEC,
                ModNetwork::handleTerminalCraft
        );

        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                WeaponNamingPayload.TYPE,
                WeaponNamingPayload.STREAM_CODEC,
                ModNetwork::handleNaming
        );

        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                MonocleLensSwapPayload.TYPE,
                MonocleLensSwapPayload.STREAM_CODEC,
                ModNetwork::handleMonocleLensSwap
        );

        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                TelescopeAimPayload.TYPE,
                TelescopeAimPayload.STREAM_CODEC,
                ModNetwork::handleTelescopeAim
        );

        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                AstralLensAimPayload.TYPE,
                AstralLensAimPayload.STREAM_CODEC,
                ModNetwork::handleAstralLensAim
        );

        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                SyncChartedConnectionsPayload.TYPE,
                SyncChartedConnectionsPayload.STREAM_CODEC,
                ModNetwork::handleSyncChartedConnections
        );

        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                ConstellationDiscoveryPayload.TYPE,
                ConstellationDiscoveryPayload.STREAM_CODEC,
                ModNetwork::handleConstellationDiscovery
        );
    }

    public static void handleGeneratorScroll(final GeneratorScrollPayload data, final NetworkManager.PacketContext context) {
        context.queue(() -> {
            Player player = context.getPlayer();
            if (player != null && player.level().isLoaded(data.pos())) {
                BlockEntity be = player.level().getBlockEntity(data.pos());
                if (be instanceof CreativeMateriaGeneratorBlockEntity gen) {
                    gen.cycleType(data.delta());
                }
            }
        });
    }

    public static void handleParticleGeneratorScroll(final ParticleGeneratorScrollPayload data, final NetworkManager.PacketContext context) {
        context.queue(() -> {
            Player player = context.getPlayer();
            if (player != null && player.level().isLoaded(data.pos())) {
                BlockEntity be = player.level().getBlockEntity(data.pos());
                if (be instanceof CreativeParticleGeneratorBlockEntity gen) {
                    gen.cycleParticle(data.delta());
                }
            }
        });
    }

    public static void handleTerminalCraft(final TerminalCraftPayload data, final NetworkManager.PacketContext context) {
        context.queue(() -> {
            Player player = context.getPlayer();
            if (player != null && player.level().isLoaded(data.pos())) {
                BlockEntity be = player.level().getBlockEntity(data.pos());
                if (be instanceof AethericSynthesizerBlockEntity synth) {
                    synth.instantCraft(data.isBulk());
                }
            }
        });
    }

    public static void handleNaming(final WeaponNamingPayload data, final NetworkManager.PacketContext context) {
        context.queue(() -> {
            Player player = context.getPlayer();
            if (player == null) return;

            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);

                if (stack.getItem() instanceof DynamicVisWeaponItem && stack.has(ModDataComponents.VIS_WEAPON_STATE.get())) {
                    VisWeaponState state = stack.get(ModDataComponents.VIS_WEAPON_STATE.get());

                    if (state != null && !state.hasInitializedName()) {

                        String finalName = data.name().trim();

                        if (finalName.isEmpty()) {
                            String coreTier = "Base";
                            if (state.innateSlots() >= 9) coreTier = "Ancient";
                            else if (state.innateSlots() >= 6) coreTier = "Charged";

                            String material = state.materialTier();

                            String toolType = "Weapon";
                            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
                            if (itemId != null) {
                                String path = itemId.getPath();
                                if (path.contains("sword")) toolType = "Sword";
                                else if (path.contains("axe") && !path.contains("pickaxe")) toolType = "Axe";
                                else if (path.contains("pickaxe")) toolType = "Pickaxe";
                            }

                            String rawEssence = state.baseType().name();
                            String essence = rawEssence.substring(0, 1).toUpperCase() + rawEssence.substring(1).toLowerCase();

                            finalName = coreTier + " " + material + " " + toolType + " of " + essence;
                        }

                        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.literal(finalName));
                        stack.set(ModDataComponents.VIS_WEAPON_STATE.get(), state.withInitializedName(true));

                        break;
                    }
                }
            }
        });
    }

    public static void handleMonocleLensSwap(final MonocleLensSwapPayload data, final NetworkManager.PacketContext context) {
        context.queue(() -> {
            Player player = context.getPlayer();
            if (player == null) return;
            ItemStack head = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD);
            if (head.is(ddraig.net.entropica.registry.ModItems.ARKANIST_MONOCLE.get())) {
                ddraig.net.entropica.item.ArkanistMonocleItem.cycleLens(head, player);
            }
        });
    }

    public static void handleTelescopeAim(final TelescopeAimPayload data, final NetworkManager.PacketContext context) {
        context.queue(() -> {
            Player player = context.getPlayer();
            if (player != null && player.level().isLoaded(data.pos())) {
                BlockEntity be = player.level().getBlockEntity(data.pos());
                if (be instanceof ddraig.net.entropica.block.entity.StationaryBrassTelescopeBlockEntity telescope) {
                    telescope.setAngles(data.yaw(), data.pitch());
                }
            }
        });
    }

    public static void handleAstralLensAim(final AstralLensAimPayload data, final NetworkManager.PacketContext context) {
        context.queue(() -> {
            Player player = context.getPlayer();
            if (player != null && player.level().isLoaded(data.pos())) {
                BlockEntity be = player.level().getBlockEntity(data.pos());
                if (be instanceof ddraig.net.entropica.block.entity.RefractiveAstralLensBlockEntity lens) {
                    lens.setFocus(data.yaw(), data.pitch(), data.targetName(), data.isFocused());
                }
            }
        });
    }

    public static void handleSyncChartedConnections(final SyncChartedConnectionsPayload data, final NetworkManager.PacketContext context) {
        context.queue(() -> {
            Player player = context.getPlayer();
            if (player != null) {
                if (data.action() == SyncChartedConnectionsPayload.ACTION_CLEAR) {
                    ddraig.net.entropica.astral.PlayerAstralProgress.clearChartedConnections(player);
                } else if (data.action() == SyncChartedConnectionsPayload.ACTION_REMOVE) {
                    if (data.edges() != null) {
                        for (String edge : data.edges()) {
                            ddraig.net.entropica.astral.PlayerAstralProgress.removeChartedConnection(player, edge);
                        }
                    }
                } else if (data.edges() != null) {
                    for (String edge : data.edges()) {
                        ddraig.net.entropica.astral.PlayerAstralProgress.addChartedConnection(player, edge);
                    }
                }
            }
        });
    }

    public static void handleConstellationDiscovery(final ConstellationDiscoveryPayload data, final NetworkManager.PacketContext context) {
        context.queue(() -> {
            Player player = context.getPlayer();
            if (player != null && data.constellationId() != null) {
                ddraig.net.entropica.astral.Constellation constellation = ddraig.net.entropica.astral.ModConstellations.getById(data.constellationId()).orElse(null);
                if (constellation != null) {
                    ddraig.net.entropica.astral.PlayerAstralProgress.discover(player, constellation);

                    // Safely check inventory on server thread
                    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                        net.minecraft.world.item.ItemStack stack = player.getInventory().getItem(i);
                        if (stack.is(ddraig.net.entropica.registry.ModItems.STAR_CHART_BLANK.get())) {
                            stack.shrink(1);
                            net.minecraft.world.item.ItemStack completedChart = ddraig.net.entropica.item.CompletedStarChartItem.createFor(ddraig.net.entropica.registry.ModItems.STAR_CHART_COMPLETED.get(), constellation);
                            if (!player.getInventory().add(completedChart)) {
                                player.drop(completedChart, false);
                            }
                            break;
                        }
                    }
                }
            }
        });
    }
}