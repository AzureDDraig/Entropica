package ddraig.net.entropica.block.biome1;

import com.mojang.serialization.MapCodec;
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
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Glacial Ice Maker — Frost-Core powered cryo-condenser block.
 * Right-clicking with a Water Bucket immediately yields Packed Ice (returning an empty Bucket),
 * and adjacent water source blocks freeze into Packed Ice automatically.
 */
public class GlacialIceMakerBlock extends Block {
    public static final MapCodec<GlacialIceMakerBlock> CODEC = simpleCodec(GlacialIceMakerBlock::new);

    public GlacialIceMakerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(Items.WATER_BUCKET)) {
            if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
                ItemStack emptyBucket = ItemUtils.createFilledResult(stack, player, new ItemStack(Items.BUCKET));
                player.setItemInHand(hand, emptyBucket);

                ItemStack packedIce = new ItemStack(Items.PACKED_ICE);
                if (!player.getInventory().add(packedIce)) {
                    Block.popResource(level, pos.relative(hitResult.getDirection()), packedIce);
                }

                serverLevel.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 0.9F, 1.3F);
                serverLevel.playSound(null, pos, SoundEvents.GLASS_PLACE, SoundSource.BLOCKS, 0.8F, 1.4F);
                serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                        pos.getX() + 0.5D, pos.getY() + 1.05D, pos.getZ() + 0.5D,
                        14, 0.35D, 0.2D, 0.35D, 0.03D);
            }
            return InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide()) {
            freezeAdjacentWater(level, pos);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                   @Nullable Orientation orientation, boolean isMoving) {
        super.neighborChanged(state, level, pos, neighborBlock, orientation, isMoving);
        if (!level.isClientSide()) {
            freezeAdjacentWater(level, pos);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        freezeAdjacentWater(level, pos);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        freezeAdjacentWater(level, pos);
    }

    private void freezeAdjacentWater(Level level, BlockPos pos) {
        boolean frozeAny = false;
        for (Direction dir : Direction.values()) {
            BlockPos adjPos = pos.relative(dir);
            BlockState adjState = level.getBlockState(adjPos);
            if (adjState.is(Blocks.WATER) && adjState.getFluidState().isSource()) {
                level.setBlock(adjPos, Blocks.PACKED_ICE.defaultBlockState(), 3);
                frozeAny = true;
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                            adjPos.getX() + 0.5D, adjPos.getY() + 0.5D, adjPos.getZ() + 0.5D,
                            8, 0.3D, 0.3D, 0.3D, 0.02D);
                }
            }
        }
        if (frozeAny) {
            level.playSound(null, pos, SoundEvents.GLASS_PLACE, SoundSource.BLOCKS, 0.6F, 1.5F);
        }
    }
}
