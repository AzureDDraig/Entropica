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
}