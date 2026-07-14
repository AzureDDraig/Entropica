package ddraig.net.entropica.block;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.block.entity.PneumaticCalixBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class PneumaticCalixBlock extends Block implements EntityBlock {

    public PneumaticCalixBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PneumaticCalixBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (level.getBlockEntity(pos) instanceof PneumaticCalixBlockEntity cellBlockEntity) {
            CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            CompoundTag tag = customData.copyTag();

            int storedMana = tag.getInt("StoredMana").orElse(0);
            String manaTypeStr = tag.getString("ManaType").orElse("");

            cellBlockEntity.setMana(storedMana);
            if (!manaTypeStr.isEmpty()) {
                try {
                    cellBlockEntity.setManaType(EssenceType.valueOf(manaTypeStr));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        super.setPlacedBy(level, pos, state, placer, stack);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && !player.isCreative()) {
            if (level.getBlockEntity(pos) instanceof PneumaticCalixBlockEntity cellBlockEntity) {
                ItemStack dropStack = new ItemStack(this);

                CompoundTag tag = new CompoundTag();
                tag.putInt("StoredMana", cellBlockEntity.getMana());
                if (cellBlockEntity.getManaType() != null) {
                    tag.putString("ManaType", cellBlockEntity.getManaType().name());
                }

                dropStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

                ItemEntity itemEntity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, dropStack);
                itemEntity.setDefaultPickUpDelay();
                level.addFreshEntity(itemEntity);
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        ItemStack stack = super.getCloneItemStack(level, pos, state, includeData);

        if (level.getBlockEntity(pos) instanceof PneumaticCalixBlockEntity cellBlockEntity) {
            CompoundTag tag = new CompoundTag();
            tag.putInt("StoredMana", cellBlockEntity.getMana());

            if (cellBlockEntity.getManaType() != null) {
                tag.putString("ManaType", cellBlockEntity.getManaType().name());
            }

            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }

        return stack;
    }
}
