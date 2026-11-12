package ddraig.net.entropica.block;

import net.minecraft.util.StringRepresentable;

public enum LampPostPart implements StringRepresentable {
    SINGLE("single"),
    BOTTOM("bottom"),
    LOWER_MIDDLE("lower_middle"),
    MIDDLE("middle"),
    UPPER_MIDDLE("upper_middle"),
    TOP("top");

    private final String name;

    LampPostPart(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
