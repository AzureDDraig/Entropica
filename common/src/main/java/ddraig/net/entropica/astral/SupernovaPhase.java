package ddraig.net.entropica.astral;

public enum SupernovaPhase {
    PRECURSOR("Thermal Instability"),
    FLASH("Supernova Detonation"),
    EXPANDING_NEBULA("Expanding Figure-8 Nebula"),
    REMNANT("Collapsed Stellar Remnant");

    private final String display;

    SupernovaPhase(String display) {
        this.display = display;
    }

    public String getDisplayName() {
        return display;
    }
}
