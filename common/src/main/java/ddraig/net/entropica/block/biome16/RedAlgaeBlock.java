package ddraig.net.entropica.block.biome16;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.block.EntropicaFloraGroundHelper;
import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Red Algae — mucilaginous crimson tidal algae mat carpeting shallow shoals, Sanguine Ice rims,
 * Red Clay-Sand beds, and calm bay water margins across 2.16 Blood-Ice Shallows.
 * Spreads across nearby shallows when fertilized with Bone Meal, doubles as a natural adhesive
 * and red pigment in crafting, and can be eaten for restorative relief.
 */
public class RedAlgaeBlock extends Block implements SimpleWaterloggedBlock, BonemealableBlock {
    public static final MapCodec<RedAlgaeBlock> CODEC = simpleCodec(RedAlgaeBlock::new);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);

    public RedAlgaeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState().setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos belowPos = pos.below();
        BlockState below = level.getBlockState(belowPos);

        if (below.isFaceSturdy(level, belowPos, Direction.UP)
                || Block.isFaceFull(below.getCollisionShape(level, belowPos), Direction.UP)) {
            return true;
        }
        if (EntropicaFloraGroundHelper.isAnyIce(below)
                || EntropicaFloraGroundHelper.isEntropicaMineralSandOrClay(below)) {
            return true;
        }
        if ((ModBlocks.SANGUINE_ICE.isBound() && below.is(ModBlocks.SANGUINE_ICE.get()))
                || (ModBlocks.RED_CLAY_SAND.isBound() && below.is(ModBlocks.RED_CLAY_SAND.get()))) {
            return true;
        }
        ResourceLocation belowKey = BuiltInRegistries.BLOCK.getKey(below.getBlock());
        if (belowKey != null && "entropica".equals(belowKey.getNamespace())) {
            String path = belowKey.getPath();
            if ("sanguine_ice".equals(path) || "red_clay_sand".equals(path)) {
                return true;
            }
        }
        try {
            if (below.is(BlockTags.ICE)) {
                return true;
            }
        } catch (IllegalStateException ignored) {
        }

        // Floats cleanly on calm bay water margins when placed in air above water
        return !state.getValue(WATERLOGGED)
                && level.getFluidState(belowPos).is(FluidTags.WATER)
                && level.getFluidState(pos).isEmpty();
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess tickAccess,
                                     BlockPos pos, Direction direction, BlockPos neighborPos,
                                     BlockState neighborState, RandomSource random) {
        if (state.getValue(WATERLOGGED)) {
            tickAccess.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        if (!state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, tickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        int desiredSpread = 1 + random.nextInt(3);
        List<BlockPos> candidates = new ArrayList<>();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }
                for (int dy = -1; dy <= 1; dy++) {
                    candidates.add(pos.offset(dx, dy, dz));
                }
            }
        }
        Collections.shuffle(candidates, new java.util.Random(random.nextLong()));

        int spreadCount = 0;
        for (BlockPos targetPos : candidates) {
            if (spreadCount >= desiredSpread) {
                break;
            }
            if (!level.hasChunkAt(targetPos)) {
                continue;
            }
            BlockState existing = level.getBlockState(targetPos);
            FluidState fluid = level.getFluidState(targetPos);
            boolean canOccupy = existing.isAir()
                    || (existing.is(Blocks.WATER) && fluid.getType() == Fluids.WATER && fluid.isSource());
            if (!canOccupy) {
                continue;
            }
            BlockState placed = this.defaultBlockState().setValue(WATERLOGGED, fluid.getType() == Fluids.WATER);
            if (placed.canSurvive(level, targetPos)) {
                level.setBlock(targetPos, placed, 3);
                spreadCount++;
            }
        }

        if (spreadCount == 0) {
            popResource(level, pos, new ItemStack(this.asItem()));
        }
    }
}
