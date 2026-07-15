package ddraig.net.entropica.api.pressure;

import ddraig.net.entropica.api.materia.MateriaStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import java.util.*;

public class MateriaGrid {
    private final Set<BlockPos> nodes = new HashSet<>();
    private final List<Edge> edges = new ArrayList<>();

    public record Edge(BlockPos posA, BlockPos posB, Direction directionFromAToB, float resistance, int transferRate) {}

    public Set<BlockPos> getNodes() {
        return nodes;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public void addNode(BlockPos pos) {
        nodes.add(pos);
    }

    public void addEdge(BlockPos posA, BlockPos posB, Direction dir, float resistance, int transferRate) {
        edges.add(new Edge(posA, posB, dir, resistance, transferRate));
    }

    public void clear() {
        nodes.clear();
        edges.clear();
    }

    public void tick(Level level) {
        if (level.isClientSide() || nodes.isEmpty()) return;

        // Resolve active handlers in loaded chunks
        Map<BlockPos, IPressureHandler> resolved = new HashMap<>();
        for (BlockPos pos : nodes) {
            if (level.isLoaded(pos)) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof IPressureHandler handler) {
                    resolved.put(pos, handler);
                    handler.setManagedByGraph(true);
                }
            }
        }

        // Solve flow along each edge
        for (Edge edge : edges) {
            IPressureHandler handlerA = resolved.get(edge.posA());
            IPressureHandler handlerB = resolved.get(edge.posB());

            if (handlerA == null || handlerB == null) continue;

            MateriaStack stackA = handlerA.getMateriaInTank();
            MateriaStack stackB = handlerB.getMateriaInTank();

            float pA = handlerA.getPressure();
            float pB = handlerB.getPressure();

            // Add virtual boost from adjacent Materia Pumps for A -> B
            float boostA = 0.0f;
            if (handlerA.getActiveBoostDirection() == edge.directionFromAToB()) {
                boostA = handlerA.getActiveBoostStrength();
            }

            // Add virtual boost from adjacent Materia Pumps for B -> A
            float boostB = 0.0f;
            if (handlerB.getActiveBoostDirection() == edge.directionFromAToB().getOpposite()) {
                boostB = handlerB.getActiveBoostStrength();
            }

            float pA_boosted = pA + boostA;
            float pB_boosted = pB + boostB;

            float deltaP_AtoB = pA_boosted - pB;
            float deltaP_BtoA = pB_boosted - pA;

            if (deltaP_AtoB > 1e-6f) {
                // Try flow A -> B
                if (!stackA.isEmpty()) {
                    int amountA = stackA.getAmount();
                    int amountB = stackB.getAmount();
                    boolean canFlow = (amountB == 0) || (boostA > 0.0f) || (amountA - amountB >= 2);
                    if (canFlow && (stackB.isEmpty() || stackA.getType() == stackB.getType())) {
                        float scale = 1.0f / (1.0f + edge.resistance());
                        int flow = (int) (amountA * 0.5f * deltaP_AtoB * scale);
                        int transferRate = Math.min(handlerA.getTransferRate(), handlerB.getTransferRate());
                        flow = Math.min(flow, transferRate);
                        flow = Math.min(flow, amountA);
                        flow = Math.max(1, flow);

                        if (flow > 0) {
                            MateriaStack toSend = stackA.copy();
                            toSend.setAmount(flow);
                            int accepted = handlerB.fill(toSend, false);
                            if (accepted > 0) {
                                handlerA.drain(accepted, false);
                                // Update local stack variables to reflect new values for potential reverse check
                                stackA = handlerA.getMateriaInTank();
                                stackB = handlerB.getMateriaInTank();
                                pA = handlerA.getPressure();
                                pB = handlerB.getPressure();
                            }
                        }
                    }
                }
            }

            // Recalculate B -> A pressure difference after potential A -> B flow
            pA_boosted = pA + boostA;
            pB_boosted = pB + boostB;
            deltaP_BtoA = pB_boosted - pA;

            if (deltaP_BtoA > 1e-6f) {
                // Try flow B -> A
                if (!stackB.isEmpty()) {
                    int amountA = stackA.getAmount();
                    int amountB = stackB.getAmount();
                    boolean canFlow = (amountA == 0) || (boostB > 0.0f) || (amountB - amountA >= 2);
                    if (canFlow && (stackA.isEmpty() || stackB.getType() == stackA.getType())) {
                        float scale = 1.0f / (1.0f + edge.resistance());
                        int flow = (int) (amountB * 0.5f * deltaP_BtoA * scale);
                        int transferRate = Math.min(handlerA.getTransferRate(), handlerB.getTransferRate());
                        flow = Math.min(flow, transferRate);
                        flow = Math.min(flow, amountB);
                        flow = Math.max(1, flow);

                        if (flow > 0) {
                            MateriaStack toSend = stackB.copy();
                            toSend.setAmount(flow);
                            int accepted = handlerA.fill(toSend, false);
                            if (accepted > 0) {
                                handlerB.drain(accepted, false);
                            }
                        }
                    }
                }
            }
        }

        // Post-flow checks: overpressure venting and rupture logic
        for (Map.Entry<BlockPos, IPressureHandler> entry : resolved.entrySet()) {
            BlockPos pos = entry.getKey();
            IPressureHandler handler = entry.getValue();

            // Tick venting / explosion helper
            PressureNetworkHelper.checkPressureThresholds(level, pos, handler);

            // Reset temp pump boost fields at the end of the tick
            handler.clearActiveBoost();
        }
    }
}
