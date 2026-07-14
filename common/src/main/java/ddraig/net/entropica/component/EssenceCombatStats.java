package ddraig.net.entropica.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ddraig.net.entropica.api.EssenceType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.HashMap;
import java.util.Map;

public record EssenceCombatStats(Map<EssenceType, Float> attackBonuses, Map<EssenceType, Float> resistances) {

    public static final EssenceCombatStats EMPTY = new EssenceCombatStats(new HashMap<>(), new HashMap<>());

    // Codec for saving to JSON and Disk
    public static final Codec<EssenceCombatStats> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(EssenceType.CODEC, Codec.FLOAT)
                    .fieldOf("attack_bonuses").forGetter(EssenceCombatStats::attackBonuses),
            Codec.unboundedMap(EssenceType.CODEC, Codec.FLOAT)
                    .fieldOf("resistances").forGetter(EssenceCombatStats::resistances)
    ).apply(instance, EssenceCombatStats::new));

    // StreamCodec for sending over the Network
    public static final StreamCodec<RegistryFriendlyByteBuf, EssenceCombatStats> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, EssenceType.STREAM_CODEC, ByteBufCodecs.FLOAT), EssenceCombatStats::attackBonuses,
            ByteBufCodecs.map(HashMap::new, EssenceType.STREAM_CODEC, ByteBufCodecs.FLOAT), EssenceCombatStats::resistances,
            EssenceCombatStats::new
    );

    // Helper Builder to easily construct these in your item code
    public static class Builder {
        private final Map<EssenceType, Float> attacks = new HashMap<>();
        private final Map<EssenceType, Float> resists = new HashMap<>();

        public Builder addAttack(EssenceType type, float amount) {
            this.attacks.put(type, this.attacks.getOrDefault(type, 0f) + amount);
            return this;
        }

        public Builder addResistance(EssenceType type, float amount) {
            this.resists.put(type, this.resists.getOrDefault(type, 0f) + amount);
            return this;
        }

        public EssenceCombatStats build() {
            return new EssenceCombatStats(this.attacks, this.resists);
        }
    }
}