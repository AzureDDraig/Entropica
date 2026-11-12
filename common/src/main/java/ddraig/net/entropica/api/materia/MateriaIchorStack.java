package ddraig.net.entropica.api.materia;

import ddraig.net.entropica.api.EssenceType;
import net.minecraft.nbt.CompoundTag;

/**
 * Materia in its Divine Blood Form (T7 — Mich): the Ichor.
 * Transported by Sanguine Conduit.
 * Conversion: 5 Mich = 1 Mtra
 */
public class MateriaIchorStack extends MateriaStack {

    public static final MateriaIchorStack EMPTY = new MateriaIchorStack(EssenceType.REGULAR, 0) {
        @Override public boolean isEmpty() { return true; }
    };

    public MateriaIchorStack(EssenceType type, int amount) {
        super(type, amount);
    }

    @Override
    public MateriaIchorStack copy() {
        return this.isEmpty() ? EMPTY : new MateriaIchorStack(this.type, this.amount);
    }

    public MateriaIchorStack split(int amount) {
        int splitAmount = Math.min(amount, this.amount);
        MateriaIchorStack result = new MateriaIchorStack(this.type, splitAmount);
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

    public static MateriaIchorStack load(CompoundTag tag) {
        String typeString = tag.getString("MateriaType").orElse("");
        int amount = tag.getInt("Amount").orElse(0);
        if (typeString.equals("empty") || amount <= 0) return EMPTY;
        try {
            return new MateriaIchorStack(EssenceType.valueOf(typeString.toUpperCase()), amount);
        } catch (IllegalArgumentException e) {
            return EMPTY;
        }
    }
}
