package ddraig.net.entropica.spell.apparatus;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ddraig.net.entropica.spell.assembly.SpellAssembly;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DataComponent state attached to Casting Apparatuses ({@code Arcanum Focus}, {@code Wand},
 * {@code Staff}, {@code Grimoire}) and Engraved Weapons/Tools.
 */
public record CastingApparatusState(
        ApparatusType apparatusType,
        int activeSlotIndex,
        float materialEfficiencyBonus,
        List<SocketedSpellgem> sockets
) {
    public CastingApparatusState {
        if (apparatusType == null) apparatusType = ApparatusType.ARCANUM_FOCUS;
        int cap = apparatusType.getGemCapacity();
        List<SocketedSpellgem> normalized = new ArrayList<>(cap);
        for (int i = 0; i < cap; i++) {
            if (sockets != null && i < sockets.size() && sockets.get(i) != null) {
                normalized.add(sockets.get(i));
            } else {
                normalized.add(SocketedSpellgem.EMPTY);
            }
        }
        sockets = List.copyOf(normalized);
        activeSlotIndex = Math.clamp(activeSlotIndex, 0, cap - 1);
        materialEfficiencyBonus = Math.clamp(materialEfficiencyBonus, 0.0F, 0.50F);
    }

    public static final Codec<CastingApparatusState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ApparatusType.CODEC.fieldOf("apparatus_type").forGetter(CastingApparatusState::apparatusType),
            Codec.INT.optionalFieldOf("active_slot_index", 0).forGetter(CastingApparatusState::activeSlotIndex),
            Codec.FLOAT.optionalFieldOf("material_efficiency_bonus", 0.0F).forGetter(CastingApparatusState::materialEfficiencyBonus),
            SocketedSpellgem.CODEC.listOf().fieldOf("sockets").forGetter(CastingApparatusState::sockets)
    ).apply(instance, CastingApparatusState::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CastingApparatusState> STREAM_CODEC = StreamCodec.composite(
            ApparatusType.STREAM_CODEC, CastingApparatusState::apparatusType,
            ByteBufCodecs.VAR_INT, CastingApparatusState::activeSlotIndex,
            ByteBufCodecs.FLOAT, CastingApparatusState::materialEfficiencyBonus,
            SocketedSpellgem.STREAM_CODEC.apply(ByteBufCodecs.list()), CastingApparatusState::sockets,
            CastingApparatusState::new
    );

    public static CastingApparatusState emptyFor(ApparatusType type) {
        return new CastingApparatusState(type, 0, 0.0F, List.of());
    }

    public SocketedSpellgem getActiveSocket() {
        return this.sockets.get(this.activeSlotIndex);
    }

    public Optional<SpellAssembly> getActiveSpell() {
        return getActiveSocket().spell();
    }

    public int countSocketedGems() {
        int count = 0;
        for (SocketedSpellgem s : this.sockets) {
            if (!s.isEmpty()) count++;
        }
        return count;
    }

    public CastingApparatusState withActiveSlot(int newSlotIndex) {
        int wrapped = Math.floorMod(newSlotIndex, this.apparatusType.getGemCapacity());
        return new CastingApparatusState(this.apparatusType, wrapped, this.materialEfficiencyBonus, this.sockets);
    }

    public CastingApparatusState cycleActiveSlot(int delta) {
        int cap = this.apparatusType.getGemCapacity();
        if (cap <= 1) return this;
        int next = Math.floorMod(this.activeSlotIndex + delta, cap);
        return withActiveSlot(next);
    }

    public CastingApparatusState withSocket(int slotIndex, SocketedSpellgem gem) {
        int cap = this.apparatusType.getGemCapacity();
        if (slotIndex < 0 || slotIndex >= cap) return this;
        List<SocketedSpellgem> copy = new ArrayList<>(this.sockets);
        copy.set(slotIndex, gem == null ? SocketedSpellgem.EMPTY : gem);
        return new CastingApparatusState(this.apparatusType, this.activeSlotIndex, this.materialEfficiencyBonus, copy);
    }

    public CastingApparatusState withMaterialEfficiencyBonus(float newBonus) {
        return new CastingApparatusState(this.apparatusType, this.activeSlotIndex, newBonus, this.sockets);
    }
}
