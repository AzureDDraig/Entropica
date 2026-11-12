package ddraig.net.entropica.block.entity;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.api.starlight.IWirelessFluxReceiver;
import ddraig.net.entropica.block.AttunedDisplayVitrineBlock;
import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class AttunedDisplayVitrineBlockEntity extends BlockEntity implements IWirelessFluxReceiver {

    private ItemStack displayedItem = ItemStack.EMPTY;
    private String activeStarName = "Astral";
    private EssenceType activeEssence = EssenceType.ASTRAL;
    private int pulseTicksRemaining = 0;
    private int animationTicks = 0;

    public AttunedDisplayVitrineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ATTUNED_DISPLAY_VITRINE_BE.get(), pos, state);
    }

    public ItemStack getDisplayedItem() {
        return displayedItem;
    }

    public void setDisplayedItem(ItemStack stack) {
        this.displayedItem = stack;
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public void receiveFluxPulse(String starName, EssenceType essence) {
        this.activeStarName = starName != null ? starName : "Astral";
        this.activeEssence = essence != null ? essence : EssenceType.ASTRAL;
        this.pulseTicksRemaining = 35;
        setChanged();

        if (level != null && !level.isClientSide()) {
            BlockState current = getBlockState();
            if (current.hasProperty(AttunedDisplayVitrineBlock.LIT) && !current.getValue(AttunedDisplayVitrineBlock.LIT)) {
                level.setBlock(worldPosition, current.setValue(AttunedDisplayVitrineBlock.LIT, true), 3);
            }
        }
    }

    @Override
    public boolean isFluxPowered() {
        return pulseTicksRemaining > 0;
    }

    @Override
    public int getPulseTicksRemaining() {
        return pulseTicksRemaining;
    }

    @Override
    public BlockPos getReceiverPos() {
        return worldPosition;
    }

    public String getActiveStarName() {
        return activeStarName;
    }

    public EssenceType getActiveEssence() {
        return activeEssence;
    }

    public int getAnimationTicks() {
        return animationTicks;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AttunedDisplayVitrineBlockEntity be) {
        be.animationTicks++;

        if (be.pulseTicksRemaining > 0) {
            be.pulseTicksRemaining--;

            if (be.pulseTicksRemaining <= 0 && !level.isClientSide()) {
                if (state.hasProperty(AttunedDisplayVitrineBlock.LIT) && state.getValue(AttunedDisplayVitrineBlock.LIT)) {
                    level.setBlock(pos, state.setValue(AttunedDisplayVitrineBlock.LIT, false), 3);
                }
                be.setChanged();
            }
        }
    }

    public void dropContents() {
        if (level != null && !level.isClientSide() && !displayedItem.isEmpty()) {
            Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, displayedItem);
            displayedItem = ItemStack.EMPTY;
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("ActiveStarName", Codec.STRING, this.activeStarName);
        output.store("ActiveEssence", Codec.STRING, this.activeEssence.name());
        output.store("PulseTicksRemaining", Codec.INT, this.pulseTicksRemaining);
        if (!displayedItem.isEmpty()) {
            output.store("DisplayedItem", ItemStack.CODEC, this.displayedItem);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.activeStarName = input.read("ActiveStarName", Codec.STRING).orElse("Astral");
        String essStr = input.read("ActiveEssence", Codec.STRING).orElse("ASTRAL");
        try {
            this.activeEssence = EssenceType.valueOf(essStr);
        } catch (Exception e) {
            this.activeEssence = EssenceType.ASTRAL;
        }
        this.pulseTicksRemaining = input.read("PulseTicksRemaining", Codec.INT).orElse(0);
        this.displayedItem = input.read("DisplayedItem", ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }
}
