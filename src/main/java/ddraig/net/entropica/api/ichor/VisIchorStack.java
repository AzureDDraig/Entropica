package ddraig.net.entropica.api.ichor;

import ddraig.net.entropica.api.EssenceType;

/**
 * Represents a discrete quantity of liquid Materia Ichor.
 * Functions similarly to a FluidStack or ItemStack.
 */
public class VisIchorStack {
    public static final VisIchorStack EMPTY = new VisIchorStack(null, 0);

    private final EssenceType type;
    private int amount;

    public VisIchorStack(EssenceType type, int amount) {
        this.type = type;
        this.amount = amount;
    }

    public boolean isEmpty() {
        return this == EMPTY || this.type == null || this.amount <= 0;
    }

    public EssenceType getType() {
        return type;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void grow(int amount) {
        this.amount += amount;
    }

    public void shrink(int amount) {
        this.amount -= amount;
    }

    public VisIchorStack copy() {
        if (this.isEmpty()) return EMPTY;
        return new VisIchorStack(this.type, this.amount);
    }
}