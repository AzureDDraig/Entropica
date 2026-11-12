package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.api.pressure.IPressureHandler;
import ddraig.net.entropica.api.pressure.PressureNetworkHelper;
import ddraig.net.entropica.block.MateriaPumpBlock;
import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

public class MateriaPumpBlockEntity extends BlockEntity {

    protected ItemStack fuelStack = ItemStack.EMPTY;
    protected int burnTime = 0;
    protected int maxBurnTime = 0;

    public MateriaPumpBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MATERIA_PUMP_BE.get(), pos, state);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide()) return;

        boolean wasActive = burnTime > 0;
        boolean isPowered = state.getValue(MateriaPumpBlock.POWERED);

        if (burnTime > 0) {
            burnTime--;
        }

        // Consume fuel if powered and out of burn time
        if (burnTime == 0 && isPowered && !fuelStack.isEmpty()) {
            int duration = getBurnDuration(fuelStack);
            if (duration > 0) {
                this.burnTime = duration;
                this.maxBurnTime = duration;
                fuelStack.shrink(1);
                this.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }
        }

        boolean isActive = burnTime > 0 || isPowered;

        if (isActive) {
            Direction facing = state.getValue(MateriaPumpBlock.FACING);
            BlockPos targetPos = pos.relative(facing);
            BlockEntity targetBE = level.getBlockEntity(targetPos);

            if (targetBE instanceof IPressureHandler target) {
                // Diminishing returns based on Materia tier inside the target
                int tier = PressureNetworkHelper.getMateriaTier(target.getMateriaInTank());
                float baseStrength = (burnTime > 0) ? 1.0f : 0.5f;
                float strength = baseStrength / Math.max(1, tier - 1);

                target.applyActiveBoost(facing, strength);
            }
        }

        if (wasActive != (burnTime > 0)) {
            this.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    public boolean interactWithPlayer(Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);

        // Extract fuel from slot
        if (heldItem.isEmpty()) {
            if (!this.fuelStack.isEmpty()) {
                player.setItemInHand(hand, this.fuelStack.copy());
                this.fuelStack = ItemStack.EMPTY;
                this.setChanged();
                if (this.level != null && !this.level.isClientSide()) {
                    this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
                }
                return true;
            }
        }
        // Insert fuel
        else {
            if (isFuel(heldItem)) {
                if (this.fuelStack.isEmpty()) {
                    this.fuelStack = heldItem.split(1);
                    this.setChanged();
                    if (this.level != null && !this.level.isClientSide()) {
                        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
                    }
                    return true;
                } else if (ItemStack.isSameItemSameComponents(this.fuelStack, heldItem)) {
                    if (this.fuelStack.getCount() < this.fuelStack.getMaxStackSize()) {
                        this.fuelStack.grow(1);
                        heldItem.shrink(1);
                        this.setChanged();
                        if (this.level != null && !this.level.isClientSide()) {
                            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isFuel(ItemStack stack) {
        Item item = stack.getItem();
        return item == Items.COAL || item == Items.CHARCOAL || item == Items.COAL_BLOCK;
    }

    private int getBurnDuration(ItemStack stack) {
        Item item = stack.getItem();
        if (item == Items.COAL) return 1600;
        if (item == Items.CHARCOAL) return 1600;
        if (item == Items.COAL_BLOCK) return 16000;
        return 0;
    }

    public void dropContents() {
        if (this.level != null && !this.fuelStack.isEmpty()) {
            Containers.dropItemStack(this.level, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5, this.fuelStack);
        }
    }

    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        this.dropContents();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("FuelStack", ItemStack.OPTIONAL_CODEC, this.fuelStack);
        output.putInt("BurnTime", this.burnTime);
        output.putInt("MaxBurnTime", this.maxBurnTime);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.fuelStack = input.read("FuelStack", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        this.burnTime = input.getIntOr("BurnTime", 0);
        this.maxBurnTime = input.getIntOr("MaxBurnTime", 0);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider p) {
        return this.saveWithoutMetadata(p);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
