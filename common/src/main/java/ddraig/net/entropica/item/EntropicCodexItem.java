package ddraig.net.entropica.item;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.client.gui.EntropicCodexScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

public class EntropicCodexItem extends Item {

    public EntropicCodexItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        String blockId = level.getBlockState(pos).getBlock().getDescriptionId();
        if (blockId.contains("materia_terminal") || blockId.contains("aetheric_terminal") || blockId.contains("terminal")) {
            CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
                tag.putLong("PairedTerminalPos", pos.asLong());
            });

            if (!level.isClientSide() && player != null) {
                player.displayClientMessage(Component.literal("§a[Entropic Codex] Paired to Materia Terminal at " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()), true);

                ((ServerLevel) level).sendParticles(ParticleTypes.ENCHANT, 
                        pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 
                        25, 0.3, 0.3, 0.3, 0.2);
                level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.8F, 1.5F);
            }
            return InteractionResult.SUCCESS;
        }

        return super.useOn(context);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            EssenceType highestEssence = scanHighestEssence(player);
            openCodexScreen(highestEssence);
        } else {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), 
                    SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        return InteractionResult.SUCCESS;
    }

    public static EssenceType scanHighestEssence(Player player) {
        EssenceType best = null;
        int maxTier = -1;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack invStack = player.getInventory().getItem(i);
            if (!invStack.isEmpty() && invStack.getItem() instanceof EssenceItem) {
                EssenceType type = EssenceItem.getEssenceType(invStack);
                int tier = getEssenceTier(type);
                if (tier > maxTier) {
                    maxTier = tier;
                    best = type;
                }
            }
        }
        return best;
    }

    public static int getGemColor(EssenceType type) {
        if (type == null) return 0xFF1A1A24;
        return switch (type) {
            case IGNIS, MAGMA, FERVOR, HEAT, NETHER -> 0xFFFF4500;
            case WATER, FROZEN, GLACIAL, STORM, FLOW -> 0xFF00D9FF;
            case AIR, RADIANT, LIGHTNING, KINETIC, VOLT -> 0xFFFFD700;
            case EARTH, NATURE, OVERGROWTH, SPORE, GROWTH -> 0xFF00FF44;
            case UMBRAL, VOID, ASTRAL, PENUMBRA, SILENCE, SHADOW -> 0xFFAA00FF;
            case VITAE, BLOOD, AURA, AMBER -> 0xFFDC143C;
            default -> 0xFF1A1A24;
        };
    }

    private static int getEssenceTier(EssenceType type) {
        if (type == null) return 0;
        return switch (type) {
            case VITAE, BLOOD -> 1;
            case IGNIS, WATER, AIR, EARTH, NATURE -> 2;
            case UMBRAL, VOID -> 3;
            default -> 1;
        };
    }

    private static void openCodexScreen(EssenceType essenceType) {
        Minecraft.getInstance().setScreen(new EntropicCodexScreen(essenceType));
    }

    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("§7Technomantic Guidebook & Knowledge Vault"));
        tooltipComponents.add(Component.literal("§dRight-click§7 to open spatial research map."));
        tooltipComponents.add(Component.literal("§dRight-click Materia Terminal§7 to pair facility telemetry."));
    }
}
