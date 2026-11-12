package ddraig.net.entropica.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ddraig.net.entropica.block.entity.LampPostBlockEntity;
import ddraig.net.entropica.registry.ModBlockEntities;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class LampPostBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {

    public enum MaterialVariant implements StringRepresentable {
        IRON("iron"),
        BRASS("brass"),
        STEEL("steel"),
        GLASS("glass");

        private final String name;

        MaterialVariant(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public static final MapCodec<LampPostBlock> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    StringRepresentable.fromEnum(MaterialVariant::values).fieldOf("material").forGetter(LampPostBlock::getMaterialVariant),
                    propertiesCodec()
            ).apply(instance, LampPostBlock::new)
    );

    public static final EnumProperty<LampPostPart> PART = EnumProperty.create("part", LampPostPart.class);
    public static final EnumProperty<LampPostArm> NORTH_ARM = EnumProperty.create("north_arm", LampPostArm.class);
    public static final EnumProperty<LampPostArm> EAST_ARM = EnumProperty.create("east_arm", LampPostArm.class);
    public static final EnumProperty<LampPostArm> SOUTH_ARM = EnumProperty.create("south_arm", LampPostArm.class);
    public static final EnumProperty<LampPostArm> WEST_ARM = EnumProperty.create("west_arm", LampPostArm.class);
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Voxel Shapes for Parts
    // Bottom: 16x16x1 baseplate + tiered plinth 12x12 -> 10x10 -> 9x9 -> 8x8 shaft
    private static final VoxelShape BASEPLATE = Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);
    private static final VoxelShape SHAPE_BOTTOM = Shapes.or(
            BASEPLATE,
            Block.box(2.0, 1.0, 2.0, 14.0, 3.0, 14.0),
            Block.box(3.0, 3.0, 3.0, 13.0, 5.0, 13.0),
            Block.box(3.5, 5.0, 3.5, 12.5, 10.0, 12.5),
            Block.box(4.0, 10.0, 4.0, 12.0, 16.0, 12.0)
    );
    private static final VoxelShape SHAPE_SINGLE = Shapes.or(
            BASEPLATE,
            Block.box(2.0, 1.0, 2.0, 14.0, 3.0, 14.0),
            Block.box(3.5, 3.0, 3.5, 12.5, 5.0, 12.5),
            Block.box(4.5, 5.0, 4.5, 11.5, 8.0, 11.5),
            Block.box(5.5, 8.0, 5.5, 10.5, 16.0, 10.5)
    );
    private static final VoxelShape SHAPE_LOWER_MIDDLE = Block.box(3.5, 0.0, 3.5, 12.5, 16.0, 12.5);
    private static final VoxelShape SHAPE_MIDDLE       = Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0);
    private static final VoxelShape SHAPE_UPPER_MIDDLE = Block.box(4.5, 0.0, 4.5, 11.5, 16.0, 11.5);
    private static final VoxelShape SHAPE_TOP          = Block.box(5.0, 0.0, 5.0, 11.0, 16.0, 11.0);

    // Arm Extension Shapes (Y=8 to 14)
    private static final VoxelShape ARM_SHAPE_NORTH = Block.box(6.0, 8.0, 0.0, 10.0, 14.0, 6.0);
    private static final VoxelShape ARM_SHAPE_EAST  = Block.box(10.0, 8.0, 6.0, 16.0, 14.0, 10.0);
    private static final VoxelShape ARM_SHAPE_SOUTH = Block.box(6.0, 8.0, 10.0, 10.0, 14.0, 16.0);
    private static final VoxelShape ARM_SHAPE_WEST  = Block.box(0.0, 8.0, 6.0, 6.0, 14.0, 10.0);

    private final MaterialVariant materialVariant;

    public LampPostBlock(MaterialVariant materialVariant, Properties properties) {
        super(properties.lightLevel(state -> {
            if (!state.getValue(LIT)) return 0;
            // If any arm has a bulb or if it's glass, full bright 15!
            if (state.getValue(NORTH_ARM) == LampPostArm.BULB ||
                state.getValue(EAST_ARM) == LampPostArm.BULB ||
                state.getValue(SOUTH_ARM) == LampPostArm.BULB ||
                state.getValue(WEST_ARM) == LampPostArm.BULB ||
                materialVariant == MaterialVariant.GLASS) {
                return 15;
            }
            return 8; // Soft starlight conduit glow for metal posts without bulbs
        }));
        this.materialVariant = materialVariant;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(PART, LampPostPart.SINGLE)
                .setValue(NORTH_ARM, LampPostArm.NONE)
                .setValue(EAST_ARM, LampPostArm.NONE)
                .setValue(SOUTH_ARM, LampPostArm.NONE)
                .setValue(WEST_ARM, LampPostArm.NONE)
                .setValue(LIT, false)
                .setValue(WATERLOGGED, false));
    }

    public MaterialVariant getMaterialVariant() {
        return materialVariant;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PART, NORTH_ARM, EAST_ARM, SOUTH_ARM, WEST_ARM, LIT, WATERLOGGED);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape baseShape = switch (state.getValue(PART)) {
            case SINGLE -> SHAPE_SINGLE;
            case BOTTOM -> SHAPE_BOTTOM;
            case LOWER_MIDDLE -> SHAPE_LOWER_MIDDLE;
            case MIDDLE -> SHAPE_MIDDLE;
            case UPPER_MIDDLE -> SHAPE_UPPER_MIDDLE;
            case TOP -> SHAPE_TOP;
        };

        if (state.getValue(NORTH_ARM).hasArm()) baseShape = Shapes.or(baseShape, ARM_SHAPE_NORTH);
        if (state.getValue(EAST_ARM).hasArm())  baseShape = Shapes.or(baseShape, ARM_SHAPE_EAST);
        if (state.getValue(SOUTH_ARM).hasArm()) baseShape = Shapes.or(baseShape, ARM_SHAPE_SOUTH);
        if (state.getValue(WEST_ARM).hasArm())  baseShape = Shapes.or(baseShape, ARM_SHAPE_WEST);

        return baseShape;
    }

    public static int getColumnHeight(BlockGetter level, BlockPos pos) {
        int count = 1;
        BlockPos check = pos.below();
        while (level.getBlockState(check).getBlock() instanceof LampPostBlock) {
            count++;
            check = check.below();
        }
        check = pos.above();
        while (level.getBlockState(check).getBlock() instanceof LampPostBlock) {
            count++;
            check = check.above();
        }
        return count;
    }

    public static LampPostPart calculatePart(BlockGetter level, BlockPos pos) {
        int below = 0;
        BlockPos check = pos.below();
        while (level.getBlockState(check).getBlock() instanceof LampPostBlock) {
            below++;
            check = check.below();
        }
        int above = 0;
        check = pos.above();
        while (level.getBlockState(check).getBlock() instanceof LampPostBlock) {
            above++;
            check = check.above();
        }

        int total = below + above + 1;
        if (total == 1) return LampPostPart.SINGLE;
        if (below == 0) return LampPostPart.BOTTOM;
        if (above == 0) return LampPostPart.TOP;

        // Intermediate shaft blocks based on height and position in column
        if (total == 3) {
            return LampPostPart.MIDDLE;
        } else if (total == 4) {
            return below == 1 ? LampPostPart.LOWER_MIDDLE : LampPostPart.UPPER_MIDDLE;
        } else if (total == 5) {
            if (below == 1) return LampPostPart.LOWER_MIDDLE;
            if (below == 2) return LampPostPart.MIDDLE;
            return LampPostPart.UPPER_MIDDLE;
        } else { // total == 6
            if (below == 1) return LampPostPart.LOWER_MIDDLE;
            if (below == 2) return LampPostPart.MIDDLE;
            if (below == 3) return LampPostPart.MIDDLE;
            return LampPostPart.UPPER_MIDDLE;
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        // Enforce max 6-high column constraint
        if (getColumnHeight(level, pos) > 6) {
            if (context.getPlayer() != null) {
                context.getPlayer().displayClientMessage(Component.literal("§cLamp posts cannot exceed 6 blocks in height."), true);
            }
            return null;
        }

        FluidState fluidState = level.getFluidState(pos);
        LampPostPart part = calculatePart(level, pos);

        BlockState state = this.defaultBlockState()
                .setValue(PART, part)
                .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);

        // Auto-connect to adjacent optic fibers on placement
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos neighborPos = pos.relative(dir);
            if (level.getBlockState(neighborPos).getBlock() instanceof PureOpticFiberBlock) {
                state = state.setValue(getArmProperty(dir), LampPostArm.CABLE);
            }
        }

        return state;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable net.minecraft.world.entity.LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        updateColumn(level, pos);
    }

    @Override
    public void destroy(net.minecraft.world.level.LevelAccessor level, BlockPos pos, BlockState state) {
        super.destroy(level, pos, state);
        if (level instanceof Level lvl) {
            updateColumn(lvl, pos.below());
            updateColumn(lvl, pos.above());
        }
    }

    public static void updateColumn(Level level, BlockPos pos) {
        if (level.isClientSide()) return;
        if (!(level.getBlockState(pos).getBlock() instanceof LampPostBlock)) return;

        BlockPos bottom = pos;
        while (level.getBlockState(bottom.below()).getBlock() instanceof LampPostBlock) {
            bottom = bottom.below();
        }

        BlockPos check = bottom;
        while (level.getBlockState(check).getBlock() instanceof LampPostBlock) {
            BlockState current = level.getBlockState(check);
            LampPostPart expectedPart = calculatePart(level, check);
            if (current.getValue(PART) != expectedPart) {
                level.setBlock(check, current.setValue(PART, expectedPart), 3);
            }
            check = check.above();
        }
    }

    public static EnumProperty<LampPostArm> getArmProperty(Direction dir) {
        return switch (dir) {
            case NORTH -> NORTH_ARM;
            case EAST -> EAST_ARM;
            case SOUTH -> SOUTH_ARM;
            case WEST -> WEST_ARM;
            default -> NORTH_ARM;
        };
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, net.minecraft.world.level.ScheduledTickAccess tickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, net.minecraft.util.RandomSource random) {
        if (state.getValue(WATERLOGGED)) {
            tickAccess.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        if (direction == Direction.UP || direction == Direction.DOWN) {
            return state.setValue(PART, calculatePart(level, pos));
        }

        if (direction.getAxis().isHorizontal()) {
            EnumProperty<LampPostArm> armProp = getArmProperty(direction);
            LampPostArm currentArm = state.getValue(armProp);
            if (neighborState.getBlock() instanceof PureOpticFiberBlock) {
                if (currentArm == LampPostArm.NONE) {
                    state = state.setValue(armProp, LampPostArm.CABLE);
                }
            } else if (currentArm == LampPostArm.CABLE) {
                state = state.setValue(armProp, LampPostArm.NONE);
            }
        }

        return state;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        Direction hitDir = hitResult.getDirection();
        if (hitDir == Direction.UP || hitDir == Direction.DOWN) {
            // Pick facing from hit vector relative to center
            double dx = hitResult.getLocation().x - (pos.getX() + 0.5);
            double dz = hitResult.getLocation().z - (pos.getZ() + 0.5);
            if (Math.abs(dx) > Math.abs(dz)) {
                hitDir = dx > 0 ? Direction.EAST : Direction.WEST;
            } else {
                hitDir = dz > 0 ? Direction.SOUTH : Direction.NORTH;
            }
        }

        EnumProperty<LampPostArm> armProp = getArmProperty(hitDir);
        LampPostArm currentArm = state.getValue(armProp);

        // 1. Placing / attaching a Caged Optic Bulb
        if (stack.is(ModItems.CAGED_OPTIC_BULB_ITEM.get())) {
            if (currentArm != LampPostArm.BULB) {
                if (!level.isClientSide()) {
                    if (currentArm == LampPostArm.CABLE) {
                        Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5, new ItemStack(ModItems.PURE_OPTIC_FIBER_ITEM.get()));
                    }
                    level.setBlock(pos, state.setValue(armProp, LampPostArm.BULB), 3);
                    level.playSound(null, pos, SoundEvents.LANTERN_PLACE, SoundSource.BLOCKS, 1.0f, 1.2f);
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }

        // 2. Placing / installing an Optic Cable Arm using Pure Optic Fiber
        if (stack.is(ModItems.PURE_OPTIC_FIBER_ITEM.get())) {
            if (currentArm != LampPostArm.CABLE) {
                if (!level.isClientSide()) {
                    if (currentArm == LampPostArm.BULB) {
                        Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5, new ItemStack(ModItems.CAGED_OPTIC_BULB_ITEM.get()));
                    }
                    level.setBlock(pos, state.setValue(armProp, LampPostArm.CABLE), 3);
                    level.playSound(null, pos, SoundEvents.GLASS_PLACE, SoundSource.BLOCKS, 1.0f, 1.2f);
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }

        // 3. Click with empty hand to de-toggle/cycle attachments (bulb, cable, or arm bracket)
        if (stack.isEmpty()) {
            if (currentArm == LampPostArm.BULB) {
                if (!level.isClientSide()) {
                    level.setBlock(pos, state.setValue(armProp, LampPostArm.ARM), 3);
                    level.playSound(null, pos, SoundEvents.LANTERN_BREAK, SoundSource.BLOCKS, 1.0f, 1.0f);
                    if (!player.getAbilities().instabuild) {
                        Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5, new ItemStack(ModItems.CAGED_OPTIC_BULB_ITEM.get()));
                    }
                }
                return InteractionResult.SUCCESS;
            } else if (currentArm == LampPostArm.CABLE) {
                if (!level.isClientSide()) {
                    level.setBlock(pos, state.setValue(armProp, LampPostArm.NONE), 3);
                    level.playSound(null, pos, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1.0f, 1.0f);
                    if (!player.getAbilities().instabuild) {
                        Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5, new ItemStack(ModItems.PURE_OPTIC_FIBER_ITEM.get()));
                    }
                }
                return InteractionResult.SUCCESS;
            } else if (currentArm == LampPostArm.ARM) {
                if (!level.isClientSide()) {
                    level.setBlock(pos, state.setValue(armProp, LampPostArm.NONE), 3);
                    level.playSound(null, pos, SoundEvents.METAL_BREAK, SoundSource.BLOCKS, 0.8f, 1.1f);
                }
                return InteractionResult.SUCCESS;
            } else if (currentArm == LampPostArm.NONE) {
                if (!level.isClientSide()) {
                    level.setBlock(pos, state.setValue(armProp, LampPostArm.ARM), 3);
                    level.playSound(null, pos, SoundEvents.METAL_PLACE, SoundSource.BLOCKS, 0.9f, 1.2f);
                }
                return InteractionResult.SUCCESS;
            }
        }

        // 4. Wand or holding lamp post item toggles arm on/off
        if (stack.is(this.asItem()) || stack.getItem() instanceof ddraig.net.entropica.item.AstralLinkingWandItem) {
            if (currentArm == LampPostArm.NONE) {
                if (!level.isClientSide()) {
                    level.setBlock(pos, state.setValue(armProp, LampPostArm.ARM), 3);
                    level.playSound(null, pos, SoundEvents.METAL_PLACE, SoundSource.BLOCKS, 0.9f, 1.2f);
                }
                return InteractionResult.SUCCESS;
            } else if (currentArm == LampPostArm.ARM) {
                if (!level.isClientSide()) {
                    level.setBlock(pos, state.setValue(armProp, LampPostArm.NONE), 3);
                    level.playSound(null, pos, SoundEvents.METAL_BREAK, SoundSource.BLOCKS, 0.8f, 1.1f);
                }
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (!level.isClientSide()) {
            Vec3 eye = player.getEyePosition();
            Vec3 look = player.getViewVector(1.0f);
            Vec3 reach = eye.add(look.scale(5.0));
            BlockHitResult hit = level.clip(new ClipContext(eye, reach, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
            if (hit.getBlockPos().equals(pos)) {
                double dx = hit.getLocation().x - (pos.getX() + 0.5);
                double dz = hit.getLocation().z - (pos.getZ() + 0.5);
                if (Math.abs(dx) > 0.22 || Math.abs(dz) > 0.22) {
                    Direction armDir = Math.abs(dx) > Math.abs(dz) ? (dx > 0 ? Direction.EAST : Direction.WEST) : (dz > 0 ? Direction.SOUTH : Direction.NORTH);
                    EnumProperty<LampPostArm> armProp = getArmProperty(armDir);
                    LampPostArm arm = state.getValue(armProp);
                    if (arm != LampPostArm.NONE) {
                        if (arm == LampPostArm.BULB) {
                            level.setBlock(pos, state.setValue(armProp, LampPostArm.ARM), 3);
                            level.playSound(null, pos, SoundEvents.LANTERN_BREAK, SoundSource.BLOCKS, 1.0f, 1.0f);
                            if (!player.getAbilities().instabuild) {
                                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5, new ItemStack(ModItems.CAGED_OPTIC_BULB_ITEM.get()));
                            }
                        } else if (arm == LampPostArm.CABLE) {
                            level.setBlock(pos, state.setValue(armProp, LampPostArm.NONE), 3);
                            level.playSound(null, pos, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1.0f, 1.0f);
                            if (!player.getAbilities().instabuild) {
                                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5, new ItemStack(ModItems.PURE_OPTIC_FIBER_ITEM.get()));
                            }
                        } else if (arm == LampPostArm.ARM) {
                            level.setBlock(pos, state.setValue(armProp, LampPostArm.NONE), 3);
                            level.playSound(null, pos, SoundEvents.METAL_BREAK, SoundSource.BLOCKS, 0.8f, 1.1f);
                        }
                    }
                }
            }
        }
        super.attack(state, level, pos, player);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && !player.getAbilities().instabuild) {
            int bulbCount = 0;
            int fiberCount = 0;
            for (EnumProperty<LampPostArm> armProp : java.util.List.of(NORTH_ARM, EAST_ARM, SOUTH_ARM, WEST_ARM)) {
                if (state.getValue(armProp) == LampPostArm.BULB) bulbCount++;
                if (state.getValue(armProp) == LampPostArm.CABLE) fiberCount++;
            }

            if (bulbCount > 0) {
                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(ModItems.CAGED_OPTIC_BULB_ITEM.get(), bulbCount));
            }
            if (fiberCount > 0) {
                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(ModItems.PURE_OPTIC_FIBER_ITEM.get(), fiberCount));
            }
        }
        updateColumn(level, pos.below());
        updateColumn(level, pos.above());
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LampPostBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, ModBlockEntities.LAMP_POST_BE.get(), LampPostBlockEntity::tick);
    }
}
