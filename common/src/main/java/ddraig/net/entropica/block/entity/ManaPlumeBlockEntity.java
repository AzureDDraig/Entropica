package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

public class ManaPlumeBlockEntity extends BlockEntity {
    private BlockPos masterPos = null;

    public ManaPlumeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MANA_PLUME_BE.get(), pos, state);
    }

    public void setMasterPos(BlockPos pos) {
        this.masterPos = pos;
        this.setChanged();
    }

    // Public so the Filter can use it to check available mana types
    public EntropicCoreBlockEntity getMaster() {
        if (level != null && masterPos != null && level.getBlockEntity(masterPos) instanceof EntropicCoreBlockEntity core) {
            return core.getMaster();
        }
        return null;
    }

    public boolean isCoreActive() {
        EntropicCoreBlockEntity master = getMaster();
        return master != null && master.isFormed() && master.isActive();
    }

    public int getParticleColor() {
        EntropicCoreBlockEntity master = getMaster();
        if (master != null) {
            return master.getEssencePool().entrySet().stream()
                    .filter(e -> e.getValue() > 0)
                    .max(Map.Entry.comparingByValue())
                    .map(e -> e.getKey().getColorInt())
                    .orElse(0xFFFFFF);
        }
        return 0xFFFFFF;
    }

    public int extractManaFromAttachedCore(EssenceType type, int amount) {
        EntropicCoreBlockEntity master = getMaster();
        return master != null ? master.extractMana(type, amount) : 0;
    }
}