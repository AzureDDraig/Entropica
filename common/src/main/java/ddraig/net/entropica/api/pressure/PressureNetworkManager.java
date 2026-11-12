package ddraig.net.entropica.api.pressure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import java.util.*;

public class PressureNetworkManager {
    private static final Map<Level, List<MateriaGrid>> GRIDS = new WeakHashMap<>();
    private static final Map<Level, Long> LAST_TICK_TIMES = new WeakHashMap<>();

    public static void tick(Level level) {
        if (level.isClientSide()) return;
        long gameTime = level.getGameTime();
        Long lastTick = LAST_TICK_TIMES.get(level);
        if (lastTick != null && lastTick == gameTime) return;

        LAST_TICK_TIMES.put(level, gameTime);

        List<MateriaGrid> grids = GRIDS.get(level);
        if (grids != null) {
            grids.removeIf(grid -> grid.getNodes().isEmpty());
            for (MateriaGrid grid : new ArrayList<>(grids)) {
                grid.tick(level);
            }
        }
    }

    public static void onBlockAdded(Level level, BlockPos pos, IPressureHandler handler) {
        if (level.isClientSide()) return;
        rebuildNetworkAround(level, pos);
    }

    public static void onBlockEntityRemoved(BlockEntity be) {
        Level level = be.getLevel();
        if (level != null && !level.isClientSide()) {
            BlockPos pos = be.getBlockPos();
            if (level.hasChunkAt(pos)) {
                BlockState currentState = level.getBlockState(pos);
                if (currentState.getBlock() != be.getBlockState().getBlock()) {
                    onBlockRemoved(level, pos);
                }
            }
        }
    }

    public static void onBlockRemoved(Level level, BlockPos pos) {
        if (level.isClientSide()) return;

        // Remove pos from any existing grids
        List<MateriaGrid> grids = GRIDS.get(level);
        if (grids != null) {
            MateriaGrid parentGrid = null;
            for (MateriaGrid grid : grids) {
                if (grid.getNodes().contains(pos)) {
                    parentGrid = grid;
                    break;
                }
            }

            if (parentGrid != null) {
                parentGrid.getNodes().remove(pos);
                for (BlockPos node : parentGrid.getNodes()) {
                    BlockEntity be = level.getBlockEntity(node);
                    if (be instanceof IPressureHandler handler) {
                        handler.setManagedByGraph(false);
                    }
                }
                grids.remove(parentGrid);

                // Rebuild networks for remaining neighbors
                for (Direction dir : Direction.values()) {
                    BlockPos neighborPos = pos.relative(dir);
                    rebuildNetworkAround(level, neighborPos);
                }
            }
        }
    }

    private static void rebuildNetworkAround(Level level, BlockPos startPos) {
        BlockEntity startBE = level.getBlockEntity(startPos);
        if (!(startBE instanceof IPressureHandler startHandler)) return;

        // Run a BFS to discover all contiguous connected nodes of the same network type
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();
        List<MateriaGrid.Edge> edges = new ArrayList<>();

        queue.add(startPos);
        visited.add(startPos);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            BlockEntity currentBE = level.getBlockEntity(current);
            if (!(currentBE instanceof IPressureHandler currentHandler)) continue;

            BlockState state = currentBE.getBlockState();

            for (Direction dir : Direction.values()) {
                BooleanProperty prop = getDirectionProperty(dir);
                if (state.hasProperty(prop) && state.getValue(prop)) {
                    BlockPos neighborPos = current.relative(dir);
                    if (visited.contains(neighborPos)) continue;

                    BlockEntity neighborBE = level.getBlockEntity(neighborPos);
                    if (neighborBE instanceof IPressureHandler neighborHandler) {
                        // Add edge exactly once
                        if (current.compareTo(neighborPos) < 0) {
                            float resistance = currentHandler.getResistance() + neighborHandler.getResistance();
                            int transferRate = getTransferRate(currentBE, neighborBE);
                            edges.add(new MateriaGrid.Edge(current, neighborPos, dir, resistance, transferRate));
                        }

                        visited.add(neighborPos);
                        queue.add(neighborPos);
                    }
                }
            }
        }

        // Unregister visited nodes from any existing grids to merge them
        List<MateriaGrid> grids = GRIDS.computeIfAbsent(level, k -> new ArrayList<>());
        grids.removeIf(grid -> {
            boolean hasIntersection = false;
            for (BlockPos pos : visited) {
                if (grid.getNodes().contains(pos)) {
                    hasIntersection = true;
                    break;
                }
            }
            if (hasIntersection) {
                for (BlockPos pos : grid.getNodes()) {
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof IPressureHandler handler) {
                        handler.setManagedByGraph(false);
                    }
                }
                return true;
            }
            return false;
        });

        // Create new merged grid
        MateriaGrid newGrid = new MateriaGrid();
        for (BlockPos pos : visited) {
            newGrid.addNode(pos);
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof IPressureHandler handler) {
                handler.setManagedByGraph(true);
            }
        }
        for (MateriaGrid.Edge edge : edges) {
            newGrid.addEdge(edge.posA(), edge.posB(), edge.directionFromAToB(), edge.resistance(), edge.transferRate());
        }

        grids.add(newGrid);
    }

    private static BooleanProperty getDirectionProperty(Direction direction) {
        return switch (direction) {
            case NORTH -> BlockStateProperties.NORTH;
            case SOUTH -> BlockStateProperties.SOUTH;
            case EAST -> BlockStateProperties.EAST;
            case WEST -> BlockStateProperties.WEST;
            case UP -> BlockStateProperties.UP;
            case DOWN -> BlockStateProperties.DOWN;
        };
    }

    private static int getTransferRate(BlockEntity beA, BlockEntity beB) {
        int rateA = beA instanceof IPressureHandler handler ? handler.getTransferRate() : 20;
        int rateB = beB instanceof IPressureHandler handler ? handler.getTransferRate() : 20;
        return Math.min(rateA, rateB);
    }
}
