package ddraig.net.entropica.item;

import ddraig.net.entropica.data.CelestialJournalData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Handheld Scholar's Journal item representing one of the 13 Celestial Lore Journals
 * from the Entropica archives (Volumes I - XIII), complete with interactive chapter entries
 * and persistent reading bookmark state.
 */
public class CelestialJournalItem extends Item {

    public static final String TAG_LAST_ENTRY = "JournalLastEntry";
    public static final String TAG_READ_ENTRIES = "JournalReadEntries";

    private final int volumeNumber;

    public CelestialJournalItem(Properties properties, int volumeNumber) {
        super(properties);
        this.volumeNumber = volumeNumber;
    }

    public int getVolumeNumber() {
        return this.volumeNumber;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            ClientScreenBridge.open(stack, this.volumeNumber);
        } else {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 0.9F, 1.05F);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);

        CelestialJournalData.JournalVolume vol = CelestialJournalData.getVolume(this.volumeNumber);
        if (!vol.subtitle().isEmpty()) {
            tooltipComponents.accept(Component.literal(vol.subtitle())
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
        if (!vol.author().isEmpty()) {
            tooltipComponents.accept(Component.literal("✦ " + vol.author())
                    .withStyle(ChatFormatting.GOLD));
        }

        int totalEntries = vol.entries().size();
        Set<Integer> read = getReadEntries(stack);
        int lastEntry = getLastEntryIndex(stack);

        tooltipComponents.accept(Component.literal("Entries: ")
                .withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal(totalEntries + " Chapters").withStyle(ChatFormatting.AQUA))
                .append(Component.literal("  |  Read: ").withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.literal(Math.min(read.size(), totalEntries) + " / " + totalEntries).withStyle(ChatFormatting.WHITE)));

        if (lastEntry >= 0 && lastEntry < totalEntries) {
            String bookmarkTitle = vol.entries().get(lastEntry).title();
            tooltipComponents.accept(Component.literal("Bookmark: ")
                    .withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal(bookmarkTitle).withStyle(ChatFormatting.YELLOW)));
        }

        tooltipComponents.accept(Component.literal("Right-click to read journal entries")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    public static int getLastEntryIndex(ItemStack stack) {
        CustomData custom = stack.get(DataComponents.CUSTOM_DATA);
        if (custom == null) return 0;
        CompoundTag tag = custom.copyTag();
        return Math.max(0, tag.getInt(TAG_LAST_ENTRY).orElse(0));
    }

    public static Set<Integer> getReadEntries(ItemStack stack) {
        Set<Integer> out = new HashSet<>();
        CustomData custom = stack.get(DataComponents.CUSTOM_DATA);
        if (custom == null) return out;
        CompoundTag tag = custom.copyTag();
        tag.getIntArray(TAG_READ_ENTRIES).ifPresent(arr -> {
            for (int v : arr) {
                out.add(v);
            }
        });
        return out;
    }

    public static void updateClientBookmark(ItemStack stack, int entryIndex) {
        Set<Integer> read = getReadEntries(stack);
        read.add(entryIndex);
        int[] readArr = read.stream().mapToInt(Integer::intValue).sorted().toArray();
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putInt(TAG_LAST_ENTRY, entryIndex);
            tag.put(TAG_READ_ENTRIES, new IntArrayTag(readArr));
        });
    }

    private static final class ClientScreenBridge {
        private static void open(ItemStack stack, int volumeNumber) {
            ddraig.net.entropica.client.gui.CelestialJournalScreen.open(stack, volumeNumber);
        }
    }
}
