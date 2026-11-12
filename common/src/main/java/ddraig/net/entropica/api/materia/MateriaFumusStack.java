package ddraig.net.entropica.api.materia;

import ddraig.net.entropica.api.EssenceType;
import net.minecraft.nbt.CompoundTag;

/**
 * Materia in its Fume Form (T2 — Mfum): the primary gaseous transport medium.
 * Transported by Vapor Pneumatic Pipes.
 * Conversion: 360 Mfum = 1 Msub
 */
public class MateriaFumusStack extends MateriaStack {

    public static final MateriaFumusStack EMPTY = new MateriaFumusStack(EssenceType.REGULAR, 0) {
        @Override public boolean isEmpty() { return true; }
    };

    public MateriaFumusStack(EssenceType type, int amount) {
        super(type, amount);
    }

    @Override
    public MateriaFumusStack copy() {
        return this.isEmpty() ? EMPTY : new MateriaFumusStack(this.type, this.amount);
    }

    public MateriaFumusStack split(int amount) {
        int splitAmount = Math.min(amount, this.amount);
        MateriaFumusStack result = new MateriaFumusStack(this.type, splitAmount);
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

    public static MateriaFumusStack load(CompoundTag tag) {
        String typeString = tag.getString("MateriaType").orElse("");
        int amount = tag.getInt("Amount").orElse(0);
        if (typeString.equals("empty") || amount <= 0) return EMPTY;
        try {
            return new MateriaFumusStack(EssenceType.valueOf(typeString.toUpperCase()), amount);
        } catch (IllegalArgumentException e) {
            return EMPTY;
        }
    }
}
