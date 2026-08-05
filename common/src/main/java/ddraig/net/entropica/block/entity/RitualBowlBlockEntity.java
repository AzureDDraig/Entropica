package ddraig.net.entropica.block.entity;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.api.ItemEssenceMap;
import ddraig.net.entropica.item.EssenceItem;
import ddraig.net.entropica.registry.ModBlockEntities;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RitualBowlBlockEntity extends BlockEntity {

    public final boolean isBasalt;

    public final SimpleContainer inventory = new SimpleContainer(5) {
        @Override
        public void setChanged() {
            super.setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
                RitualBowlBlockEntity.this.setChanged();
            }
        }
    };

    public int burningProgress = 0;
    public final int maxBurningTime = 100;

    public RitualBowlBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.RITUAL_BOWL_BE.get(), pos, blockState);
        if (blockState.getBlock() instanceof ddraig.net.entropica.block.RitualBowlBlock bowl) {
            this.isBasalt = bowl.isBasalt;
        } else {
            this.isBasalt = false;
        }
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        // Ritual bowls hold items and runes safely without passively burning them.
        // Essence extraction is handled by the Crucible.
    }

    private boolean canFitOutputs(List<ItemStack> outputs) {
        ItemStack[] sim = new ItemStack[4];
        for(int i = 0; i < 4; i++) sim[i] = inventory.getItem(i + 1).copy();

        for(ItemStack out : outputs) {
            int remaining = out.getCount();
            for(int i = 0; i < 4; i++) {
                if (sim[i].isEmpty()) {
                    sim[i] = out.copy();
                    remaining = 0;
                    break;
                } else if (ItemStack.isSameItemSameComponents(sim[i], out)) {
                    int space = sim[i].getMaxStackSize() - sim[i].getCount();
                    int add = Math.min(space, remaining);
                    sim[i].grow(add);
                    remaining -= add;
                    if (remaining <= 0) break;
                }
            }
            if (remaining > 0) return false;
        }
        return true;
    }

    private void mergeOutputs(List<ItemStack> outputs) {
        for(ItemStack out : outputs) {
            int remaining = out.getCount();
            for(int i = 1; i <= 4; i++) {
                ItemStack current = inventory.getItem(i);
                if (current.isEmpty()) {
                    inventory.setItem(i, out.copyWithCount(remaining));
                    break;
                } else if (ItemStack.isSameItemSameComponents(current, out)) {
                    int space = current.getMaxStackSize() - current.getCount();
                    int add = Math.min(space, remaining);
                    current.grow(add);
                    inventory.setItem(i, current);
                    remaining -= add;
                    if (remaining <= 0) break;
                }
            }
        }
    }

    public boolean interactWithPlayer(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);

        if (player.isShiftKeyDown() || held.isEmpty()) {
            for (int i = 1; i <= 4; i++) {
                ItemStack out = inventory.getItem(i);
                if (!out.isEmpty()) {
                    int extractCount = player.isShiftKeyDown() ? out.getCount() : 1;
                    ItemStack extracted = out.copyWithCount(extractCount);
                    out.shrink(extractCount);
                    inventory.setItem(i, out.isEmpty() ? ItemStack.EMPTY : out);
                    if (!player.getInventory().add(extracted)) player.drop(extracted, false);
                    if (level != null) level.playSound(null, worldPosition, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
                    return true;
                }
            }

            ItemStack in = inventory.getItem(0);
            if (!in.isEmpty()) {
                int extractCount = player.isShiftKeyDown() ? in.getCount() : 1;
                ItemStack extracted = in.copyWithCount(extractCount);
                in.shrink(extractCount);
                inventory.setItem(0, in.isEmpty() ? ItemStack.EMPTY : in);
                if (!player.getInventory().add(extracted)) player.drop(extracted, false);
                if (level != null) level.playSound(null, worldPosition, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
                return true;
            }
            return false;

        } else {
            if (held.getItem() instanceof EssenceItem) return false;

            ItemStack in = inventory.getItem(0);
            if (in.isEmpty() || ItemStack.isSameItemSameComponents(held, in)) {
                int space = in.isEmpty() ? held.getMaxStackSize() : in.getMaxStackSize() - in.getCount();
                if (space > 0) {
                    int insertCount = player.isShiftKeyDown() ? Math.min(space, held.getCount()) : 1;
                    if (in.isEmpty()) {
                        inventory.setItem(0, held.copyWithCount(insertCount));
                    } else {
                        in.grow(insertCount);
                        inventory.setItem(0, in);
                    }
                    held.shrink(insertCount);
                    if (level != null) level.playSound(null, worldPosition, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0F, 1.0F);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack stack = this.inventory.getItem(i);
            if (!stack.isEmpty()) output.store("Slot" + i, ItemStack.OPTIONAL_CODEC, stack);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.inventory.clearContent();
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            int slot = i;
            input.read("Slot" + i, ItemStack.OPTIONAL_CODEC).ifPresent(stack -> this.inventory.setItem(slot, stack));
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}