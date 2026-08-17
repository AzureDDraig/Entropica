package ddraig.net.entropica.astral;

public record ConstellationStar(int index, float x, float y, float brightness, SpectralClass spectralClass) {
    public ConstellationStar(int index, float x, float y) {
        this(index, x, y, 1.0f, SpectralClass.CLASS_A);
    }
}
