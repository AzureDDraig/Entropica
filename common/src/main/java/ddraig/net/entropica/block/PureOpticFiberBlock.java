package ddraig.net.entropica.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;
import java.util.Map;

import ddraig.net.entropica.block.entity.PureOpticFiberBlockEntity;
import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

public class PureOpticFiberBlock extends Block implements EntityBlock {
    public static final MapCodec<PureOpticFiberBlock> CODEC = simpleCodec(PureOpticFiberBlock::new);

    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST  = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST  = BlockStateProperties.WEST;
    public static final BooleanProperty UP    = BlockStateProperties.UP;
    public static final BooleanProperty DOWN  = BlockStateProperties.DOWN;

    public static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = new EnumMap<>(Direction.class);

    static {
        PROPERTY_BY_DIRECTION.put(Direction.NORTH, NORTH);
        PROPERTY_BY_DIRECTION.put(Direction.EAST,  EAST);
        PROPERTY_BY_DIRECTION.put(Direction.SOUTH, SOUTH);
        PROPERTY_BY_DIRECTION.put(Direction.WEST,  WEST);
        PROPERTY_BY_DIRECTION.put(Direction.UP,    UP);
        PROPERTY_BY_DIRECTION.put(Direction.DOWN,  DOWN);
    }

    private static final VoxelShape CORE = Block.box(6.0, 6.0, 6.0, 10.0, 10.0, 10.0);
    private static final VoxelShape ARM_NORTH = Block.box(6.0, 6.0, 0.0, 10.0, 10.0, 6.0);
    private static final VoxelShape ARM_SOUTH = Block.box(6.0, 6.0, 10.0, 10.0, 10.0, 16.0);
    private static final VoxelShape ARM_WEST  = Block.box(0.0, 6.0, 6.0, 6.0, 10.0, 10.0);
    private static final VoxelShape ARM_EAST  = Block.box(10.0, 6.0, 6.0, 16.0, 10.0, 10.0);
    private static final VoxelShape ARM_DOWN  = Block.box(6.0, 0.0, 6.0, 10.0, 6.0, 10.0);
    private static final VoxelShape ARM_UP    = Block.box(6.0, 10.0, 6.0, 10.0, 16.0, 10.0);

    public static final net.minecraft.world.level.block.state.properties.IntegerProperty COLOR_INDEX = net.minecraft.world.level.block.state.properties.IntegerProperty.create("color_index", 0, 16);

    public PureOpticFiberBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(UP, false)
                .setValue(DOWN, false)
                .setValue(COLOR_INDEX, 0));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN, COLOR_INDEX);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PureOpticFiberBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, ModBlockEntities.PURE_OPTIC_FIBER_BE.get(), PureOpticFiberBlockEntity::tick);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> serverType, BlockEntityType<E> clientType, BlockEntityTicker<? super E> ticker) {
        return clientType == serverType ? (BlockEntityTicker<A>) ticker : null;
    }

    public static boolean canConnectTo(BlockGetter level, BlockPos pos, Direction direction, BlockState currentState) {
        BlockPos neighborPos = pos.relative(direction);
        BlockState neighborState = level.getBlockState(neighborPos);
        Block block = neighborState.getBlock();

        if (block instanceof PureOpticFiberBlock) {
            int currentColor = currentState != null ? currentState.getValue(COLOR_INDEX) : 0;
            int neighborColor = neighborState.getValue(COLOR_INDEX);
            // If either is undyed (0), or colors match, connect! If distinct colors, isolate and do not connect.
            return currentColor == 0 || neighborColor == 0 || currentColor == neighborColor;
        }

        return block instanceof OpticalTransmitterPortBlock
                || block instanceof OpticalReceiverPortBlock
                || block instanceof OpticalBoosterAmplifierBlock
                || block instanceof OpticReceiverBlock
                || block instanceof OpticTransmitterBlock
                || block instanceof CagedOpticBulbBlock
                || block instanceof LampPostBlock;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockGetter level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState defaultState = this.defaultBlockState();
        return defaultState
                .setValue(NORTH, canConnectTo(level, pos, Direction.NORTH, defaultState))
                .setValue(EAST,  canConnectTo(level, pos, Direction.EAST, defaultState))
                .setValue(SOUTH, canConnectTo(level, pos, Direction.SOUTH, defaultState))
                .setValue(WEST,  canConnectTo(level, pos, Direction.WEST, defaultState))
                .setValue(UP,    canConnectTo(level, pos, Direction.UP, defaultState))
                .setValue(DOWN,  canConnectTo(level, pos, Direction.DOWN, defaultState));
    }

    @Override
    protected net.minecraft.world.InteractionResult useItemOn(net.minecraft.world.item.ItemStack stack, BlockState state, net.minecraft.world.level.Level level, BlockPos pos, net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand, net.minecraft.world.phys.BlockHitResult hitResult) {
        // 1. Astral Linking Wand: Network Diagnostic Ping
        if (stack.getItem() instanceof ddraig.net.entropica.item.AstralLinkingWandItem) {
            if (!level.isClientSide()) {
                performNetworkPing(level, pos, player);
            }
            return net.minecraft.world.InteractionResult.SUCCESS;
        }

        // 2. Sponge / Water Bottle / Water Bucket: De-Coloring (Cleanses back to Universal White)
        if (stack.is(net.minecraft.world.item.Items.SPONGE)
                || stack.is(net.minecraft.world.item.Items.WET_SPONGE)
                || stack.is(net.minecraft.world.item.Items.WATER_BUCKET)
                || (stack.is(net.minecraft.world.item.Items.POTION) && net.minecraft.world.item.alchemy.PotionContents.EMPTY.equals(stack.get(net.minecraft.core.component.DataComponents.POTION_CONTENTS)) || isWaterPotion(stack))) {
            if (state.getValue(COLOR_INDEX) != 0) {
                int cleansedCount = 0;
                int maxCleanse = player.isShiftKeyDown() ? 8 : 1;

                java.util.Queue<BlockPos> queue = new java.util.ArrayDeque<>();
                java.util.Set<BlockPos> visited = new java.util.HashSet<>();
                queue.add(pos);
                visited.add(pos);

                while (!queue.isEmpty() && cleansedCount < maxCleanse) {
                    BlockPos current = queue.poll();
                    BlockState curState = level.getBlockState(current);
                    if (curState.getBlock() instanceof PureOpticFiberBlock) {
                        if (curState.getValue(COLOR_INDEX) != 0) {
                            level.setBlock(current, curState.setValue(COLOR_INDEX, 0), 3);
                            cleansedCount++;
                        }
                        if (cleansedCount < maxCleanse) {
                            for (Direction dir : Direction.values()) {
                                BlockPos neighbor = current.relative(dir);
                                if (!visited.contains(neighbor) && level.getBlockState(neighbor).getBlock() instanceof PureOpticFiberBlock) {
                                    visited.add(neighbor);
                                    queue.add(neighbor);
                                }
                            }
                        }
                    }
                }

                if (!level.isClientSide()) {
                    level.playSound(null, pos, net.minecraft.sounds.SoundEvents.GENERIC_SPLASH, net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.4f);
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal("§b[Optic Fiber] §7Cleansed §f" + cleansedCount + " §7conduit casing(s) back to §fUNIVERSAL WHITE"), true);
                }
                return net.minecraft.world.InteractionResult.SUCCESS;
            }
        }

        // 3. Dye Item: Quick-Dyeing (Single click for 1 block, Sneak + Click propagates up to 8 contiguous blocks)
        if (stack.getItem() instanceof net.minecraft.world.item.DyeItem dyeItem) {
            net.minecraft.world.item.DyeColor dyeColor = dyeItem.getDyeColor();
            int newColorIdx = dyeColor.getId() + 1;
            int maxDye = player.isShiftKeyDown() ? 8 : 1;

            java.util.Queue<BlockPos> queue = new java.util.ArrayDeque<>();
            java.util.Set<BlockPos> visited = new java.util.HashSet<>();
            queue.add(pos);
            visited.add(pos);
            int dyedCount = 0;

            while (!queue.isEmpty() && dyedCount < maxDye) {
                BlockPos current = queue.poll();
                BlockState curState = level.getBlockState(current);
                if (curState.getBlock() instanceof PureOpticFiberBlock) {
                    if (curState.getValue(COLOR_INDEX) != newColorIdx) {
                        level.setBlock(current, curState.setValue(COLOR_INDEX, newColorIdx), 3);
                        dyedCount++;
                    }
                    if (dyedCount < maxDye) {
                        for (Direction dir : Direction.values()) {
                            BlockPos neighbor = current.relative(dir);
                            if (!visited.contains(neighbor) && level.getBlockState(neighbor).getBlock() instanceof PureOpticFiberBlock) {
                                visited.add(neighbor);
                                queue.add(neighbor);
                            }
                        }
                    }
                }
            }

            if (dyedCount > 0) {
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                if (!level.isClientSide()) {
                    level.playSound(null, pos, net.minecraft.sounds.SoundEvents.DYE_USE, net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal("§b[Optic Fiber] §7Dyed §f" + dyedCount + " §7conduit casing(s) to §f" + dyeColor.getName().toUpperCase()), true);
                }
                return net.minecraft.world.InteractionResult.SUCCESS;
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    private static boolean isWaterPotion(net.minecraft.world.item.ItemStack stack) {
        net.minecraft.world.item.alchemy.PotionContents contents = stack.get(net.minecraft.core.component.DataComponents.POTION_CONTENTS);
        if (contents != null && contents.potion().isPresent()) {
            return contents.potion().get().is(net.minecraft.world.item.alchemy.Potions.WATER);
        }
        return false;
    }

    private static void performNetworkPing(net.minecraft.world.level.Level level, BlockPos startPos, net.minecraft.world.entity.player.Player player) {
        java.util.Set<BlockPos> visited = new java.util.HashSet<>();
        java.util.Queue<BlockPos> queue = new java.util.ArrayDeque<>();
        queue.add(startPos);
        visited.add(startPos);

        int fiberCount = 0;
        int ampCount = 0;
        int transmitterCount = 0;
        int receiverCount = 0;

        while (!queue.isEmpty() && visited.size() < 512) {
            BlockPos current = queue.poll();
            BlockState bs = level.getBlockState(current);
            Block b = bs.getBlock();

            if (b instanceof PureOpticFiberBlock) {
                fiberCount++;
                for (Direction dir : Direction.values()) {
                    if (canConnectTo(level, current, dir, bs)) {
                        BlockPos neighbor = current.relative(dir);
                        if (!visited.contains(neighbor)) {
                            visited.add(neighbor);
                            queue.add(neighbor);
                        }
                    }
                }
            } else if (b instanceof OpticalBoosterAmplifierBlock) {
                ampCount++;
            } else if (b instanceof OpticalTransmitterPortBlock) {
                transmitterCount++;
            } else if (b instanceof OpticalReceiverPortBlock) {
                receiverCount++;
            }
        }

        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            for (BlockPos p : visited) {
                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 2, 0.15, 0.15, 0.15, 0.01);
            }
            level.playSound(null, startPos, net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME, net.minecraft.sounds.SoundSource.BLOCKS, 1.2f, 1.6f);
        }

        double loss = Math.min(100.0, Math.max(0.0, (fiberCount / 16.0) * 1.0 - (ampCount * 100.0)));
        int throughputPct = (int) Math.max(0, 100 - loss);

        player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                "§d✦ [Optic Network] §f" + fiberCount + " §7Fibers | §b" + ampCount + " §7Amplifiers | §e" + receiverCount + " §7Receivers | §aThroughput: " + throughputPct + "%"
        ), true);
    }

    @Override
    protected BlockState updateShape(BlockState state, net.minecraft.world.level.LevelReader level, net.minecraft.world.level.ScheduledTickAccess tickAccess, BlockPos currentPos, Direction direction, BlockPos neighborPos, BlockState neighborState, net.minecraft.util.RandomSource random) {
        BooleanProperty prop = PROPERTY_BY_DIRECTION.get(direction);
        if (prop != null) {
            return state.setValue(prop, canConnectTo(level, currentPos, direction, state));
        }
        return super.updateShape(state, level, tickAccess, currentPos, direction, neighborPos, neighborState, random);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = CORE;
        if (state.getValue(NORTH)) shape = Shapes.or(shape, ARM_NORTH);
        if (state.getValue(SOUTH)) shape = Shapes.or(shape, ARM_SOUTH);
        if (state.getValue(WEST))  shape = Shapes.or(shape, ARM_WEST);
        if (state.getValue(EAST))  shape = Shapes.or(shape, ARM_EAST);
        if (state.getValue(UP))    shape = Shapes.or(shape, ARM_UP);
        if (state.getValue(DOWN))  shape = Shapes.or(shape, ARM_DOWN);
        return shape;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }
}
