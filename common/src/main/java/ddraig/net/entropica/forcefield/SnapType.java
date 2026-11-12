package ddraig.net.entropica.forcefield;

/**
 * Classification of edge-fusing and seamless snapping alignments.
 */
public enum SnapType {
    NONE("Free Floating"),
    COPLANAR_EXTENSION("Wall Extension"),
    CORNER_PERPENDICULAR("90° Corner"),
    CORNER_ANGLED("45° Corner"),
    VERTICAL_STACK("Vertical Extension"),
    TANGENT_RADIAL("Tangent Contact");

    private final String displayName;

    SnapType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
