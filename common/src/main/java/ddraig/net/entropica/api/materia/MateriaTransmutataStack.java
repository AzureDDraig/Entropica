package ddraig.net.entropica.api.materia;

import ddraig.net.entropica.api.EssenceType;
import net.minecraft.nbt.CompoundTag;

/**
 * Materia in its Transmuted Form (T8 — Mtra).
 * Transported by Equilibrium Conduit.
 * Conversion: 5 Mtra = 1 Mper
 */
public class MateriaTransmutataStack extends MateriaStack {

    public static final MateriaTransmutataStack EMPTY = new MateriaTransmutataStack(EssenceType.REGULAR, 0) {
        @Override public boolean isEmpty() { return true; }
    };

    public MateriaTransmutataStack(EssenceType type, int amount) {
        super(type, amount);
    }

    @Override
    public MateriaTransmutataStack copy() {
        return this.isEmpty() ? EMPTY : new MateriaTransmutataStack(this.type, this.amount);
    }

    public MateriaTransmutataStack split(int amount) {
        int splitAmount = Math.min(amount, this.amount);
        MateriaTransmutataStack result = new MateriaTransmutataStack(this.type, splitAmount);
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

    public static MateriaTransmutataStack load(CompoundTag tag) {
        String typeString = tag.getString("MateriaType").orElse("");
        int amount = tag.getInt("Amount").orElse(0);
        if (typeString.equals("empty") || amount <= 0) return EMPTY;
        try {
            return new MateriaTransmutataStack(EssenceType.valueOf(typeString.toUpperCase()), amount);
        } catch (IllegalArgumentException e) {
            return EMPTY;
        }
    }
}
