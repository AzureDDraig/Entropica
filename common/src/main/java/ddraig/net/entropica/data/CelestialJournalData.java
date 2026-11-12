package ddraig.net.entropica.data;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ddraig.net.entropica.Entropica;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads and caches the 13 Scholar's Journals (Celestial Tomes Volumes I - XIII)
 * and all 233 of their chapter entries from bundled JSON assets.
 */
public final class CelestialJournalData {

    public static final int TOTAL_VOLUMES = 13;
    private static final Gson GSON = new Gson();
    private static final Map<Integer, JournalVolume> CACHE = new ConcurrentHashMap<>();

    private CelestialJournalData() {
    }

    public record EntryBlock(String type, String text) {
    }

    public record JournalEntry(String title, String part, List<EntryBlock> blocks) {
    }

    public record JournalVolume(
            int volume,
            String roman,
            String id,
            String title,
            String shortTitle,
            String subtitle,
            String author,
            List<String> epigraph,
            int guiCoverColor,
            int guiTrimColor,
            int guiAccentColor,
            List<JournalEntry> entries
    ) {
    }

    public static JournalVolume getVolume(int volumeNumber) {
        int clamped = Math.max(1, Math.min(TOTAL_VOLUMES, volumeNumber));
        return CACHE.computeIfAbsent(clamped, CelestialJournalData::loadVolumeFromResources);
    }

    private static JournalVolume loadVolumeFromResources(int volumeNumber) {
        String path = "/assets/entropica/journals/vol" + volumeNumber + ".json";
        try (InputStream in = CelestialJournalData.class.getResourceAsStream(path)) {
            if (in == null) {
                Entropica.LOGGER.error("[Entropica] Missing journal resource: {}", path);
                return createFallbackVolume(volumeNumber);
            }
            try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                JsonObject root = GSON.fromJson(reader, JsonObject.class);
                int vol = root.has("volume") ? root.get("volume").getAsInt() : volumeNumber;
                String roman = getString(root, "roman", String.valueOf(volumeNumber));
                String id = getString(root, "id", "celestial_journal_vol" + volumeNumber);
                String title = getString(root, "title", "Volume " + roman);
                String shortTitle = getString(root, "shortTitle", title);
                String subtitle = getString(root, "subtitle", "");
                String author = getString(root, "author", "");

                List<String> epigraph = new ArrayList<>();
                if (root.has("epigraph") && root.get("epigraph").isJsonArray()) {
                    for (JsonElement el : root.getAsJsonArray("epigraph")) {
                        epigraph.add(el.getAsString());
                    }
                }

                int coverCol = root.has("guiCoverColor") ? (int) root.get("guiCoverColor").getAsLong() : 0xFF1A2842;
                int trimCol = root.has("guiTrimColor") ? (int) root.get("guiTrimColor").getAsLong() : 0xFFDAAF4C;
                int accentCol = root.has("guiAccentColor") ? (int) root.get("guiAccentColor").getAsLong() : 0xFF4CE2FF;

                List<JournalEntry> entries = new ArrayList<>();
                if (root.has("entries") && root.get("entries").isJsonArray()) {
                    JsonArray arr = root.getAsJsonArray("entries");
                    for (JsonElement entryEl : arr) {
                        JsonObject eObj = entryEl.getAsJsonObject();
                        String eTitle = getString(eObj, "title", "Entry");
                        String ePart = getString(eObj, "part", "");
                        List<EntryBlock> blocks = new ArrayList<>();
                        if (eObj.has("blocks") && eObj.get("blocks").isJsonArray()) {
                            for (JsonElement bEl : eObj.getAsJsonArray("blocks")) {
                                JsonObject bObj = bEl.getAsJsonObject();
                                blocks.add(new EntryBlock(
                                        getString(bObj, "type", "paragraph"),
                                        getString(bObj, "text", "")
                                ));
                            }
                        }
                        entries.add(new JournalEntry(eTitle, ePart, Collections.unmodifiableList(blocks)));
                    }
                }

                return new JournalVolume(
                        vol, roman, id, title, shortTitle, subtitle, author,
                        Collections.unmodifiableList(epigraph),
                        coverCol, trimCol, accentCol,
                        Collections.unmodifiableList(entries)
                );
            }
        } catch (Exception e) {
            Entropica.LOGGER.error("[Entropica] Failed to load journal volume {}: {}", volumeNumber, e.getMessage());
            return createFallbackVolume(volumeNumber);
        }
    }

    private static String getString(JsonObject obj, String key, String fallback) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : fallback;
    }

    private static JournalVolume createFallbackVolume(int volumeNumber) {
        return new JournalVolume(
                volumeNumber,
                String.valueOf(volumeNumber),
                "celestial_journal_vol" + volumeNumber,
                "Scholar's Journal Vol. " + volumeNumber,
                "Scholar's Journal",
                "Faded Astral Pages",
                "Unknown Scribe",
                List.of(),
                0xFF1A2842,
                0xFFDAAF4C,
                0xFF4CE2FF,
                List.of(new JournalEntry("Faded Entry", "", List.of(new EntryBlock("paragraph", "The ink upon this parchment has faded."))))
        );
    }

    /**
     * Parses inline Markdown (**bold** and *italic*) into a styled Minecraft {@link MutableComponent}.
     */
    public static MutableComponent parseStyledMarkdown(String raw, int baseRgb, boolean baseItalic) {
        MutableComponent root = Component.empty();
        if (raw == null || raw.isEmpty()) {
            return root;
        }

        Style baseStyle = Style.EMPTY.withColor(baseRgb).withItalic(baseItalic);
        int i = 0;
        int len = raw.length();
        StringBuilder plain = new StringBuilder();

        while (i < len) {
            if (i + 1 < len && raw.charAt(i) == '*' && raw.charAt(i + 1) == '*') {
                int close = raw.indexOf("**", i + 2);
                if (close > i + 2) {
                    if (!plain.isEmpty()) {
                        root.append(Component.literal(plain.toString()).withStyle(baseStyle));
                        plain.setLength(0);
                    }
                    String inner = raw.substring(i + 2, close);
                    // Check if inner is also wrapped in *italic*
                    boolean innerItalic = baseItalic;
                    if (inner.length() > 2 && inner.startsWith("*") && inner.endsWith("*")) {
                        inner = inner.substring(1, inner.length() - 1);
                        innerItalic = true;
                    }
                    root.append(Component.literal(inner).withStyle(baseStyle.withBold(true).withItalic(innerItalic)));
                    i = close + 2;
                    continue;
                }
            }
            if (raw.charAt(i) == '*') {
                int close = raw.indexOf('*', i + 1);
                if (close > i + 1) {
                    if (!plain.isEmpty()) {
                        root.append(Component.literal(plain.toString()).withStyle(baseStyle));
                        plain.setLength(0);
                    }
                    String inner = raw.substring(i + 1, close);
                    root.append(Component.literal(inner).withStyle(baseStyle.withItalic(true)));
                    i = close + 1;
                    continue;
                }
            }
            plain.append(raw.charAt(i));
            i++;
        }

        if (!plain.isEmpty()) {
            root.append(Component.literal(plain.toString()).withStyle(baseStyle));
        }
        return root;
    }

    public static String stripMarkdownMarkers(String raw) {
        if (raw == null) return "";
        return raw.replace("**", "").replace("*", "");
    }
}
