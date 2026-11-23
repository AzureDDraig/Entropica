package ddraig.net.entropica.block.biome4;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.registry.ModItems;
import ddraig.net.entropica.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Bismuth Key Pedestal — prismatic stone lock pedestal guarding the inner Monolith vault.
 * Seating a Bismuth Ingot unlocks the pedestal, emits full strength (15) redstone in all
 * directions, and opens adjacent Iron Doors; empty-hand interacting retrieves the ingot.
 */
public class BismuthKeyPedestalBlock extends Block {
    public static final MapCodec<BismuthKeyPedestalBlock> CODEC = simpleCodec(BismuthKeyPedestalBlock::new);
    public static final BooleanProperty UNLOCKED = BooleanProperty.create("unlocked");

    protected static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 14.0D, 14.0D);

    public BismuthKeyPedestalBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(UNLOCKED, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(UNLOCKED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!state.getValue(UNLOCKED)
                && ModItems.BISMUTH_INGOT.isBound()
                && stack.is(ModItems.BISMUTH_INGOT.get())) {
            if (!level.isClientSide()) {
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                level.setBlock(pos, state.setValue(UNLOCKED, true), 3);
                notifyNeighborsAndDoors(level, pos, true);
                level.playSound(null, pos, SoundEvents.VAULT_OPEN_SHUTTER, SoundSource.BLOCKS, 1.0F, 1.05F);
                level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F, 1.25F);
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.END_ROD,
                            pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D,
                            14, 0.25D, 0.25D, 0.25D, 0.04D);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (state.getValue(UNLOCKED)) {
            if (!level.isClientSide()) {
                if (ModItems.BISMUTH_INGOT.isBound()) {
                    popResource(level, pos.above(), new ItemStack(ModItems.BISMUTH_INGOT.get()));
                }
                level.setBlock(pos, state.setValue(UNLOCKED, false), 3);
                notifyNeighborsAndDoors(level, pos, false);
                level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.9F, 0.75F);
            }
            return InteractionResult.SUCCESS;
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean isMoving) {
        super.affectNeighborsAfterRemoval(state, level, pos, isMoving);
        if (!isMoving && state.getValue(UNLOCKED)) {
            if (ModItems.BISMUTH_INGOT.isBound()) {
                popResource(level, pos, new ItemStack(ModItems.BISMUTH_INGOT.get()));
            }
            notifyNeighborsAndDoors(level, pos, false);
        }
    }

    private void notifyNeighborsAndDoors(Level level, BlockPos pos, boolean open) {
        level.updateNeighborsAt(pos, this);
        for (Direction dir : Direction.values()) {
            level.updateNeighborsAt(pos.relative(dir), this);
        }
        level.updateNeighborsAt(pos.below(2), this);
        level.updateNeighborsAt(pos.above(2), this);

        for (BlockPos check : BlockPos.betweenClosed(pos.offset(-2, -1, -2), pos.offset(2, 2, 2))) {
            BlockState doorState = level.getBlockState(check);
            if (doorState.is(Blocks.IRON_DOOR) && doorState.getBlock() instanceof DoorBlock doorBlock) {
                doorBlock.setOpen(null, level, doorState, check.immutable(), open);
            }
        }
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(UNLOCKED) ? 15 : 0;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(UNLOCKED) ? 15 : 0;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (state.getValue(UNLOCKED) && random.nextInt(3) == 0) {
            double x = pos.getX() + 0.35D + random.nextDouble() * 0.3D;
            double y = pos.getY() + 0.92D + random.nextDouble() * 0.25D;
            double z = pos.getZ() + 0.35D + random.nextDouble() * 0.3D;
            if (ModParticles.SPECTRUM_SPARKLE.isBound() && random.nextBoolean()) {
                level.addParticle(ModParticles.SPECTRUM_SPARKLE.get(), x, y, z, 0.86F, 0.38F, 0.95F);
            } else {
                level.addParticle(ParticleTypes.END_ROD, x, y, z, 0.0D, 0.015D, 0.0D);
            }
        }
    }
}
