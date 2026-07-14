package ddraig.net.entropica.api.materia;

import ddraig.net.entropica.api.EssenceType;
import net.minecraft.nbt.CompoundTag;

/**
 * Materia in its Liquid Form (T4 — Mliq): the basic fluid.
 * Transported by Hydraulic Pipeline. Requires pressure to move.
 * Decompresses to Msub (×6) when entering a Vapor Pneumatic network via coupling.
 * Conversion: 2 Mliq = 1 Mvol
 */
public class MateriaLiquidaStack extends MateriaStack {

    public static final MateriaLiquidaStack EMPTY = new MateriaLiquidaStack(EssenceType.REGULAR, 0) {
        @Override public boolean isEmpty() { return true; }
    };

    public MateriaLiquidaStack(EssenceType type, int amount) {
        super(type, amount);
    }

    @Override
    public MateriaLiquidaStack copy() {
        return this.isEmpty() ? EMPTY : new MateriaLiquidaStack(this.type, this.amount);
    }

    public MateriaLiquidaStack split(int amount) {
        int splitAmount = Math.min(amount, this.amount);
        MateriaLiquidaStack result = new MateriaLiquidaStack(this.type, splitAmount);
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

    public static MateriaLiquidaStack load(CompoundTag tag) {
        String typeString = tag.getString("MateriaType").orElse("");
        int amount = tag.getInt("Amount").orElse(0);
        if (typeString.equals("empty") || amount <= 0) return EMPTY;
        try {
            return new MateriaLiquidaStack(EssenceType.valueOf(typeString.toUpperCase()), amount);
        } catch (IllegalArgumentException e) {
            return EMPTY;
        }
    }
}
