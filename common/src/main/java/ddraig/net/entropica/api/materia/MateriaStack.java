package ddraig.net.entropica.api.materia;

import ddraig.net.entropica.api.EssenceType;

/**
 * Abstract base class for all Materia stage stacks.
 * Each stage of the 10-stage Materia Cycle has its own concrete subclass.
 * The EssenceType represents the Materia flavour (Fire, Water, Void, etc.),
 * which is preserved across stage conversions.
 */
public abstract class MateriaStack {

    protected EssenceType type;
    protected int amount;

    protected MateriaStack(EssenceType type, int amount) {
        this.type = type;
        this.amount = Math.max(0, amount);
    }

    public boolean isEmpty() {
        return this.amount <= 0;
    }

    public EssenceType getType() {
        return this.isEmpty() ? EssenceType.REGULAR : this.type;
    }

    public int getAmount() {
        return this.isEmpty() ? 0 : this.amount;
    }

    public void setAmount(int amount) {
        this.amount = Math.max(0, amount);
    }

    public void grow(int amount) {
        this.setAmount(this.amount + amount);
    }

    public void shrink(int amount) {
        this.setAmount(this.amount - amount);
    }

    public boolean is(EssenceType otherType) {
        return !this.isEmpty() && this.type == otherType;
    }

    /** Returns a deep copy of this stack. */
    public abstract MateriaStack copy();
}
