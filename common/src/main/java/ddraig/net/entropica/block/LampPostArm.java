package ddraig.net.entropica.block;

import net.minecraft.util.StringRepresentable;

public enum LampPostArm implements StringRepresentable {
    NONE("none"),
    ARM("arm"),
    BULB("bulb"),
    CABLE("cable");

    private final String name;

    LampPostArm(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public boolean hasArm() {
        return this != NONE;
    }

    public boolean hasBulb() {
        return this == BULB;
    }

    public boolean isCable() {
        return this == CABLE;
    }
}
