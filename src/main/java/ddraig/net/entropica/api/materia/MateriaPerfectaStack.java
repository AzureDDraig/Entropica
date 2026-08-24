package ddraig.net.entropica.api.materia;

import ddraig.net.entropica.api.EssenceType;
import net.minecraft.nbt.CompoundTag;

/**
 * Materia in its Perfect Form (T9 — Mper).
 * Transported by Pristine Conduit.
 * Conversion: 5 Mper = 1 Mlim
 */
public class MateriaPerfectaStack extends MateriaStack {

    public static final MateriaPerfectaStack EMPTY = new MateriaPerfectaStack(EssenceType.REGULAR, 0) {
        @Override public boolean isEmpty() { return true; }
    };

    public MateriaPerfectaStack(EssenceType type, int amount) {
        super(type, amount);
    }

    @Override
    public MateriaPerfectaStack copy() {
        return this.isEmpty() ? EMPTY : new MateriaPerfectaStack(this.type, this.amount);
    }

    public MateriaPerfectaStack split(int amount) {
        int splitAmount = Math.min(amount, this.amount);
        MateriaPerfectaStack result = new MateriaPerfectaStack(this.type, splitAmount);
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

    public static MateriaPerfectaStack load(CompoundTag tag) {
        String typeString = tag.getString("MateriaType").orElse("");
        int amount = tag.getInt("Amount").orElse(0);
        if (typeString.equals("empty") || amount <= 0) return EMPTY;
        try {
            return new MateriaPerfectaStack(EssenceType.valueOf(typeString.toUpperCase()), amount);
        } catch (IllegalArgumentException e) {
            return EMPTY;
        }
    }
}
