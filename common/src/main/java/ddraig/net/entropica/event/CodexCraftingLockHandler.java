package ddraig.net.entropica.event;

import ddraig.net.entropica.codex.CodexCategoryRegistry;
import ddraig.net.entropica.codex.CodexCategoryRegistry.CodexNode;
import ddraig.net.entropica.config.EntropicaConfig;
import ddraig.net.entropica.data.CodexPlayerData;
import ddraig.net.entropica.registry.ModItems;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class CodexCraftingLockHandler {

    private static final Map<Item, String> ITEM_TO_NODE_MAP = new HashMap<>();
    private static boolean initialized = false;

    public static void register() {
        PlayerEvent.CRAFT_ITEM.register((player, stack, container) -> {
            if (player == null || stack.isEmpty()) return;

            if (!Boolean.TRUE.equals(EntropicaConfig.REQUIRE_RESEARCH_TO_CRAFT.get())) {
                return;
            }

            CodexNode node = getRequiredNodeForStack(stack);
            if (node != null) {
                boolean isLocked = CodexPlayerData.CLIENT_DATA.isNodeLocked(node.id)
                        || node.requiredTier > CodexPlayerData.CLIENT_DATA.getResearchTierLevel()
                        || (node.prerequisiteId != null && CodexPlayerData.CLIENT_DATA.isNodeLocked(node.prerequisiteId));

                if (isLocked) {
                    stack.setCount(0);
                    player.displayClientMessage(
                            Component.literal("§cCrafting Locked: Requires research node '" + node.title + "' in Entropic Codex!"),
                            true
                    );
                }
            }
        });
    }

    private static synchronized void ensureMappingsInitialized() {
        if (initialized) return;

        ITEM_TO_NODE_MAP.clear();
        try {
            if (ModItems.AETHERIC_MONOCLE.isBound()) ITEM_TO_NODE_MAP.put(ModItems.AETHERIC_MONOCLE.get(), "arcanist_monocle");
            if (ModItems.ARKANIST_MONOCLE.isBound()) ITEM_TO_NODE_MAP.put(ModItems.ARKANIST_MONOCLE.get(), "arcanist_monocle");
            if (ModItems.ARCANITE_INGOT.isBound()) ITEM_TO_NODE_MAP.put(ModItems.ARCANITE_INGOT.get(), "materials_arcanite");
            if (ModItems.ARCANITE_WIRE.isBound()) ITEM_TO_NODE_MAP.put(ModItems.ARCANITE_WIRE.get(), "materials_arcanite");
            if (ModItems.ARCANITE_PLATE.isBound()) ITEM_TO_NODE_MAP.put(ModItems.ARCANITE_PLATE.get(), "materials_arcanite");
            if (ModItems.VISCANITE_INGOT.isBound()) ITEM_TO_NODE_MAP.put(ModItems.VISCANITE_INGOT.get(), "materials_viscanite");
            if (ModItems.RESONITE_INGOT.isBound()) ITEM_TO_NODE_MAP.put(ModItems.RESONITE_INGOT.get(), "materials_viscanite");
            if (ModItems.EIDOLON_PATHMARKER.isBound()) ITEM_TO_NODE_MAP.put(ModItems.EIDOLON_PATHMARKER.get(), "workstations_lathe");
            if (ModItems.EIDOLIC_FOCAL_PEDESTAL_ITEM.isBound()) ITEM_TO_NODE_MAP.put(ModItems.EIDOLIC_FOCAL_PEDESTAL_ITEM.get(), "workstations_lathe");
            if (ModItems.SCRIBING_CONTROLLER_ITEM.isBound()) ITEM_TO_NODE_MAP.put(ModItems.SCRIBING_CONTROLLER_ITEM.get(), "multiblock_scribing_engine");
        } catch (Exception e) {
            // Safe fallback if any item is not bound
        }
        initialized = true;
    }

    public static CodexNode getRequiredNodeForStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;

        ensureMappingsInitialized();

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());

        // Strictly enforce namespace check - NEVER lock vanilla items!
        if (!"entropica".equals(id.getNamespace())) {
            return null;
        }

        // Check explicit item mapping
        String nodeId = ITEM_TO_NODE_MAP.get(stack.getItem());
        if (nodeId != null) {
            return CodexCategoryRegistry.getNodeById(nodeId);
        }

        // Fallback path-to-node matching for Entropica items
        String path = id.getPath();
        for (CodexNode node : CodexCategoryRegistry.ALL_NODES) {
            if (path.equals(node.id) || node.id.endsWith(path)) {
                return node;
            }
        }

        return null;
    }
}
