package ddraig.net.entropica.block;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.block.entity.AstralAltarCoreBlockEntity;
import ddraig.net.entropica.item.CompletedStarChartItem;
import ddraig.net.entropica.registry.ModBlockEntities;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class AstralAltarCoreBlock extends BaseEntityBlock {

    public static final MapCodec<AstralAltarCoreBlock> CODEC = simpleCodec(AstralAltarCoreBlock::new);

    public static final BooleanProperty FORMED = BooleanProperty.create("formed");
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public AstralAltarCoreBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FORMED, false).setValue(ACTIVE, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FORMED, ACTIVE);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AstralAltarCoreBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, ModBlockEntities.ASTRAL_ALTAR_CORE_BE.get(), AstralAltarCoreBlockEntity::tick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof AstralAltarCoreBlockEntity altar)) {
            return super.useWithoutItem(state, level, pos, player, hitResult);
        }

        ItemStack heldStack = player.getItemInHand(InteractionHand.MAIN_HAND);

        // Wand interaction to force check / trigger ritual
        if (heldStack.is(ModItems.ASTRAL_LINKING_WAND.get())) {
            if (!level.isClientSide()) {
                altar.triggerWandInteraction(player);
            }
            return InteractionResult.SUCCESS;
        }

        // Insert / swap Completed Star Chart
        if (heldStack.getItem() instanceof CompletedStarChartItem) {
            if (!level.isClientSide()) {
                ItemStack oldChart = altar.getStarChart();
                altar.setStarChart(heldStack.split(1));
                if (!oldChart.isEmpty()) {
                    if (!player.getInventory().add(oldChart)) {
                        player.drop(oldChart, false);
                    }
                }
                level.playSound(null, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1.0f, 1.2f);
                player.displayClientMessage(Component.literal("§d[Astral Altar] §7Inserted Star Chart: §f" + altar.getStarChart().getHoverName().getString()), true);
            }
            return InteractionResult.SUCCESS;
        }

        // Insert / swap held Crystal or reagent item on altar core
        if (altar.getHeldItem().isEmpty() && !heldStack.isEmpty()) {
            if (!level.isClientSide()) {
                altar.setHeldItem(heldStack.split(1));
                level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_PLACE, SoundSource.BLOCKS, 1.0f, 1.2f);
            }
            return InteractionResult.SUCCESS;
        }

        // Shift-click with empty hand -> Extract held item or star chart
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                if (!altar.getHeldItem().isEmpty()) {
                    ItemStack extracted = altar.getHeldItem().copy();
                    altar.setHeldItem(ItemStack.EMPTY);
                    if (!player.getInventory().add(extracted)) {
                        player.drop(extracted, false);
                    }
                    level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.8f, 1.2f);
                    return InteractionResult.SUCCESS;
                } else if (!altar.getStarChart().isEmpty()) {
                    ItemStack extracted = altar.getStarChart().copy();
                    altar.setStarChart(ItemStack.EMPTY);
                    if (!player.getInventory().add(extracted)) {
                        player.drop(extracted, false);
                    }
                    level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.8f, 1.2f);
                    player.displayClientMessage(Component.literal("§7[Astral Altar] Extracted Star Chart."), true);
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.SUCCESS;
        } else {
            if (!level.isClientSide()) {
                altar.displayAltarStatus(player);
            }
            return InteractionResult.SUCCESS;
        }
    }
}
