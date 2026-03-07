package ddraig.net.entropica.block;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.block.entity.CatalystReceptacleBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class CatalystReceptacleBlock extends BaseEntityBlock {
    public static final BooleanProperty HAS_CATALYST = BooleanProperty.create("has_catalyst");

    // Required codec to satisfy BaseEntityBlock's abstract method
    public static final MapCodec<CatalystReceptacleBlock> CODEC = simpleCodec(CatalystReceptacleBlock::new);

    public CatalystReceptacleBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(HAS_CATALYST, false));
    }

    @Override
    public MapCodec<CatalystReceptacleBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HAS_CATALYST);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CatalystReceptacleBlockEntity(pos, state);
    }

    // Handles Inserting the Catalyst
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof CatalystReceptacleBlockEntity be) {
            if (!state.getValue(HAS_CATALYST) && !stack.isEmpty()) {

                // Safely fetch the item's registry path name instead of relying on toString()
                String itemName = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().toLowerCase();

                if (itemName.contains("chimera") || itemName.contains("prismatic")) {
                    if (!level.isClientSide()) {
                        ItemStack catalyst = stack.copy();
                        catalyst.setCount(1);
                        be.setCatalyst(catalyst);
                        stack.shrink(1);
                        level.setBlock(pos, state.setValue(HAS_CATALYST, true), 3);
                    }
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }

    // Handles Extracting the Catalyst with an Empty Hand
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof CatalystReceptacleBlockEntity be) {
            if (state.getValue(HAS_CATALYST)) {
                if (!level.isClientSide()) {
                    ItemStack extracted = be.getCatalyst();
                    if (!extracted.isEmpty()) {
                        player.getInventory().placeItemBackInInventory(extracted);
                        be.setCatalyst(ItemStack.EMPTY);
                        level.setBlock(pos, state.setValue(HAS_CATALYST, false), 3);
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}