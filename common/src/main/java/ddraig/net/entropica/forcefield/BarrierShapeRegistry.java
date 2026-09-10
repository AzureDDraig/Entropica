package ddraig.net.entropica.forcefield;

import ddraig.net.entropica.forcefield.shape.*;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry managing all registered barrier geometric shape primitives.
 * Provides lookup by ResourceLocation and fixed integer ordinals (0-5) for network and entity data compatibility.
 */
public class BarrierShapeRegistry {

    private static final Map<ResourceLocation, BarrierShapeHandler> BY_ID = new ConcurrentHashMap<>();
    private static final Map<Integer, BarrierShapeHandler> BY_ORDINAL = new ConcurrentHashMap<>();
    private static final List<BarrierShapeHandler> ALL_SHAPES = new ArrayList<>();
    private static BarrierShapeHandler DEFAULT_SHAPE;

    public static final BarrierShapeHandler PLANAR_QUAD = register(new PlanarQuadShapeHandler());
    public static final BarrierShapeHandler CIRCULAR_DISC = register(new CircularDiscShapeHandler());
    public static final BarrierShapeHandler HEMISPHERICAL_DOME = register(new HemisphericalDomeShapeHandler());
    public static final BarrierShapeHandler SPHERICAL_BUBBLE = register(new SphericalBubbleShapeHandler());
    public static final BarrierShapeHandler CYLINDER = register(new CylinderShapeHandler());
    public static final BarrierShapeHandler CONVEX_POLYGON = register(new ConvexPolygonShapeHandler());

    public static synchronized <T extends BarrierShapeHandler> T register(T handler) {
        Objects.requireNonNull(handler, "BarrierShapeHandler cannot be null");
        BY_ID.put(handler.getId(), handler);
        BY_ORDINAL.put(handler.getOrdinal(), handler);

        ALL_SHAPES.removeIf(h -> h.getOrdinal() == handler.getOrdinal());
        ALL_SHAPES.add(handler);
        ALL_SHAPES.sort(Comparator.comparingInt(BarrierShapeHandler::getOrdinal));

        if (DEFAULT_SHAPE == null || handler.getOrdinal() == 0) {
            DEFAULT_SHAPE = handler;
        }
        return handler;
    }

    public static BarrierShapeHandler get(int ordinal) {
        BarrierShapeHandler handler = BY_ORDINAL.get(ordinal);
        return handler != null ? handler : DEFAULT_SHAPE;
    }

    public static BarrierShapeHandler get(ResourceLocation id) {
        BarrierShapeHandler handler = BY_ID.get(id);
        return handler != null ? handler : DEFAULT_SHAPE;
    }

    public static BarrierShapeHandler get(BarrierShape shape) {
        return get(shape != null ? shape.ordinal() : 0);
    }

    public static List<BarrierShapeHandler> getAll() {
        return Collections.unmodifiableList(ALL_SHAPES);
    }

    public static BarrierShapeHandler getDefault() {
        return DEFAULT_SHAPE;
    }
}
