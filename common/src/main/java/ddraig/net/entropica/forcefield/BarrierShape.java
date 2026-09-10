package ddraig.net.entropica.forcefield;

import net.minecraft.util.StringRepresentable;

import java.util.Locale;

/**
 * Supported geometric primitives for soap-film forcefield barriers.
 * Fully modular and extensible for future non-Euclidean or procedural shapes.
 */
public enum BarrierShape implements StringRepresentable {
    PLANAR_QUAD("Planar Quad", "Flat rectangular wall, floor, or angled pane"),
    CIRCULAR_DISC("Circular Disc", "Radial circular soap-film hoop"),
    HEMISPHERICAL_DOME("Hemispherical Dome", "Bubble canopy dome over an area or machine"),
    SPHERICAL_BUBBLE("Spherical Bubble", "Complete 360-degree soap-bubble sphere"),
    CYLINDER("Cylindrical Column", "Tubular column barrier extending along an axis"),
    CONVEX_POLYGON("Convex Polygon", "Arbitrary multi-vertex planar polygon ribbon");

    private final String displayName;
    private final String description;

    BarrierShape(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    public static BarrierShape fromOrdinal(int ordinal) {
        BarrierShape[] values = values();
        if (ordinal < 0 || ordinal >= values.length) {
            return PLANAR_QUAD;
        }
        return values[ordinal];
    }
}
