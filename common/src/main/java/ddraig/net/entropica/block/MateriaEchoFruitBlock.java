package ddraig.net.entropica.block;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.registry.ModBlocks;

import net.minecraft.core.BlockPos;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MateriaEchoFruitBlock extends Block implements BonemealableBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_2;
    protected static final VoxelShape SHAPE = Block.box(4.0, 6.0, 4.0, 12.0, 16.0, 12.0);

    public MateriaEchoFruitBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState aboveState = level.getBlockState(pos.above());
        return aboveState.is(ModBlocks.MATERIA_ECHO_LEAVES.get());
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return state.getValue(AGE) < 2;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int age = state.getValue(AGE);
        if (age < 2 && random.nextInt(5) == 0) {
            level.setBlock(pos, state.setValue(AGE, age + 1), 2);
        }
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        int age = state.getValue(AGE);
        if (age == 2) {
            if (!level.isClientSide()) {
                EssenceType aspect = getAspectForBiome(level, pos);
                ItemStack dropStack = ddraig.net.entropica.item.EchoFruitItem.createFruitForAspect(aspect);
                popResource(level, pos, dropStack);
                level.setBlock(pos, state.setValue(AGE, 0), 2);
                level.playSound(null, pos, net.minecraft.sounds.SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
            }
            return InteractionResult.SUCCESS;
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    public static EssenceType getAspectForBiome(Level level, BlockPos pos) {
        var holder = level.getBiome(pos);
        var key = holder.unwrapKey();
        if (key.isPresent()) {
            String path = key.get().location().getPath();
            if (path.contains("snow") || path.contains("ice") || path.contains("frozen") || path.contains("taiga")) {
                return EssenceType.FROZEN;
            } else if (path.contains("desert") || path.contains("badlands") || path.contains("nether") || path.contains("basalt")) {
                return EssenceType.NETHER;
            } else if (path.contains("jungle") || path.contains("swamp") || path.contains("dark_forest") || path.contains("lush")) {
                return EssenceType.NATURE;
            } else if (path.contains("end") || path.contains("deep_dark")) {
                return EssenceType.VOID;
            } else if (path.contains("savanna") || path.contains("windswept") || path.contains("peaks")) {
                return EssenceType.LIGHTNING;
            } else if (path.contains("ocean") || path.contains("river") || path.contains("beach")) {
                return EssenceType.WATER;
            } else if (path.contains("soul") || path.contains("pale")) {
                return EssenceType.UNDEAD;
            } else if (path.contains("meadow") || path.contains("cherry") || path.contains("flower")) {
                return EssenceType.RADIANT;
            }
        }
        return EssenceType.REGULAR;
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return state.getValue(AGE) < 2;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        level.setBlock(pos, state.setValue(AGE, state.getValue(AGE) + 1), 2);
    }
}
