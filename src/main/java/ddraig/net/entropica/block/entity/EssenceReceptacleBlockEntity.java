package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Represents a block entity for the Essence Receptacle in the game.
 * This block entity is used as part of a multiblock structure centered around an
 * Entropic Core. The Essence Receptacle interacts with this core and automates
 * resource management tasks such as exposing buffers for automation.
 */
public class EssenceReceptacleBlockEntity extends BlockEntity {
    private BlockPos masterPos = null;
    // A dummy container to return when the multiblock is broken so pipes don't crash
    private final SimpleContainer emptyContainer = new SimpleContainer(0);

    public EssenceReceptacleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ESSENCE_RECEPTACLE_BE.get(), pos, state);
    }

    public void setMasterPos(BlockPos pos) {
        this.masterPos = pos;
        this.setChanged();
    }

    public EntropicCoreBlockEntity getMaster() {
        if (level != null && masterPos != null && level.getBlockEntity(masterPos) instanceof EntropicCoreBlockEntity core) {
            return core.getMaster();
        }
        return null;
    }

    /**
     * Exposes the Master Core's buffer to automation (Hoppers, Pipes, etc.).
     * If the structure is unformed, returns an empty dummy container to prevent crashes.
     */
    public Container getContainer() {
        EntropicCoreBlockEntity master = getMaster();
        if (master != null && master.isFormed()) {
            return master.sharedReceptacleBuffer;
        }
        return emptyContainer;
    }
}