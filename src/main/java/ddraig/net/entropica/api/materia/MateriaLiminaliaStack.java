package ddraig.net.entropica.api.materia;

import ddraig.net.entropica.api.EssenceType;
import net.minecraft.nbt.CompoundTag;

/**
 * Materia in its Liminal Form (T10 — Mlim): the boundary form.
 * Transported by Athanor Conduit.
 * The apex of the Materia Cycle.
 */
public class MateriaLiminaliaStack extends MateriaStack {

    public static final MateriaLiminaliaStack EMPTY = new MateriaLiminaliaStack(EssenceType.REGULAR, 0) {
        @Override public boolean isEmpty() { return true; }
    };

    public MateriaLiminaliaStack(EssenceType type, int amount) {
        super(type, amount);
    }

    @Override
    public MateriaLiminaliaStack copy() {
        return this.isEmpty() ? EMPTY : new MateriaLiminaliaStack(this.type, this.amount);
    }

    public MateriaLiminaliaStack split(int amount) {
        int splitAmount = Math.min(amount, this.amount);
        MateriaLiminaliaStack result = new MateriaLiminaliaStack(this.type, splitAmount);
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

    public static MateriaLiminaliaStack load(CompoundTag tag) {
        String typeString = tag.getString("MateriaType").orElse("");
        int amount = tag.getInt("Amount").orElse(0);
        if (typeString.equals("empty") || amount <= 0) return EMPTY;
        try {
            return new MateriaLiminaliaStack(EssenceType.valueOf(typeString.toUpperCase()), amount);
        } catch (IllegalArgumentException e) {
            return EMPTY;
        }
    }
}
