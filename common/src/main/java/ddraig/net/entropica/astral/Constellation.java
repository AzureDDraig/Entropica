package ddraig.net.entropica.astral;

import ddraig.net.entropica.api.EssenceType;
import net.minecraft.resources.ResourceLocation;
import java.util.List;

public class Constellation {
    private final ResourceLocation id;
    private final String unlocalizedName;
    private final ConstellationTier tier;
    private final SpectralClass primarySpectralClass;
    private final EssenceType essenceType;
    private final float fluxRate; // Calibrated flux generation per tick
    private final int moonPhaseMask; // 8-bit mask for 8 Minecraft moon phases (bit 0 = Full Moon, bit 4 = New Moon)
    private final List<ConstellationStar> stars;
    private final List<ConstellationConnection> connections;
    private final String ritualEffect;

    public Constellation(ResourceLocation id, String unlocalizedName, ConstellationTier tier,
                         SpectralClass primarySpectralClass, EssenceType essenceType, float fluxRate,
                         int moonPhaseMask, List<ConstellationStar> stars, List<ConstellationConnection> connections,
                         String ritualEffect) {
        this.id = id;
        this.unlocalizedName = unlocalizedName;
        this.tier = tier;
        this.primarySpectralClass = primarySpectralClass;
        this.essenceType = essenceType;
        this.fluxRate = fluxRate;
        this.moonPhaseMask = moonPhaseMask;
        this.stars = List.copyOf(stars);
        this.connections = List.copyOf(connections);
        this.ritualEffect = ritualEffect;
    }

    public ResourceLocation getId() {
        return id;
    }

    public String getUnlocalizedName() {
        return unlocalizedName;
    }

    public ConstellationTier getTier() {
        return tier;
    }

    public SpectralClass getPrimarySpectralClass() {
        return primarySpectralClass;
    }

    public EssenceType getEssenceType() {
        return essenceType;
    }

    public float getFluxRate() {
        return fluxRate;
    }

    public int getMoonPhaseMask() {
        return moonPhaseMask;
    }

    public boolean isVisibleDuringMoonPhase(int moonPhaseIndex) {
        if (moonPhaseIndex < 0 || moonPhaseIndex > 7) return true;
        return (moonPhaseMask & (1 << moonPhaseIndex)) != 0;
    }

    public List<ConstellationStar> getStars() {
        return stars;
    }

    public List<ConstellationConnection> getConnections() {
        return connections;
    }

    public String getRitualEffect() {
        return ritualEffect;
    }
}
