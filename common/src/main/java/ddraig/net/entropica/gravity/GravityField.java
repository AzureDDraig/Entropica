package ddraig.net.entropica.gravity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;

public class GravityField {

    public enum Mode implements StringRepresentable {
        ZERO_G("Zero-G Chamber", 0x4CC9F0),
        LUNAR("Lunar Pavilion", 0xF7D070),
        INVERSION("Graviton Inversion", 0x7209B7),
        SINGULARITY("Singularity Well", 0x3A0CA3),
        GRAV_LIFT("Grav-Lift Beam", 0x4895EF),
        REPULSOR("Repulsor Dome", 0xF72585),
        TIDAL_PULSE("Tidal Pulse", 0x4361EE);

        private final String displayName;
        private final int color;

        Mode(String displayName, int color) {
            this.displayName = displayName;
            this.color = color;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getColor() {
            return color;
        }

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    private final ResourceLocation id;
    private final ResourceKey<Level> dimension;
    private final BlockPos centerPos;
    private final Vec3 centerVec;
    private final double radius;
    private Mode mode;
    private Direction liftDirection = Direction.UP;
    private float strength = 1.0f;
    private int ageTicks = 0;
    private final int maxDurationTicks; // -1 for persistent blocks

    public GravityField(ResourceLocation id, ResourceKey<Level> dimension, BlockPos pos, double radius, Mode mode) {
        this(id, dimension, pos, radius, mode, -1);
    }

    public GravityField(ResourceLocation id, ResourceKey<Level> dimension, BlockPos pos, double radius, Mode mode, int maxDurationTicks) {
        this.id = id;
        this.dimension = dimension != null ? dimension : Level.OVERWORLD;
        this.centerPos = pos;
        this.centerVec = Vec3.atCenterOf(pos);
        this.radius = radius;
        this.mode = mode;
        this.maxDurationTicks = maxDurationTicks;
    }

    public GravityField(ResourceLocation id, BlockPos pos, double radius, Mode mode) {
        this(id, Level.OVERWORLD, pos, radius, mode, -1);
    }

    public GravityField(ResourceLocation id, BlockPos pos, double radius, Mode mode, int maxDurationTicks) {
        this(id, Level.OVERWORLD, pos, radius, mode, maxDurationTicks);
    }

    public ResourceLocation getId() {
        return id;
    }

    public ResourceKey<Level> getDimension() {
        return dimension;
    }

    public BlockPos getCenterPos() {
        return centerPos;
    }

    public Vec3 getCenterVec() {
        return centerVec;
    }

    public double getRadius() {
        return radius;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public Direction getLiftDirection() {
        return liftDirection;
    }

    public void setLiftDirection(Direction liftDirection) {
        this.liftDirection = liftDirection;
    }

    public float getStrength() {
        return strength;
    }

    public void setStrength(float strength) {
        this.strength = strength;
    }

    public int getAgeTicks() {
        return ageTicks;
    }

    public void setAgeTicks(int ageTicks) {
        this.ageTicks = ageTicks;
    }

    public void tickAge() {
        this.ageTicks++;
    }

    public boolean isExpired() {
        return maxDurationTicks > 0 && ageTicks >= maxDurationTicks;
    }

    public AABB getBoundingBox() {
        if (mode == Mode.GRAV_LIFT) {
            // Project a columnar tube along the lift direction
            Vec3 dirVec = new Vec3(liftDirection.getStepX(), liftDirection.getStepY(), liftDirection.getStepZ()).scale(radius * 2);
            Vec3 start = centerVec.subtract(1.0, 1.0, 1.0);
            Vec3 end = centerVec.add(dirVec).add(1.0, 1.0, 1.0);
            return new AABB(start, end);
        }
        return new AABB(
                centerVec.x - radius, centerVec.y - radius, centerVec.z - radius,
                centerVec.x + radius, centerVec.y + radius, centerVec.z + radius
        );
    }

    public boolean contains(Vec3 pos) {
        if (mode == Mode.GRAV_LIFT) {
            return getBoundingBox().contains(pos);
        }
        return centerVec.distanceToSqr(pos) <= (radius * radius);
    }
}