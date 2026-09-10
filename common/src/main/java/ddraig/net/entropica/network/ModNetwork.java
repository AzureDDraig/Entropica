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

        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                ReinscribeStarChartPayload.TYPE,
                ReinscribeStarChartPayload.STREAM_CODEC,
                ModNetwork::handleReinscribeStarChart
        );

        NetworkManager.registerReceiver(
                NetworkManager.Side.S2C,
                SyncAstralProgressPayload.TYPE,
                SyncAstralProgressPayload.STREAM_CODEC,
                ModNetwork::handleSyncAstralProgress
        );

        NetworkManager.registerReceiver(
                NetworkManager.Side.S2C,
                SyncSupernovaPayload.TYPE,
                SyncSupernovaPayload.STREAM_CODEC,
                ModNetwork::handleSyncSupernova
        );

        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                AstrolabeScrollPayload.TYPE,
                AstrolabeScrollPayload.STREAM_CODEC,
                ModNetwork::handleAstrolabeScroll
        );

        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                GravityFlipPayload.TYPE,
                GravityFlipPayload.STREAM_CODEC,
                ModNetwork::handleGravityFlip
        );

        NetworkManager.registerReceiver(
                NetworkManager.Side.S2C,
                BarrierImpactPayload.TYPE,
                BarrierImpactPayload.STREAM_CODEC,
                ModNetwork::handleBarrierImpact
        );

        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                UpdateBarrierConfigPayload.TYPE,
                UpdateBarrierConfigPayload.STREAM_CODEC,
                ModNetwork::handleUpdateBarrierConfig
        );

        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                WeaverScrollPayload.TYPE,
                WeaverScrollPayload.STREAM_CODEC,
                ModNetwork::handleWeaverScroll
        );
    }

    public static void handleBarrierImpact(final BarrierImpactPayload data, final NetworkManager.PacketContext context) {
        context.queue(() -> {
            Player player = context.getPlayer();
            if (player != null && player.level() != null) {
                net.minecraft.world.entity.Entity entity = player.level().getEntity(data.entityId());
                if (entity instanceof ddraig.net.entropica.entity.forcefield.ForcefieldBarrierEntity barrier) {
                    barrier.addRipple(new net.minecraft.world.phys.Vec3(data.x(), data.y(), data.z()), data.intensity());
                }
            }
        });
    }

    public static void handleUpdateBarrierConfig(final UpdateBarrierConfigPayload data, final NetworkManager.PacketContext context) {
        context.queue(() -> {
            Player player = context.getPlayer();
            if (player != null && player.level() != null) {
                net.minecraft.world.entity.Entity entity = player.level().getEntity(data.entityId());
                if (entity instanceof ddraig.net.entropica.entity.forcefield.ForcefieldBarrierEntity barrier) {
                    if (!barrier.isAlive() || barrier.isRemoved() || barrier.distanceToSqr(player) > 64.0 * 64.0) {
                        return;
                    }
                    boolean isOwner = barrier.getOwnerUUID().map(u -> u.equals(player.getUUID())).orElse(false);
                    if (!isOwner && !player.isCreative()) {
                        player.displayClientMessage(Component.literal("§cOnly the creator can modify this barrier!"), true);
                        return;
                    }
                    if (barrier.isBossEncounter() && !player.isCreative()) {
                        player.displayClientMessage(Component.literal("§cThis barrier is bound to an active Apex Predator and cannot be modified!"), true);
                        return;
                    }

                    barrier.setShape(ddraig.net.entropica.forcefield.BarrierShape.fromOrdinal(data.shapeOrdinal()));
                    float safeWidth = Float.isFinite(data.width()) ? Math.max(1.0F, Math.min(32.0F, data.width())) : 4.0F;
                    float safeHeight = Float.isFinite(data.height()) ? Math.max(1.0F, Math.min(32.0F, data.height())) : 4.0F;
                    float safeRadius = Float.isFinite(data.radius()) ? Math.max(1.0F, Math.min(32.0F, data.radius())) : 4.0F;
                    float safeElasticity = Float.isFinite(data.elasticity()) ? Math.max(0.20F, Math.min(2.00F, data.elasticity())) : 1.0F;
                    barrier.setWidth(safeWidth);
                    barrier.setHeight(safeHeight);
                    barrier.setRadius(safeRadius);
                    barrier.setFilterMode(ddraig.net.entropica.forcefield.BarrierFilterMode.fromOrdinal(data.filterModeOrdinal()));
                    barrier.setPredatorTheme(ddraig.net.entropica.forcefield.ApexPredatorTheme.fromOrdinal(data.themeOrdinal()));
                    barrier.setBounceElasticity(safeElasticity);
                    barrier.setOneWay(data.oneWay());
                    barrier.setRedstoneMode(Math.max(0, Math.min(2, data.redstoneMode())));
                    if (data.colorTint() != 0) {
                        barrier.setColorTint(data.colorTint());
                    }
                    if (data.whitelistUsernames() != null) {
                        barrier.setWhitelistUsernames(data.whitelistUsernames(), player.level().getServer());
                    }

                    player.displayClientMessage(Component.literal("§aBarrier configuration updated successfully!"), true);
                }
            }
        });
    }

    public static void handleGravityFlip(final GravityFlipPayload data, final NetworkManager.PacketContext context) {
        context.queue(() -> {
            Player player = context.getPlayer();
            if (player != null) {
                if (data.inverted()) {
                    ddraig.net.entropica.gravity.GravityApi.SOLES_INVERTED_ENTITIES.add(player.getUUID());
                    ddraig.net.entropica.gravity.GravityApi.setGravity(player, data.targetGravity());
                } else {
                    ddraig.net.entropica.gravity.GravityApi.SOLES_INVERTED_ENTITIES.remove(player.getUUID());
                    ddraig.net.entropica.gravity.GravityApi.resetGravity(player);
                }
            }
        });
    }

    public static void handleAstrolabeScroll(final AstrolabeScrollPayload data, final NetworkManager.PacketContext context) {
        context.queue(() -> {
            Player player = context.getPlayer();
            if (player != null) {
                ItemStack stack = player.getMainHandItem();
                if (!stack.is(ddraig.net.entropica.registry.ModItems.ASTROLABE.get())) {
                    stack = player.getOffhandItem();
                }
                if (stack.is(ddraig.net.entropica.registry.ModItems.ASTROLABE.get())) {
                    net.minecraft.world.item.component.CustomData customData = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY);
                    net.minecraft.nbt.CompoundTag tag = customData.copyTag();
                    int activeSlot = ddraig.net.entropica.item.AstrolabeItem.getActiveSlot(tag);
                    ddraig.net.entropica.item.AstrolabeItem.saveCurrentSlotToNbt(tag, activeSlot);

                    int nextSlot = (activeSlot + (data.delta() > 0 ? 1 : 2)) % 3;
                    ddraig.net.entropica.item.AstrolabeItem.loadSlotFromNbt(tag, nextSlot);
                    stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));

                    int nextBp = tag.getInt(ddraig.net.entropica.item.AstrolabeItem.TAG_BLUEPRINT_INDEX).orElse(0);
                    boolean nextHasAnchor = tag.getBoolean(ddraig.net.entropica.item.AstrolabeItem.TAG_HAS_ANCHOR).orElse(false);
                    net.minecraft.core.BlockPos anchorPos = nextHasAnchor ? new net.minecraft.core.BlockPos(tag.getInt(ddraig.net.entropica.item.AstrolabeItem.TAG_ANCHOR_X).orElse(0), tag.getInt(ddraig.net.entropica.item.AstrolabeItem.TAG_ANCHOR_Y).orElse(0), tag.getInt(ddraig.net.entropica.item.AstrolabeItem.TAG_ANCHOR_Z).orElse(0)) : null;
                    int dist = (nextHasAnchor) ? (int) Math.sqrt(player.blockPosition().distSqr(anchorPos)) : 0;

                    player.playNotifySound(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(), net.minecraft.sounds.SoundSource.PLAYERS, 0.8f, 1.4f);
                    String status = nextHasAnchor ? (" §a(" + anchorPos.toShortString() + " - " + dist + "m)") : " §8(Unanchored)";
                    player.displayClientMessage(Component.literal("§6[Astrolabe Memory] §7Active Slot: §b[" + (nextSlot + 1) + "/3] §7- Blueprint: §f" + ddraig.net.entropica.item.AstrolabeItem.BLUEPRINT_NAMES[nextBp] + status), true);
                }
            }
        });
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
                } else if (be instanceof ddraig.net.entropica.block.entity.SecondaryAstralLensBlockEntity secondaryLens) {
                    secondaryLens.setFocus(data.yaw(), data.pitch(), data.targetName(), data.isFocused());
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

    public static void handleReinscribeStarChart(final ReinscribeStarChartPayload data, final NetworkManager.PacketContext context) {
        context.queue(() -> {
            Player player = context.getPlayer();
            if (player != null && data.constellationId() != null) {
                ddraig.net.entropica.astral.Constellation constellation = ddraig.net.entropica.astral.ModConstellations.getById(data.constellationId()).orElse(null);
                if (constellation != null) {
                    boolean hasBlank = player.isCreative();
                    int blankSlot = -1;

                    if (!hasBlank) {
                        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                            net.minecraft.world.item.ItemStack stack = player.getInventory().getItem(i);
                            if (stack.is(ddraig.net.entropica.registry.ModItems.STAR_CHART_BLANK.get())) {
                                hasBlank = true;
                                blankSlot = i;
                                break;
                            }
                        }
                    }

                    if (hasBlank) {
                        if (!player.isCreative() && blankSlot >= 0) {
                            player.getInventory().getItem(blankSlot).shrink(1);
                        }

                        net.minecraft.world.item.ItemStack completedChart = ddraig.net.entropica.item.CompletedStarChartItem.createFor(ddraig.net.entropica.registry.ModItems.STAR_CHART_COMPLETED.get(), constellation);
                        if (!player.getInventory().add(completedChart)) {
                            player.drop(completedChart, false);
                        }

                        player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.2f);
                        String name = net.minecraft.network.chat.Component.translatable(constellation.getUnlocalizedName()).getString();
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("§6[Celestial Atlas] §7Reinscribed Star Chart for §b" + name), true);
                    } else {
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("§c[Celestial Atlas] You need a Blank Star Chart in your inventory to reinscribe."), true);
                    }
                }
            }
        });
    }

    public static void handleSyncAstralProgress(final SyncAstralProgressPayload data, final NetworkManager.PacketContext context) {
        context.queue(() -> {
            ddraig.net.entropica.astral.PlayerAstralProgress.setClientProgress(data.discoveredConstellations(), data.chartedConnections());
        });
    }

    public static void handleSyncSupernova(final SyncSupernovaPayload data, final NetworkManager.PacketContext context) {
        context.queue(() -> {
            ddraig.net.entropica.astral.SupernovaManager.setClientEvents(data.events());
        });
    }

    public static void handleWeaverScroll(final WeaverScrollPayload data, final NetworkManager.PacketContext context) {
        context.queue(() -> {
            Player player = context.getPlayer();
            if (player != null) {
                ItemStack stack = player.getMainHandItem();
                if (!stack.is(ddraig.net.entropica.registry.ModItems.FIRMAMENT_WEAVER.get())) {
                    stack = player.getOffhandItem();
                }
                if (stack.is(ddraig.net.entropica.registry.ModItems.FIRMAMENT_WEAVER.get())) {
                    if (!ddraig.net.entropica.item.FirmamentWeaverItem.hasAnchor(stack)) {
                        ddraig.net.entropica.item.FirmamentWeaverItem.cycleShape(stack, data.delta());
                    }
                }
            }
        });
    }
}