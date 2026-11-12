package ddraig.net.entropica.block;

import ddraig.net.entropica.block.entity.RubberLogBlockEntity;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

public class RubberLogBlock extends RotatedPillarBlock implements EntityBlock {

    public static final BooleanProperty HAS_RESIN = BooleanProperty.create("has_resin");
    public static final EnumProperty<Direction> RESIN_FACING = BlockStateProperties.HORIZONTAL_FACING;

    public RubberLogBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(AXIS, Direction.Axis.Y)
                .setValue(HAS_RESIN, false)
                .setValue(RESIN_FACING, Direction.NORTH));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RubberLogBlockEntity(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HAS_RESIN, RESIN_FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) state = this.defaultBlockState();
        
        Direction horizontal = context.getHorizontalDirection().getOpposite();
        return state.setValue(HAS_RESIN, false).setValue(RESIN_FACING, horizontal);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.is(ModItems.RUBBER_TAP.get())) {
            boolean hasResin = state.getValue(HAS_RESIN);
            Direction resinFacing = state.getValue(RESIN_FACING);
            Direction clickedFace = hit.getDirection();

            if (hasResin && (clickedFace == resinFacing || clickedFace.getAxis().isHorizontal())) {
                if (!level.isClientSide()) {
                    int count = level.random.nextInt(3) + 1;
                    ItemStack rubberStack = new ItemStack(ModItems.RAW_RUBBER.get(), count);
                    double px = pos.getX() + 0.5 + clickedFace.getStepX() * 0.5;
                    double py = pos.getY() + 0.5;
                    double pz = pos.getZ() + 0.5 + clickedFace.getStepZ() * 0.5;
                    ItemEntity itemEntity = new ItemEntity(level, px, py, pz, rubberStack);
                    level.addFreshEntity(itemEntity);

                    level.setBlock(pos, state.setValue(HAS_RESIN, false), 3);

                    level.playSound(null, pos, SoundEvents.SLIME_BLOCK_BREAK, SoundSource.BLOCKS, 1.0f, 1.2f);
                    level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 0.8f, 1.0f);

                    if (player instanceof ServerPlayer serverPlayer) {
                        EquipmentSlot slot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                        stack.hurtAndBreak(1, serverPlayer, slot);
                    }
                } else {
                    double px = pos.getX() + 0.5 + clickedFace.getStepX() * 0.4;
                    double py = pos.getY() + 0.5;
                    double pz = pos.getZ() + 0.5 + clickedFace.getStepZ() * 0.4;
                    for (int i = 0; i < 5; i++) {
                        level.addParticle(ParticleTypes.WAX_OFF, px, py, pz, 0.0, -0.05, 0.0);
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return !state.getValue(HAS_RESIN);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(HAS_RESIN)) {
            if (random.nextInt(5) == 0) {
                Direction[] horizontalDirs = new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
                Direction randomFacing = horizontalDirs[random.nextInt(horizontalDirs.length)];
                level.setBlock(pos, state.setValue(HAS_RESIN, true).setValue(RESIN_FACING, randomFacing), 3);
                if (level.getBlockEntity(pos) instanceof RubberLogBlockEntity be) {
                    be.generateRandomSpotOffset();
                }
            }
        }
    }
}
