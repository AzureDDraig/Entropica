package ddraig.net.entropica.api.materia;

import ddraig.net.entropica.api.EssenceType;
import net.minecraft.nbt.CompoundTag;

/**
 * Materia in its Sublimated Form (T3 — Msub): the purified gas.
 * Transported by Vapor Pneumatic Pipes at HALF capacity.
 * Conversion: 6 Msub = 1 Mliq
 */
public class MateriaSublimataStack extends MateriaStack {

    public static final MateriaSublimataStack EMPTY = new MateriaSublimataStack(EssenceType.REGULAR, 0) {
        @Override public boolean isEmpty() { return true; }
    };

    public MateriaSublimataStack(EssenceType type, int amount) {
        super(type, amount);
    }

    @Override
    public MateriaSublimataStack copy() {
        return this.isEmpty() ? EMPTY : new MateriaSublimataStack(this.type, this.amount);
    }

    public MateriaSublimataStack split(int amount) {
        int splitAmount = Math.min(amount, this.amount);
        MateriaSublimataStack result = new MateriaSublimataStack(this.type, splitAmount);
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

    public static MateriaSublimataStack load(CompoundTag tag) {
        String typeString = tag.getString("MateriaType").orElse("");
        int amount = tag.getInt("Amount").orElse(0);
        if (typeString.equals("empty") || amount <= 0) return EMPTY;
        try {
            return new MateriaSublimataStack(EssenceType.valueOf(typeString.toUpperCase()), amount);
        } catch (IllegalArgumentException e) {
            return EMPTY;
        }
    }
}
