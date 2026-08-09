package ddraig.net.entropica.block;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.block.entity.MateriaBlessingBlockEntity;
import ddraig.net.entropica.registry.ModBlockEntities;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Abstract base for Materia Blessing multiblock crystals.
 * Provides shared logic for multiblock placement, cascading destruction,
 * and block entity management. Subclasses define dimensions and drops.
 */
public abstract class AbstractMateriaBlessingBlock extends BaseEntityBlock {

    private static boolean destroying = false;

    protected AbstractMateriaBlessingBlock(Properties properties) {
        super(properties.noOcclusion());
        this.registerDefaultState(this.stateDefinition.any().setValue(getPartProperty(), 0));
    }

    // ─────── Subclass-defined dimensions ───────

    /** The IntegerProperty for PART (range depends on subclass) */
    public abstract IntegerProperty getPartProperty();

    /** Width of the multiblock (X axis) */
    public abstract int getWidth();

    /** Height of the multiblock (Y axis) */
    public abstract int getHeight();

    /** Depth of the multiblock (Z axis) */
    public abstract int getDepth();

    /** Total number of parts (width * height * depth) */
    public int getPartCount() {
        return getWidth() * getHeight() * getDepth();
    }

    /** Number of shards to drop (min) */
    public abstract int getMinShardDrop();

    /** Number of additional random shards */
    public abstract int getShardDropRange();

    // ─────── Part ↔ Position Mapping ───────

    public BlockPos getMasterPos(BlockPos pos, BlockState state) {
        int part = state.getValue(getPartProperty());
        int w = getWidth();
        int d = getDepth();
        int dx = part % w;
        int dz = (part / w) % d;
        int dy = part / (w * d);
        return pos.offset(-dx, -dy, -dz);
    }

    public BlockPos getPartPos(BlockPos masterPos, int part) {
        int w = getWidth();
        int d = getDepth();
        int dx = part % w;
        int dz = (part / w) % d;
        int dy = part / (w * d);
        return masterPos.offset(dx, dy, dz);
    }

    // ─────── Block State ───────

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(getPartProperty());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getCrystalShape(state);
    }

    /**
     * Returns a VoxelShape for this specific part of the multiblock that approximates
     * the crystal geometry passing through this block position.
     *
     * The crystal is centered on the multiblock footprint and its cross-section
     * varies with height (wider at mid-section, narrow at tips).
     *
     * Crystal ring profile (in blocks, at scale=1.0):
     * Y=0.0: R=0.0  (bottom tip)
     * Y=0.6: R=0.42 (widening)
     * Y=1.0: R=0.55 (maximum width)
     * Y=1.6: R=0.42 (narrowing)
     * Y=2.4: R=0.0  (top tip)
     */
    protected VoxelShape getCrystalShape(BlockState state) {
        int part = state.getValue(getPartProperty());
        int w = getWidth();
        int d = getDepth();
        int dx = part % w;
        int dz = (part / w) % d;
        int dy = part / (w * d);

        // Crystal center relative to the multiblock origin (block units)
        float centerX = (w - 1) / 2.0f;
        float centerZ = (d - 1) / 2.0f;

        // This block's position relative to the crystal center
        float blockMinX = dx - centerX - 0.5f;
        float blockMaxX = dx - centerX + 0.5f;
        float blockMinZ = dz - centerZ - 0.5f;
        float blockMaxZ = dz - centerZ + 0.5f;

        // Crystal radius at this height layer (scale 1.0 for lesser)
        // The crystal profile: bottom tip at Y=0, max width at Y~1.0, top tip at Y~2.4
        float layerMinY = dy;
        float layerMaxY = dy + 1;

        // Get the maximum crystal radius in this Y range
        float maxRadius = getMaxRadiusInRange(layerMinY, layerMaxY);

        if (maxRadius < 0.05f) {
            // Crystal doesn't pass through this block at all
            return Shapes.empty();
        }

        // Check if the crystal column overlaps this block's XZ bounds
        // Crystal is a cylinder centered at (centerX+0.5, centerZ+0.5) in block coords
        // Simplify: create a box representing the crystal cross-section at this height
        float crystalMinX = -maxRadius + 0.5f;
        float crystalMaxX = maxRadius + 0.5f;
        float crystalMinZ = -maxRadius + 0.5f;
        float crystalMaxZ = maxRadius + 0.5f;

        // Offset to be relative to THIS block's coordinate space
        float shapeMinX = Math.max(0, (centerX + crystalMinX) - dx);
        float shapeMaxX = Math.min(1, (centerX + crystalMaxX) - dx);
        float shapeMinZ = Math.max(0, (centerZ + crystalMinZ) - dz);
        float shapeMaxZ = Math.min(1, (centerZ + crystalMaxZ) - dz);

        if (shapeMinX >= shapeMaxX || shapeMinZ >= shapeMaxZ) {
            return Shapes.empty();
        }

        // Get the minimum crystal radius (for the narrow end of this layer)
        float minRadius = getMinRadiusInRange(layerMinY, layerMaxY);

        // Create a tapered shape: narrower box at the narrow end
        // For simplicity, use a single box that bounds the crystal section
        return Shapes.box(
                shapeMinX, 0, shapeMinZ,
                shapeMaxX, 1, shapeMaxZ
        );
    }

    /**
     * Returns the maximum crystal radius within the given Y range.
     * Y values are in block-local coordinates (0 = bottom of multiblock).
     * Crystal profile height is ~2.4 blocks (at scale 1.0 for Lesser).
     */
    private float getMaxRadiusInRange(float minY, float maxY) {
        float scale = getHeight() / 3.0f; // 1.0 for Lesser (3 high), 1.33 for Greater (4 high)
        // Crystal ring data (Y, Radius) at base scale
        float[][] rings = {
                {0.0f, 0.0f}, {0.15f, 0.12f}, {0.4f, 0.28f}, {0.6f, 0.42f},
                {0.85f, 0.52f}, {1.0f, 0.55f}, {1.3f, 0.50f}, {1.6f, 0.42f},
                {1.9f, 0.32f}, {2.15f, 0.20f}, {2.35f, 0.08f}, {2.4f, 0.0f}
        };

        float maxR = 0;
        for (float[] ring : rings) {
            float ringY = ring[0] * scale + 0.3f; // scale + 0.3 offset for floating
            if (ringY >= minY && ringY <= maxY) {
                maxR = Math.max(maxR, ring[1] * scale);
            }
        }
        // Also check the Y bounds themselves via interpolation
        maxR = Math.max(maxR, interpolateRadius(minY, rings, scale));
        maxR = Math.max(maxR, interpolateRadius(maxY, rings, scale));
        return maxR;
    }

    private float getMinRadiusInRange(float minY, float maxY) {
        float scale = getHeight() / 3.0f;
        float[][] rings = {
                {0.0f, 0.0f}, {0.15f, 0.12f}, {0.4f, 0.28f}, {0.6f, 0.42f},
                {0.85f, 0.52f}, {1.0f, 0.55f}, {1.3f, 0.50f}, {1.6f, 0.42f},
                {1.9f, 0.32f}, {2.15f, 0.20f}, {2.35f, 0.08f}, {2.4f, 0.0f}
        };
        return Math.min(interpolateRadius(minY, rings, scale), interpolateRadius(maxY, rings, scale));
    }

    private float interpolateRadius(float y, float[][] rings, float scale) {
        float adjustedY = (y - 0.3f) / scale; // undo the floating offset and scaling
        if (adjustedY <= 0) return 0;
        if (adjustedY >= 2.4f) return 0;
        for (int i = 0; i < rings.length - 1; i++) {
            if (adjustedY >= rings[i][0] && adjustedY <= rings[i + 1][0]) {
                float t = (adjustedY - rings[i][0]) / (rings[i + 1][0] - rings[i][0]);
                return (rings[i][1] + t * (rings[i + 1][1] - rings[i][1])) * scale;
            }
        }
        return 0;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    // ─────── Block Entity ───────

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (state.getValue(getPartProperty()) == 0) {
            return new MateriaBlessingBlockEntity(pos, state);
        }
        return null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return createTickerHelper(type, ModBlockEntities.MATERIA_BLESSING_BE.get(),
                    (lvl, pos, st, be) -> be.clientTick(lvl, pos, st));
        } else {
            return createTickerHelper(type, ModBlockEntities.MATERIA_BLESSING_BE.get(),
                    (lvl, pos, st, be) -> be.serverTick(lvl, pos, st));
        }
    }

    // ─────── Placement ───────

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();

        for (int i = 0; i < getPartCount(); i++) {
            BlockPos partPos = getPartPos(pos, i);
            if (!level.getBlockState(partPos).canBeReplaced(context) || !level.getWorldBorder().isWithinBounds(partPos)) {
                return null;
            }
        }
        return this.defaultBlockState().setValue(getPartProperty(), 0);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        for (int i = 1; i < getPartCount(); i++) {
            BlockPos partPos = getPartPos(pos, i);
            level.setBlock(partPos, state.setValue(getPartProperty(), i), 3);
        }
    }

    // ─────── Cascading Destruction ───────

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!destroying) {
            destroying = true;
            BlockPos masterPos = getMasterPos(pos, state);
            for (int i = 0; i < getPartCount(); i++) {
                BlockPos partPos = getPartPos(masterPos, i);
                BlockState partState = level.getBlockState(partPos);
                if (partState.is(this)) {
                    if (i == 0 && !player.isCreative()) {
                        int count = getMinShardDrop() + level.random.nextInt(getShardDropRange() + 1);
                        ItemStack shard = new ItemStack(ModItems.MATERIA_BLESSING_SHARD.get(), count);
                        Block.popResource(level, partPos, shard);
                    }
                    if (!partPos.equals(pos)) {
                        level.destroyBlock(partPos, false, player);
                    }
                }
            }
            destroying = false;
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
