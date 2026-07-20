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

public class VoltaicCalixBlockEntity extends BlockEntity {
    private int mana = 0;
    private EssenceType manaType = null;

    public VoltaicCalixBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VOLTAIC_CALIX_BE.get(), pos, state);
    }

    public int getMana() { return mana; }
    public void setMana(int mana) {
        this.mana = mana;
        this.setChanged();
    }

    public EssenceType getManaType() { return manaType; }
    public void setManaType(EssenceType type) {
        this.manaType = type;
        this.setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("StoredMateria5", this.mana);
        if (this.manaType != null) {
            output.putString("MateriaType", this.manaType.name());
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.mana = input.getIntOr("StoredMateria5", 0);

        String typeStr = input.getStringOr("MateriaType", "");
        if (!typeStr.isEmpty()) {
            try {
                this.manaType = EssenceType.valueOf(typeStr);
            } catch (IllegalArgumentException e) {
                this.manaType = null;
            }
        }
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("StoredMateria5", this.mana);
        if (this.manaType != null) {
            tag.putString("MateriaType", this.manaType.name());
        }
        return tag;
    }
}
