package ddraig.net.entropica.network;

import ddraig.net.entropica.block.entity.AethericSynthesizerBlockEntity;
import ddraig.net.entropica.block.entity.CreativeVisFumeGeneratorBlockEntity;
import ddraig.net.entropica.component.VisWeaponState;
import ddraig.net.entropica.item.DynamicVisWeaponItem;
import ddraig.net.entropica.registry.ModDataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ModNetwork {

    public static void register(RegisterPayloadHandlersEvent event) {
        // Register the Generator Scroll Payload
        event.registrar("1.0").playToServer(
                GeneratorScrollPayload.TYPE,
                GeneratorScrollPayload.STREAM_CODEC,
                ModNetwork::handleGeneratorScroll
        );

        // Register the Synthesizer UI Crafting Payload
        event.registrar("1.0").playToServer(
                TerminalCraftPayload.TYPE,
                TerminalCraftPayload.STREAM_CODEC,
                ModNetwork::handleTerminalCraft
        );

        // Register the Weapon Naming Payload
        event.registrar("1.0").playToServer(
                WeaponNamingPayload.TYPE,
                WeaponNamingPayload.STREAM_CODEC,
                ModNetwork::handleNaming
        );
    }

    public static void handleGeneratorScroll(final GeneratorScrollPayload data, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player != null && player.level().isLoaded(data.pos())) {
                BlockEntity be = player.level().getBlockEntity(data.pos());
                if (be instanceof CreativeVisFumeGeneratorBlockEntity gen) {
                    gen.cycleType(data.delta());
                }
            }
        });
    }

    public static void handleTerminalCraft(final TerminalCraftPayload data, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player != null && player.level().isLoaded(data.pos())) {
                BlockEntity be = player.level().getBlockEntity(data.pos());
                if (be instanceof AethericSynthesizerBlockEntity synth) {
                    synth.instantCraft(data.isBulk());
                }
            }
        });
    }

    public static void handleNaming(final WeaponNamingPayload data, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player == null) return;

            // Search the player's inventory for the first un-named Eidolic weapon
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);

                if (stack.getItem() instanceof DynamicVisWeaponItem && stack.has(ModDataComponents.VIS_WEAPON_STATE.get())) {
                    VisWeaponState state = stack.get(ModDataComponents.VIS_WEAPON_STATE.get());

                    if (state != null && !state.hasInitializedName()) {

                        String finalName = data.name().trim();

                        // IF EMPTY: GENERATE DEFAULT DYNAMIC NAME
                        if (finalName.isEmpty()) {

                            // 1. Determine Core Tier from Augment Slots
                            String coreTier = "Base";
                            if (state.innateSlots() >= 9) coreTier = "Ancient";
                            else if (state.innateSlots() >= 6) coreTier = "Charged";

                            // 2. Material Type
                            String material = state.materialTier();

                            // 3. Weapon/Tool Type
                            String toolType = "Weapon";
                            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
                            if (itemId != null) {
                                String path = itemId.getPath();
                                if (path.contains("sword")) toolType = "Sword";
                                else if (path.contains("axe") && !path.contains("pickaxe")) toolType = "Axe";
                                else if (path.contains("pickaxe")) toolType = "Pickaxe";
                            }

                            // 4. Essence Type (Properly Capitalized)
                            String rawEssence = state.baseType().name();
                            String essence = rawEssence.substring(0, 1).toUpperCase() + rawEssence.substring(1).toLowerCase();

                            // Combine into final default format!
                            finalName = coreTier + " " + material + " " + toolType + " of " + essence;
                        }

                        // Apply the Name and flip the initialization flag so it never asks again
                        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.literal(finalName));
                        stack.set(ModDataComponents.VIS_WEAPON_STATE.get(), state.withInitializedName(true));

                        break; // Only process one weapon per packet!
                    }
                }
            }
        });
    }
}