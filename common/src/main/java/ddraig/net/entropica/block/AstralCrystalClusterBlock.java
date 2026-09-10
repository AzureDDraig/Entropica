package ddraig.net.entropica.block;

import ddraig.net.entropica.block.entity.AstralCrystalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

/**
 * AstralCrystalClusterBlock — AmethystClusterBlock variant that delegates in-world rendering
 * to the procedural AstralCrystalRenderer via RenderShape.INVISIBLE, with custom dynamic
 * multi-faceted hitboxes tailored to each growing stage.
 */
public class AstralCrystalClusterBlock extends AmethystClusterBlock implements EntityBlock {

    private final int stage;
    private final Map<Direction, VoxelShape> stageShapes = new EnumMap<>(Direction.class);

    public AstralCrystalClusterBlock(int stage, int height, int xzOffset, Properties properties) {
        super(height, xzOffset, properties);
        this.stage = stage;
        initShapes();
    }

    private void initShapes() {
        for (Direction dir : Direction.values()) {
            stageShapes.put(dir, buildShapeFor(this.stage, dir));
        }
    }

    private static VoxelShape buildShapeFor(int stage, Direction dir) {
        return switch (stage) {
            case 1 -> Shapes.or(
                    makeBox(dir, 5.5, 0.0, 5.5, 10.5, 7.5, 10.5),
                    makeBox(dir, 4.0, 0.0, 4.0, 7.5, 4.0, 7.5)
            );
            case 2 -> Shapes.or(
                    makeBox(dir, 5.0, 0.0, 5.0, 11.0, 10.5, 11.0),
                    makeBox(dir, 3.5, 0.0, 7.0, 8.0, 7.5, 11.5),
                    makeBox(dir, 7.5, 0.0, 4.0, 11.5, 5.0, 8.0)
            );
            case 3 -> Shapes.or(
                    makeBox(dir, 4.5, 0.0, 4.5, 11.5, 13.5, 11.5),
                    makeBox(dir, 2.5, 0.0, 6.0, 7.5, 9.5, 12.0),
                    makeBox(dir, 8.0, 0.0, 2.0, 13.5, 8.5, 7.5),
                    makeBox(dir, 3.0, 0.0, 2.5, 8.0, 5.5, 7.5)
            );
            default -> Shapes.or(
                    makeBox(dir, 4.0, 0.0, 4.0, 12.0, 15.5, 12.0),
                    makeBox(dir, 1.5, 0.0, 1.5, 7.5, 11.0, 7.5),
                    makeBox(dir, 8.5, 0.0, 8.5, 14.5, 10.5, 14.5),
                    makeBox(dir, 8.5, 0.0, 1.5, 14.5, 9.5, 7.5),
                    makeBox(dir, 1.5, 0.0, 8.5, 7.5, 9.0, 14.5),
                    makeBox(dir, 1.0, 0.0, 1.0, 15.0, 5.0, 15.0)
            );
        };
    }

    private static VoxelShape makeBox(Direction dir, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return switch (dir) {
            case UP -> Block.box(minX, minY, minZ, maxX, maxY, maxZ);
            case DOWN -> Block.box(minX, 16.0 - maxY, minZ, maxX, 16.0 - minY, maxZ);
            case NORTH -> Block.box(minX, minZ, 16.0 - maxY, maxX, maxZ, 16.0 - minY);
            case SOUTH -> Block.box(minX, 16.0 - maxZ, minY, maxX, 16.0 - minZ, maxY);
            case WEST -> Block.box(16.0 - maxY, minX, minZ, 16.0 - minY, maxX, maxZ);
            case EAST -> Block.box(minY, 16.0 - maxX, minZ, maxY, 16.0 - minX, maxZ);
        };
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction dir = state.getValue(FACING);
        VoxelShape shape = stageShapes.get(dir);
        return shape != null ? shape : super.getShape(state, level, pos, context);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction dir = state.getValue(FACING);
        VoxelShape shape = stageShapes.get(dir);
        return shape != null ? shape : super.getCollisionShape(state, level, pos, context);
    }

    public int getStage() {
        return this.stage;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AstralCrystalBlockEntity(pos, state, this.stage);
    }
}
