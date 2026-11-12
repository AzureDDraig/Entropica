package ddraig.net.entropica.api.materia;

import ddraig.net.entropica.api.EssenceType;
import net.minecraft.nbt.CompoundTag;

/**
 * Materia in its Dense Congealed Form (T6 — Mcoa).
 * Transported by Viscous Agitator.
 * Conversion: 4 Mcoa = 1 Mich
 */
public class MateriaCoagulataStack extends MateriaStack {

    public static final MateriaCoagulataStack EMPTY = new MateriaCoagulataStack(EssenceType.REGULAR, 0) {
        @Override public boolean isEmpty() { return true; }
    };

    public MateriaCoagulataStack(EssenceType type, int amount) {
        super(type, amount);
    }

    @Override
    public MateriaCoagulataStack copy() {
        return this.isEmpty() ? EMPTY : new MateriaCoagulataStack(this.type, this.amount);
    }

    public MateriaCoagulataStack split(int amount) {
        int splitAmount = Math.min(amount, this.amount);
        MateriaCoagulataStack result = new MateriaCoagulataStack(this.type, splitAmount);
        this.shrink(splitAmount);
        return result;
    }

    public CompoundTag save(CompoundTag tag) {
        if (this.isEmpty()) {
            tag.putString("MateriaType", "empty");
            tag.putInt("Amount", 0);
        } else {
            tag.putString("MateriaType", this.type.name());
            tag.putInt("Amount", this.amount);
        }
        return tag;
    }

    public static MateriaCoagulataStack load(CompoundTag tag) {
        String typeString = tag.getString("MateriaType").orElse("");
        int amount = tag.getInt("Amount").orElse(0);
        if (typeString.equals("empty") || amount <= 0) return EMPTY;
        try {
            return new MateriaCoagulataStack(EssenceType.valueOf(typeString.toUpperCase()), amount);
        } catch (IllegalArgumentException e) {
            return EMPTY;
        }
    }
}
