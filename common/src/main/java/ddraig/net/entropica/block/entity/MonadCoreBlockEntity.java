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

public class MonadCoreBlockEntity extends BlockEntity {
    private int materia = 0;
    private EssenceType materiaType = null;

    public MonadCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MONAD_CORE_BE.get(), pos, state);
    }

    public int getMateria() { return materia; }
    public void setMateria(int materia) {
        this.materia = materia;
        this.setChanged();
    }

    public EssenceType getManaType() { return materiaType; }
    public void setManaType(EssenceType type) {
        this.materiaType = type;
        this.setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("StoredMateria9", this.materia);
        if (this.materiaType != null) {
            output.putString("MateriaType", this.materiaType.name());
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.materia = input.getIntOr("StoredMateria9", 0);

        String typeStr = input.getStringOr("MateriaType", "");
        if (!typeStr.isEmpty()) {
            try {
                this.materiaType = EssenceType.valueOf(typeStr);
            } catch (IllegalArgumentException e) {
                this.materiaType = null;
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
        tag.putInt("StoredMateria9", this.materia);
        if (this.materiaType != null) {
            tag.putString("MateriaType", this.materiaType.name());
        }
        return tag;
    }
}
