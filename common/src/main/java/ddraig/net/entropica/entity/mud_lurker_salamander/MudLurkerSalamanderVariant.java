package ddraig.net.entropica.entity.mud_lurker_salamander;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

/**
 * Regional coat variants for Fauna 33 — Mud-Lurker Salamander:
 * - OVERWORLD_MUD (0, "mud"): Thermal-mud & basalt-pebble hide with burnt-orange dorsal plates.
 * - NETHER_MAGMA (1, "nether"): Scorched blackstone hide veined with magma fissures & soul-fire cyan dorsal plates.
 */
public enum MudLurkerSalamanderVariant implements StringRepresentable {
    OVERWORLD_MUD(0, "mud", "textures/entity/mud_lurker_salamander/mud_lurker_salamander.png"),
    NETHER_MAGMA(1, "nether", "textures/entity/mud_lurker_salamander/mud_lurker_salamander_nether.png");

    public static final Codec<MudLurkerSalamanderVariant> CODEC =
            StringRepresentable.fromEnum(MudLurkerSalamanderVariant::values);
    public static final Codec<MudLurkerSalamanderVariant> INT_CODEC =
            Codec.INT.xmap(MudLurkerSalamanderVariant::byId, MudLurkerSalamanderVariant::getId);

    private final int id;
    private final String name;
    private final String texturePath;

    MudLurkerSalamanderVariant(int id, String name, String texturePath) {
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

    public static MudLurkerSalamanderVariant byId(int id) {
        for (MudLurkerSalamanderVariant variant : values()) {
            if (variant.id == id) {
                return variant;
            }
        }
        return OVERWORLD_MUD;
    }
}
