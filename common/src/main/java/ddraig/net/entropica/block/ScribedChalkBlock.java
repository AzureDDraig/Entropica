package ddraig.net.entropica.block;

import ddraig.net.entropica.block.entity.ScribedChalkBlockEntity;
import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.item.ItemStack;
import ddraig.net.entropica.api.EssenceType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.redstone.Orientation;
import org.jetbrains.annotations.Nullable;

public class ScribedChalkBlock extends BaseEntityBlock {
    public static final IntegerProperty TIER = IntegerProperty.create("tier", 1, 4);
    public static final EnumProperty<NodeType> NODE_TYPE = EnumProperty.create("node_type", NodeType.class);
    public static final BooleanProperty CIRCUIT = BooleanProperty.create("circuit");

    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D);

    public ScribedChalkBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(TIER, 1)
                .setValue(NODE_TYPE, NodeType.DEFAULT)
                .setValue(CIRCUIT, false));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    public boolean canChalkSurvive(LevelReader level, BlockPos pos) {
        BlockPos belowPos = pos.below();
        return level.getBlockState(belowPos).isFaceSturdy(level, belowPos, Direction.UP);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean isMoving) {
        if (!canChalkSurvive(level, pos)) {
            if (!level.isClientSide()) {
                checkForAndDestroyCircle(level, pos, state);
            }
            level.destroyBlock(pos, true);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TIER, NODE_TYPE, CIRCUIT);
    }

    public static final com.mojang.serialization.MapCodec<ScribedChalkBlock> CODEC = simpleCodec(ScribedChalkBlock::new);

    @Override
    protected com.mojang.serialization.MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public net.minecraft.world.InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, net.minecraft.world.entity.player.Player player, net.minecraft.world.phys.BlockHitResult hitResult) {
        // Redirection logic for magic circles: click anywhere on the circle redirects to the closest node!
        BlockPos centerPos = null;
        int circleTier = 0;

        BlockEntity clickBE = level.getBlockEntity(pos);
        if (clickBE instanceof ScribedChalkBlockEntity chalkBE && chalkBE.isInActiveCircle()) {
            // Find the center OUTPUT block of the active circle
            for (int dx = -4; dx <= 4; dx++) {
                for (int dz = -4; dz <= 4; dz++) {
                    BlockPos checkPos = pos.offset(dx, 0, dz);
                    BlockState checkState = level.getBlockState(checkPos);
                    if (checkState.is(state.getBlock()) && checkState.getValue(NODE_TYPE) == NodeType.OUTPUT) {
                        BlockEntity be = level.getBlockEntity(checkPos);
                        if (be instanceof ScribedChalkBlockEntity cBE && cBE.isInActiveCircle()) {
                            // Find matching tier
                            for (int tier = 4; tier >= 1; tier--) {
                                if (checkPatternStatic(level, checkPos, checkState.getValue(CIRCUIT), tier) &&
                                    validateNodeCountsStatic(level, checkPos, tier)) {
                                    // Ensure this clicked block is on the boundary or center of this tier
                                    if (Math.max(Math.abs(pos.getX() - checkPos.getX()), Math.abs(pos.getZ() - checkPos.getZ())) <= tier || pos.equals(checkPos)) {
                                        centerPos = checkPos;
                                        circleTier = tier;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    if (centerPos != null) break;
                }
                if (centerPos != null) break;
            }
        }

        // If we are part of an active magic circle, find the closest node block based on its CURRENT SPINNING ANGLE!
        if (centerPos != null && circleTier > 0) {
            java.util.List<BlockPos> activeNodes = new java.util.ArrayList<>();
            for (int t = 1; t <= circleTier; t++) {
                int[][] offsets = getOffsetsForTier(t);
                for (int[] offset : offsets) {
                    BlockPos p = centerPos.offset(offset[0], 0, offset[1]);
                    BlockState pState = level.getBlockState(p);
                    if (pState.is(state.getBlock()) && pState.getValue(NODE_TYPE) != NodeType.DEFAULT) {
                        activeNodes.add(p);
                    }
                }
            }

            // Sort perimeter nodes by angle around center block
            BlockPos finalCenter = centerPos;
            activeNodes.sort((p1, p2) -> {
                double a1 = Math.atan2(p1.getZ() - finalCenter.getZ(), p1.getX() - finalCenter.getX());
                double a2 = Math.atan2(p2.getZ() - finalCenter.getZ(), p2.getX() - finalCenter.getX());
                if (a1 < 0) a1 += 2 * Math.PI;
                if (a2 < 0) a2 += 2 * Math.PI;
                return Double.compare(a1, a2);
            });

            // Calculate current rotation angle
            float speed = switch (circleTier) {
                case 1 -> 0.8f;
                case 2 -> 0.6f;
                case 3 -> 0.5f;
                default -> 0.4f;
            };
            double rotationAngle = Math.toRadians(-90.0f + level.getGameTime() * speed);

            BlockPos closestNode = centerPos;
            double hitX = hitResult.getLocation().x;
            double hitZ = hitResult.getLocation().z;
            
            // Check distance to static center block (Output node)
            double minDist = (centerPos.getX() + 0.5 - hitX) * (centerPos.getX() + 0.5 - hitX) + 
                              (centerPos.getZ() + 0.5 - hitZ) * (centerPos.getZ() + 0.5 - hitZ);
                              
            // Check distance to each spinning perimeter node's visual position
            int points = activeNodes.size();
            if (points > 0) {
                double angleStep = 2.0 * Math.PI / points;
                double polyRadius = (circleTier == 4) ? circleTier * 0.75f : circleTier * 0.7f;
                
                for (int i = 0; i < points; i++) {
                    double theta = rotationAngle + i * angleStep;
                    double vx = centerPos.getX() + 0.5 + polyRadius * Math.cos(theta);
                    double vz = centerPos.getZ() + 0.5 + polyRadius * Math.sin(theta);
                    
                    double dist = (vx - hitX) * (vx - hitX) + (vz - hitZ) * (vz - hitZ);
                    if (dist < minDist) {
                        minDist = dist;
                        closestNode = activeNodes.get(i);
                    }
                }
            }

            // Redirect interaction if closestNode is different from the currently clicked block pos!
            if (!closestNode.equals(pos)) {
                BlockState targetState = level.getBlockState(closestNode);
                if (targetState.getBlock() instanceof ScribedChalkBlock targetChalk) {
                    net.minecraft.world.phys.BlockHitResult redirectedHit = new net.minecraft.world.phys.BlockHitResult(
                        hitResult.getLocation(), hitResult.getDirection(), closestNode, hitResult.isInside()
                    );
                    return targetChalk.useWithoutItem(targetState, level, closestNode, player, redirectedHit);
                }
            }
        }

        // If they click the OUTPUT node with an empty hand, trigger the processing ritual!
        if (state.getValue(NODE_TYPE) == NodeType.OUTPUT && player.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND).isEmpty()) {
            int checkTier = 0;
            for (int t = 4; t >= 1; t--) {
                if (checkPatternStatic(level, pos, state.getValue(CIRCUIT), t) &&
                    validateNodeCountsStatic(level, pos, t)) {
                    checkTier = t;
                    break;
                }
            }
            if (checkTier > 0) {
                if (!level.isClientSide()) {
                    int amplifiers = 0;
                    int capacitors = 0;
                    int resonators = 0;
                    
                    for (int t = 1; t <= checkTier; t++) {
                        int[][] offsets = getOffsetsForTier(t);
                        for (int[] offset : offsets) {
                            BlockPos p = pos.offset(offset[0], 0, offset[1]);
                            BlockState neighborState = level.getBlockState(p);
                            if (neighborState.is(state.getBlock())) {
                                boolean isCircuit = neighborState.getValue(CIRCUIT);
                                NodeType type = neighborState.getValue(NODE_TYPE);
                                if (isCircuit) {
                                    if (type == NodeType.AMPLIFIER) amplifiers++;
                                    else if (type == NodeType.CAPACITOR) capacitors++;
                                    else if (type == NodeType.RESONATOR) resonators++;
                                }
                            }
                        }
                    }
                    
                    StringBuilder message = new StringBuilder("§5[Entropica] Magic Circle has begun processing the ritual!");
                    if (amplifiers > 0) {
                        double powerMult = 1.0 + amplifiers * 0.5;
                        message.append("\n§6 - Power Multiplier: ").append(String.format("%.1f", powerMult)).append("x (").append(amplifiers).append(" Amplifiers)");
                    }
                    if (capacitors > 0) {
                        double efficiencyMult = 1.0 - (1.0 - Math.pow(0.75, capacitors));
                        message.append("\n§e - Essence Efficiency: ").append(String.format("%.0f", (1.0 - efficiencyMult) * 100)).append("% cost reduction (").append(capacitors).append(" Capacitors)");
                    }
                    if (resonators > 0) {
                        double speedMult = 1.0 + resonators * 0.5;
                        message.append("\n§b - Processing Speed: ").append(String.format("%.1f", speedMult)).append("x (").append(resonators).append(" Resonators)");
                    }
                    
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal(message.toString()), false);
                    level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0f, 1.0f);
                    
                    if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                        serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.PORTAL, pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5, 30, 0.5, 0.1, 0.5, 0.1);
                        for (int t = 1; t <= checkTier; t++) {
                            int[][] offsets = getOffsetsForTier(t);
                            for (int[] offset : offsets) {
                                BlockPos p = pos.offset(offset[0], 0, offset[1]);
                                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD, p.getX() + 0.5, p.getY() + 0.1, p.getZ() + 0.5, 10, 0.2, 0.1, 0.2, 0.05);
                            }
                        }
                    }
                }
                return net.minecraft.world.InteractionResult.SUCCESS;
            }
        }

        if (state.getValue(NODE_TYPE) == NodeType.INPUT || state.getValue(NODE_TYPE) == NodeType.SOURCE) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ScribedChalkBlockEntity chalkBE) {
                net.minecraft.world.InteractionHand hand = net.minecraft.world.InteractionHand.MAIN_HAND;
                ItemStack heldItem = player.getItemInHand(hand);

                // Try to insert Orbis Cell
                if (heldItem.getItem() instanceof ddraig.net.entropica.item.OrbisCellItem && chalkBE.getStoredOrbisCell().isEmpty()) {
                    if (!level.isClientSide()) {
                        EssenceType type = ddraig.net.entropica.item.OrbisCellItem.getStoredVisType(heldItem);
                        if (type != null) {
                            ItemStack toStore = heldItem.copy();
                            toStore.setCount(1);
                            chalkBE.setStoredOrbisCell(toStore);
                            chalkBE.setActiveAffinity(type);
                            chalkBE.setColor(type.getColorInt());

                            heldItem.shrink(1);
                            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5f, 0.8f);
                        }
                    }
                    return net.minecraft.world.InteractionResult.SUCCESS;
                }

                // Try to insert Essence Item
                if (heldItem.getItem() instanceof ddraig.net.entropica.item.EssenceItem) {
                    if (!level.isClientSide()) {
                        EssenceType type = ddraig.net.entropica.item.EssenceItem.getEssenceType(heldItem);
                        if (type != null) {
                            chalkBE.setActiveAffinity(type);
                            chalkBE.setColor(type.getColorInt());

                            heldItem.shrink(1);
                            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5f, 1.2f);
                        }
                    }
                    return net.minecraft.world.InteractionResult.SUCCESS;
                }

                // Try to extract Orbis Cell
                if (!chalkBE.getStoredOrbisCell().isEmpty() && heldItem.isEmpty()) {
                    if (!level.isClientSide()) {
                        ItemStack extracted = chalkBE.getStoredOrbisCell();
                        if (!player.addItem(extracted)) {
                            player.drop(extracted, false);
                        }
                        chalkBE.setStoredOrbisCell(ItemStack.EMPTY);
                        chalkBE.setActiveAffinity(EssenceType.REGULAR);
                        chalkBE.setColor(0xFFCCCCCC);

                        level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5f, 1.5f);
                    }
                    return net.minecraft.world.InteractionResult.SUCCESS;
                }
            }
        }

        // Add Rune node insertion/extraction
        if (state.getValue(NODE_TYPE) == NodeType.RUNE) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ScribedChalkBlockEntity chalkBE) {
                net.minecraft.world.InteractionHand hand = net.minecraft.world.InteractionHand.MAIN_HAND;
                ItemStack heldItem = player.getItemInHand(hand);

                // Try to insert Rune
                if (heldItem.getItem() instanceof ddraig.net.entropica.item.RuneItem && chalkBE.getStoredRune().isEmpty()) {
                    if (!level.isClientSide()) {
                        ItemStack toStore = heldItem.copy();
                        toStore.setCount(1);
                        chalkBE.setStoredRune(toStore);
                        heldItem.shrink(1);
                        level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5f, 1.0f);
                    }
                    return net.minecraft.world.InteractionResult.SUCCESS;
                }

                // Try to extract Rune
                if (!chalkBE.getStoredRune().isEmpty() && heldItem.isEmpty()) {
                    if (!level.isClientSide()) {
                        ItemStack extracted = chalkBE.getStoredRune();
                        if (!player.addItem(extracted)) {
                            player.drop(extracted, false);
                        }
                        chalkBE.setStoredRune(ItemStack.EMPTY);
                        level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5f, 1.2f);
                    }
                    return net.minecraft.world.InteractionResult.SUCCESS;
                }
            }
        }

        return net.minecraft.world.InteractionResult.PASS;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, net.minecraft.world.entity.player.Player player) {
        if (!level.isClientSide()) {
            checkForAndDestroyCircle(level, pos, state);
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ScribedChalkBlockEntity chalkBE) {
                if (!chalkBE.getStoredOrbisCell().isEmpty()) {
                    Block.popResource(level, pos, chalkBE.getStoredOrbisCell());
                }
                if (!chalkBE.getStoredRune().isEmpty()) {
                    Block.popResource(level, pos, chalkBE.getStoredRune());
                }
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    private void checkForAndDestroyCircle(Level level, BlockPos brokenPos, BlockState brokenState) {
        boolean circuit = brokenState.getValue(CIRCUIT);
        
        // 1. If the broken block itself is an OUTPUT node, check if it was a center of a circle
        if (brokenState.getValue(NODE_TYPE) == NodeType.OUTPUT) {
            for (int tier = 4; tier >= 1; tier--) {
                if (checkPatternStatic(level, brokenPos, circuit, tier) && validateNodeCountsStatic(level, brokenPos, tier)) {
                    // Destroy all blocks in all concentric layers up to this tier
                    for (int t = 1; t <= tier; t++) {
                        int[][] offsets = getOffsetsForTier(t);
                        for (int[] offset : offsets) {
                            BlockPos p = brokenPos.offset(offset[0], 0, offset[1]);
                            if (level.getBlockState(p).is(brokenState.getBlock())) {
                                level.destroyBlock(p, true);
                            }
                        }
                    }
                    break;
                }
            }
        }
        
        // 2. Check if the broken block was a perimeter block for a nearby OUTPUT node
        for (int tier = 1; tier <= 4; tier++) {
            int[][] offsets = getOffsetsForTier(tier);
            for (int[] offset : offsets) {
                BlockPos centerPos = brokenPos.offset(-offset[0], 0, -offset[1]);
                BlockState centerState = level.getBlockState(centerPos);
                if (centerState.is(brokenState.getBlock()) && centerState.getValue(NODE_TYPE) == NodeType.OUTPUT && centerState.getValue(CIRCUIT) == circuit) {
                    if (checkPatternStaticExcept(level, centerPos, circuit, tier, brokenPos)) {
                        // Destroy center and all other remaining perimeter blocks across all layers
                        level.destroyBlock(centerPos, true);
                        for (int t = 1; t <= tier; t++) {
                            int[][] o = getOffsetsForTier(t);
                            for (int[] offset2 : o) {
                                BlockPos p = centerPos.offset(offset2[0], 0, offset2[1]);
                                if (level.getBlockState(p).is(brokenState.getBlock())) {
                                    level.destroyBlock(p, true);
                                }
                            }
                        }
                        return; // Found and destroyed
                    }
                }
            }
        }
    }

    public static boolean validateNodeCountsStatic(Level level, BlockPos center, int tier) {
        int inputCount = 0;
        int sourceCount = 0;
        int runeCount = 0;
        int specialCount = 0;
        
        for (int t = 1; t <= tier; t++) {
            int[][] offsets = getOffsetsForTier(t);
            for (int[] offset : offsets) {
                BlockPos pos = center.offset(offset[0], 0, offset[1]);
                BlockState state = level.getBlockState(pos);
                if (state.is(ddraig.net.entropica.registry.ModBlocks.SCRIBED_CHALK.get())) {
                    boolean isCircuit = state.getValue(CIRCUIT);
                    NodeType type = state.getValue(NODE_TYPE);
                    if (type == NodeType.INPUT) inputCount++;
                    else if (type == NodeType.SOURCE) sourceCount++;
                    else if (type == NodeType.RUNE) runeCount++;
                    else if (isCircuit && (type == NodeType.AMPLIFIER || type == NodeType.CAPACITOR || type == NodeType.RESONATOR)) {
                        specialCount++;
                    }
                }
            }
        }
        
        int totalNodes = inputCount + sourceCount + runeCount + specialCount;
        int expectedTotal = 2 * tier + 1;
        
        return inputCount >= 1 && sourceCount >= 1 && totalNodes == expectedTotal;
    }

    public static boolean checkPatternStatic(Level level, BlockPos center, boolean circuit, int tier) {
        for (int t = 1; t <= tier; t++) {
            int[][] offsets = getOffsetsForTier(t);
            for (int[] offset : offsets) {
                BlockPos pos = center.offset(offset[0], 0, offset[1]);
                BlockState state = level.getBlockState(pos);
                if (!state.is(ddraig.net.entropica.registry.ModBlocks.SCRIBED_CHALK.get()) || state.getValue(CIRCUIT) != circuit) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean checkPatternStaticExcept(Level level, BlockPos center, boolean circuit, int tier, BlockPos exceptPos) {
        for (int t = 1; t <= tier; t++) {
            int[][] offsets = getOffsetsForTier(t);
            for (int[] offset : offsets) {
                BlockPos pos = center.offset(offset[0], 0, offset[1]);
                if (pos.equals(exceptPos)) continue;
                BlockState state = level.getBlockState(pos);
                if (!state.is(ddraig.net.entropica.registry.ModBlocks.SCRIBED_CHALK.get()) || state.getValue(CIRCUIT) != circuit) {
                    return false;
                }
            }
        }
        return true;
    }

    public static int[][] getOffsetsForTier(int tier) {
        return switch (tier) {
            case 1 -> new int[][]{{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
            case 2 -> new int[][]{{0, -2}, {1, -1}, {2, 0}, {1, 1}, {0, 2}, {-1, 1}, {-2, 0}, {-1, -1}};
            case 3 -> new int[][]{
                {0, -3}, {1, -3}, {2, -2}, {3, -1}, {3, 1}, {2, 2}, {1, 3}, {0, 3}, {-1, 3}, {-2, 2}, {-3, 1}, {-3, -1}, {-2, -2}, {-1, -3}
            };
            default -> new int[][]{
                {0, -4}, {1, -4}, {2, -3}, {3, -2}, {4, -1}, {4, 0}, {4, 1}, {3, 2}, {2, 3}, {1, 4}, {0, 4}, {-1, 4}, {-2, 3}, {-3, 2}, {-4, 1}, {-4, 0}, {-4, -1}, {-3, -2}, {-2, -3}, {-1, -4}
            };
        };
    }
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ScribedChalkBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, ModBlockEntities.SCRIBED_CHALK_BE.get(), ScribedChalkBlockEntity::tick);
    }

    public enum NodeType implements StringRepresentable {
        DEFAULT("default"),
        INPUT("input"),
        SOURCE("source"),
        OUTPUT("output"),
        RUNE("rune"),
        COLLECTION("collection"),
        AMPLIFIER("amplifier"),
        CAPACITOR("capacitor"),
        RESONATOR("resonator"),
        DIODE("diode");

        private final String name;

        NodeType(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
