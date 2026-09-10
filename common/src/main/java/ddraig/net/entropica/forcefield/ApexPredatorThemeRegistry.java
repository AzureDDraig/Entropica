package ddraig.net.entropica.forcefield;

import ddraig.net.entropica.forcefield.theme.*;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry managing all registered Apex Predator themes and custom forcefield visuals.
 */
public class ApexPredatorThemeRegistry {

    private static final Map<ResourceLocation, ApexPredatorTheme> BY_ID = new ConcurrentHashMap<>();
    private static final Map<Integer, ApexPredatorTheme> BY_ORDINAL = new ConcurrentHashMap<>();
    private static final List<ApexPredatorTheme> ALL_THEMES = new ArrayList<>();

    public static final ApexPredatorTheme STANDARD = register(new StandardTheme());
    public static final ApexPredatorTheme STAR_EATER = register(new StarEaterTheme());
    public static final ApexPredatorTheme VOID_LEVIATHAN = register(new VoidLeviathanTheme());
    public static final ApexPredatorTheme ENTROPIC_CHIMERA = register(new EntropicChimeraTheme());
    public static final ApexPredatorTheme DEFILER_OF_SYMMETRIES = register(new DefilerOfSymmetriesTheme());
    public static final ApexPredatorTheme UNMAKER_OF_FORMS = register(new UnmakerOfFormsTheme());
    public static final ApexPredatorTheme SILENCER_OF_ECHOES = register(new SilencerOfEchoesTheme());

    public static synchronized <T extends ApexPredatorTheme> T register(T theme) {
        Objects.requireNonNull(theme, "Theme cannot be null");
        BY_ID.put(theme.getId(), theme);
        BY_ORDINAL.put(theme.getOrdinal(), theme);
        ALL_THEMES.removeIf(t -> t.getOrdinal() == theme.getOrdinal());
        ALL_THEMES.add(theme);
        ALL_THEMES.sort(Comparator.comparingInt(ApexPredatorTheme::getOrdinal));
        return theme;
    }

    public static ApexPredatorTheme get(ResourceLocation id) {
        return BY_ID.getOrDefault(id, STANDARD);
    }

    public static ApexPredatorTheme fromOrdinal(int ordinal) {
        ApexPredatorTheme theme = BY_ORDINAL.get(ordinal);
        return theme != null ? theme : STANDARD;
    }

    public static ApexPredatorTheme fromName(String name) {
        if (name == null || name.isEmpty()) return STANDARD;
        String clean = name.trim().toLowerCase(Locale.ROOT);
        for (ApexPredatorTheme theme : ALL_THEMES) {
            if (theme.getId().getPath().equalsIgnoreCase(clean) ||
                theme.getDisplayName().equalsIgnoreCase(name) ||
                theme.name().equalsIgnoreCase(clean) ||
                theme.getId().toString().equalsIgnoreCase(name)) {
                return theme;
            }
        }
        return STANDARD;
    }

    public static Collection<ApexPredatorTheme> getAll() {
        return Collections.unmodifiableList(ALL_THEMES);
    }

    public static int getCount() {
        return ALL_THEMES.size();
    }
}
