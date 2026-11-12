package ddraig.net.entropica.api.materia;

import ddraig.net.entropica.api.EssenceType;
import net.minecraft.nbt.CompoundTag;

/**
 * Materia in its Volatile Form (T5 — Mvol): the charged fluid.
 * Transported by Voltaic Conduit.
 * Decompresses to Mliq (×2) when entering a Hydraulic Pipeline via coupling.
 * Conversion: 16 Mvol = 1 Mcoa
 */
public class MateriaVolatilisStack extends MateriaStack {

    public static final MateriaVolatilisStack EMPTY = new MateriaVolatilisStack(EssenceType.REGULAR, 0) {
        @Override public boolean isEmpty() { return true; }
    };

    public MateriaVolatilisStack(EssenceType type, int amount) {
        super(type, amount);
    }

    @Override
    public MateriaVolatilisStack copy() {
        return this.isEmpty() ? EMPTY : new MateriaVolatilisStack(this.type, this.amount);
    }

    public MateriaVolatilisStack split(int amount) {
        int splitAmount = Math.min(amount, this.amount);
        MateriaVolatilisStack result = new MateriaVolatilisStack(this.type, splitAmount);
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

    public static MateriaVolatilisStack load(CompoundTag tag) {
        String typeString = tag.getString("MateriaType").orElse("");
        int amount = tag.getInt("Amount").orElse(0);
        if (typeString.equals("empty") || amount <= 0) return EMPTY;
        try {
            return new MateriaVolatilisStack(EssenceType.valueOf(typeString.toUpperCase()), amount);
        } catch (IllegalArgumentException e) {
            return EMPTY;
        }
    }
}
