package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class MateriaEnrichedGlassBlockEntity extends BlockEntity {
    @Nullable
    private BlockPos controllerPos = null;

    @Nullable
    private EssenceType essenceType = null;

    public MateriaEnrichedGlassBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MATERIA_ENRICHED_GLASS_BE.get(), pos, state);
    }

    public void setControllerPos(@Nullable BlockPos pos) {
        this.controllerPos = pos;
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Nullable
    public BlockPos getControllerPos() {
        return controllerPos;
    }

    public void setEssenceType(@Nullable EssenceType type) {
        this.essenceType = type;
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Nullable
    public EssenceType getEssenceType() {
        return essenceType;
    }

    public void notifyControllerOfBreak() {
        if (this.level != null && this.controllerPos != null && !this.level.isClientSide()) {
            BlockEntity be = this.level.getBlockEntity(this.controllerPos);
            if (be instanceof MateriaVesselControllerBlockEntity controller) {
                controller.invalidateMultiblock();
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (this.controllerPos != null) {
            output.putLong("ControllerPos", this.controllerPos.asLong());
        }
        if (this.essenceType != null) {
            output.putString("EssenceType", this.essenceType.name());
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.getLong("ControllerPos").ifPresent(l -> this.controllerPos = BlockPos.of(l));

        String typeStr = input.getStringOr("EssenceType", "");
        if (!typeStr.isEmpty()) {
            try {
                this.essenceType = EssenceType.valueOf(typeStr);
            } catch (IllegalArgumentException e) {
                this.essenceType = null;
            }
        } else {
            this.essenceType = null;
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider p) {
        return this.saveWithoutMetadata(p);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        this.notifyControllerOfBreak();
        super.preRemoveSideEffects(pos, state);
    }
}