package ddraig.net.entropica.entity.amber_weeping_stag;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum AmberStagVariant implements StringRepresentable {
    AMBER(0, "amber", "textures/entity/amber_weeping_stag/amber_weeping_stag.png"),
    FROST(1, "frost", "textures/entity/amber_weeping_stag/amber_weeping_stag_frost.png"),
    SOLAR(2, "solar", "textures/entity/amber_weeping_stag/amber_weeping_stag_solar.png");

    public static final Codec<AmberStagVariant> CODEC = StringRepresentable.fromEnum(AmberStagVariant::values);

    private final int id;
    private final String name;
    private final String texturePath;

    AmberStagVariant(int id, String name, String texturePath) {
        this.id = id;
        this.name = name;
        this.texturePath = texturePath;
    }

    public int getId() {
        return id;
    }

    public String getTexturePath() {
        return texturePath;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }

    public static AmberStagVariant byId(int id) {
        for (AmberStagVariant v : values()) {
            if (v.id == id) return v;
        }
        return AMBER;
    }
}
