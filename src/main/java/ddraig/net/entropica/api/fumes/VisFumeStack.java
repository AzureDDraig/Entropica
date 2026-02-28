package ddraig.net.entropica.api.fumes;

import ddraig.net.entropica.api.EssenceType;
import net.minecraft.nbt.CompoundTag;

public class VisFumeStack {
    public static final VisFumeStack EMPTY = new VisFumeStack(EssenceType.REGULAR, 0);

    private EssenceType type;
    private int amount;

    public VisFumeStack(EssenceType type, int amount) {
        this.type = type;
        this.amount = Math.max(0, amount);
    }

    // --- UTILITY METHODS ---
    public boolean isEmpty() {
        return this.amount <= 0 || this == EMPTY;
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

    public VisFumeStack copy() {
        return this.isEmpty() ? EMPTY : new VisFumeStack(this.type, this.amount);
    }

    // Splits a specific amount off this stack and returns it as a new stack
    public VisFumeStack split(int amount) {
        int splitAmount = Math.min(amount, this.amount);
        VisFumeStack splitStack = new VisFumeStack(this.type, splitAmount);
        this.shrink(splitAmount);
        return splitStack;
    }

    // --- NBT SERIALIZATION ---
    public CompoundTag save(CompoundTag tag) {
        if (this.isEmpty()) {
            tag.putString("EssenceType", "empty");
            tag.putInt("Amount", 0);
        } else {
            tag.putString("EssenceType", this.type.name());
            tag.putInt("Amount", this.amount);
        }
        return tag;
    }

    public static VisFumeStack load(CompoundTag tag) {
        String typeString = tag.getString("EssenceType").orElse("");
        int amount = tag.getInt("Amount").orElse(0);

        if (typeString.equals("empty") || amount <= 0) {
            return EMPTY;
        }

        try {
            EssenceType loadedType = EssenceType.valueOf(typeString);
            return new VisFumeStack(loadedType, amount);
        } catch (IllegalArgumentException e) {
            return EMPTY; // Fallback if the enum changed or corrupted
        }
    }
}