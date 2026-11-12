package ddraig.net.entropica.block;

import net.minecraft.util.StringRepresentable;

/**
 * Horizontal half selector for 2-block-wide spellcrafting workstations
 * ({@link ArcaneScribingTableBlock} and {@link RunicAnvilBlock}).
 *
 * <p>{@link #LEFT} is the primary anchor block at {@code pos}; {@link #RIGHT} occupies
 * {@code pos.relative(facing.getCounterClockWise())} (to the player's right when facing the front).</p>
 */
public enum DoubleWorkstationPart implements StringRepresentable {
    LEFT("left"),
    RIGHT("right");

    private final String name;

    DoubleWorkstationPart(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
