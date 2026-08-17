package ddraig.net.entropica.astral;

public enum SpectralClass {
    CLASS_O("O", "Deep Azure Hypergiant", 0xFF387BFF, 35000, 2.5f),
    CLASS_B("B", "Blue-White Radiant", 0xFF85B5FF, 20000, 2.0f),
    CLASS_A("A", "Pure White Diamond", 0xFFFFFFFF, 9000, 1.6f),
    CLASS_F("F", "Yellow-White Flare", 0xFFFFFFCC, 6800, 1.3f),
    CLASS_G("G", "Solar Gold", 0xFFFFE875, 5500, 1.0f),
    CLASS_K("K", "Orange Giant", 0xFFFF9A3C, 4000, 0.85f),
    CLASS_M("M", "Crimson Cinder", 0xFFFF4538, 3000, 0.7f),
    CLASS_V("V", "Void Singularity", 0xFF8A2BE2, 0, 3.5f);

    private final String code;
    private final String title;
    private final int colorRgb;
    private final int kelvinTemperature;
    private final float radianceFactor;

    SpectralClass(String code, String title, int colorRgb, int kelvinTemperature, float radianceFactor) {
        this.code = code;
        this.title = title;
        this.colorRgb = colorRgb;
        this.kelvinTemperature = kelvinTemperature;
        this.radianceFactor = radianceFactor;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public int getColorRgb() {
        return colorRgb;
    }

    public int getKelvinTemperature() {
        return kelvinTemperature;
    }

    public float getRadianceFactor() {
        return radianceFactor;
    }
}
