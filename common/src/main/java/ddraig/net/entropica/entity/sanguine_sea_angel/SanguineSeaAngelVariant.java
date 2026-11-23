package ddraig.net.entropica.entity.sanguine_sea_angel;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

/**
 * Morph variants for Biome #16 (2.16 Crimson Tide-Skerries) Fauna — Sanguine Sea-Angel:
 * - POLAR_BLOOD_SHALLOWS (0): Translucent rose-ice mantle with glowing arterial crimson heart core (surface bay morph).
 * - ABYSSAL_GARNET (1): Deep benthic garnet-violet mantle with dark ruby heart core (deep water Y < 52 or night/storm morph).
 */
public enum SanguineSeaAngelVariant implements StringRepresentable {
    POLAR_BLOOD_SHALLOWS(0, "polar_blood_shallows", "textures/entity/sanguine_sea_angel/sanguine_sea_angel.png"),
    ABYSSAL_GARNET(1, "abyssal_garnet", "textures/entity/sanguine_sea_angel/sanguine_sea_angel_abyssal.png");

    public static final Codec<SanguineSeaAngelVariant> CODEC =
            StringRepresentable.fromEnum(SanguineSeaAngelVariant::values);
    public static final Codec<SanguineSeaAngelVariant> INT_CODEC =
            Codec.INT.xmap(SanguineSeaAngelVariant::byId, SanguineSeaAngelVariant::getId);

    private final int id;
    private final String name;
    private final String texturePath;

    SanguineSeaAngelVariant(int id, String name, String texturePath) {
        this.id = id;
        this.name = name;
        this.texturePath = texturePath;
    }

    public int getId() {
        return this.id;
    }

    public String getTexturePath() {
        return this.texturePath;
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.name;
    }

    public static SanguineSeaAngelVariant byId(int id) {
        for (SanguineSeaAngelVariant variant : values()) {
            if (variant.id == id) {
                return variant;
            }
        }
        return POLAR_BLOOD_SHALLOWS;
    }
}
