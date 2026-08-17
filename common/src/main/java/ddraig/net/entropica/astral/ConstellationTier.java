package ddraig.net.entropica.astral;

public enum ConstellationTier {
    FUNDAMENTAL(1, "Fundamental", 0xFFFFFFFF, 1.0f),
    ADVANCED(2, "Advanced", 0xFF7FFFD4, 1.5f),
    MASTER(3, "Master", 0xFFFFD700, 2.0f),
    MYTHIC(4, "Mythic", 0xFFFF4500, 3.0f);

    private final int level;
    private final String displayName;
    private final int colorHex;
    private final float powerMultiplier;

    ConstellationTier(int level, String displayName, int colorHex, float powerMultiplier) {
        this.level = level;
        this.displayName = displayName;
        this.colorHex = colorHex;
        this.powerMultiplier = powerMultiplier;
    }

    public int getLevel() {
        return level;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getColorHex() {
        return colorHex;
    }

    public float getPowerMultiplier() {
        return powerMultiplier;
    }
}
