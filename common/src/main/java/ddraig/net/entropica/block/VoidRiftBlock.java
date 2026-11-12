package ddraig.net.entropica.block;

import ddraig.net.entropica.block.entity.VoidRiftBlockEntity;
import ddraig.net.entropica.item.VoidResonantTuningForkItem;
import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class VoidRiftBlock extends Block implements EntityBlock {

    public static final BooleanProperty IS_INPUT = BooleanProperty.create("is_input");

    public VoidRiftBlock(Properties properties) {
        // -1.0f makes it indestructible to normal mining and explosions!
        super(properties.noCollision().noOcclusion().destroyTime(-1.0f));
        this.registerDefaultState(this.stateDefinition.any().setValue(IS_INPUT, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(IS_INPUT);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Block.box(6.0, 6.0, 6.0, 10.0, 10.0, 10.0);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (level.isClientSide()) return;

        if (player.isCreative()) {
            level.destroyBlock(pos, false);
            return;
        }

        ItemStack handStack = player.getMainHandItem();
        if (handStack.getItem() instanceof VoidResonantTuningForkItem) {
            CustomData data = handStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            CompoundTag tag = data.copyTag();
            int currentVis = tag.getInt("StoredMateria2").orElse(0);

            if (currentVis >= VoidResonantTuningForkItem.BREAK_COST) {
                tag.putInt("StoredMateria2", currentVis - VoidResonantTuningForkItem.BREAK_COST);
                CustomData.set(DataComponents.CUSTOM_DATA, handStack, tag);

                // Safely destroy the block, dropping the Rift item itself
                level.destroyBlock(pos, true);
                level.playSound(null, pos, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1.0f, 0.5f);
            } else {
                player.displayClientMessage(Component.literal("Not enough Materia to collapse Rift! Requires " + VoidResonantTuningForkItem.BREAK_COST).withStyle(ChatFormatting.RED), true);
            }
        } else {
            player.displayClientMessage(Component.translatable("msg.entropica.void_rifts_can_only_be_collapsed").withStyle(ChatFormatting.RED), true);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VoidRiftBlockEntity(pos, state);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type == ModBlockEntities.VOID_RIFT_BE.get()) {
            return (lvl, pos, st, be) -> ((VoidRiftBlockEntity) be).tick(lvl, pos, st);
        }
        return null;
    }
}