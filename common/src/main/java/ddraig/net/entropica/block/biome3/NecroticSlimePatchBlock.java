package ddraig.net.entropica.block.biome3;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.item.biome3.CryoInsulatedArmorItem;
import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Necrotic Slime Patch — thin glistening crust of Blight Materia ooze coating Frost-Bite Floes ice.
 * Hinders uninsulated living entities and can be scooped into an empty bucket.
 */
public class NecroticSlimePatchBlock extends Block {
    public static final MapCodec<NecroticSlimePatchBlock> CODEC = simpleCodec(NecroticSlimePatchBlock::new);
    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);

    public NecroticSlimePatchBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);
        if (belowState.isAir()) {
            return false;
        }
        return belowState.isFaceSturdy(level, belowPos, Direction.UP)
                || belowState.is(BlockTags.ICE)
                || belowState.is(Blocks.ICE)
                || belowState.is(Blocks.PACKED_ICE)
                || belowState.is(Blocks.BLUE_ICE)
                || (ModBlocks.ROTTEN_ICE.isBound() && belowState.is(ModBlocks.ROTTEN_ICE.get()))
                || (ModBlocks.BLIGHTED_PACKED_ICE.isBound() && belowState.is(ModBlocks.BLIGHTED_PACKED_ICE.get()))
                || (ModBlocks.GLIMMER_ICE.isBound() && belowState.is(ModBlocks.GLIMMER_ICE.get()))
                || (ModBlocks.RUNIC_BLUE_ICE.isBound() && belowState.is(ModBlocks.RUNIC_BLUE_ICE.get()));
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess tickAccess,
                                     BlockPos pos, Direction direction, BlockPos neighborPos,
                                     BlockState neighborState, RandomSource random) {
        if (direction == Direction.DOWN && !this.canSurvive(state, level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, tickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                InsideBlockEffectApplier effectApplier, boolean isInside) {
        super.entityInside(state, level, pos, entity, effectApplier, isInside);
        if (entity instanceof LivingEntity living && !CryoInsulatedArmorItem.hasAnyCryoInsulation(living)) {
            entity.makeStuckInBlock(state, new Vec3(0.75D, 0.95D, 0.75D));
            if (!level.isClientSide()) {
                living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 0, false, true, true));
            }
        }
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(Items.BUCKET) && ModItems.NECROTIC_SLIME_BUCKET.isBound()) {
            level.playSound(player, pos, SoundEvents.SLIME_SQUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (!level.isClientSide()) {
                level.removeBlock(pos, false);
                level.gameEvent(player, GameEvent.BLOCK_DESTROY, pos);
                ItemStack filled = ItemUtils.createFilledResult(
                        stack,
                        player,
                        new ItemStack(ModItems.NECROTIC_SLIME_BUCKET.get())
                );
                player.setItemInHand(hand, filled);
            }
            return InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }
}
