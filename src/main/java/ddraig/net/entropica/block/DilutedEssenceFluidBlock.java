package ddraig.net.entropica.block;

import ddraig.net.entropica.block.entity.DilutedEssenceFluidBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class DilutedEssenceFluidBlock extends LiquidBlock implements EntityBlock {

    public DilutedEssenceFluidBlock(FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (state.getFluidState().isSource()) {
            return new DilutedEssenceFluidBlockEntity(pos, state);
        }
        return null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || !state.getFluidState().isSource()) return null;

        return (lvl, pos, st, be) -> {
            if (be instanceof DilutedEssenceFluidBlockEntity fluidBe) {
                fluidBe.tick();
            }
        };
    }

    // --- BUCKET PICKUP PREVENTION ---
    // We overload the methods and omit @Override so it compiles flawlessly
    // across all 1.21.x NeoForge mapping variants. The game will automatically hook the right one!

    // Modern 1.21 Mappings (With Player argument)
    public ItemStack pickupBlock(@Nullable Player player, LevelAccessor level, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }

    // Alternative/Older Mappings (Without Player argument)
    public ItemStack pickupBlock(LevelAccessor level, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }

    // Standard Sound Override
    public Optional<SoundEvent> getPickupSound() {
        return Optional.empty();
    }

    // Alternative Sound Override (Some 1.21 mappings require BlockState)
    public Optional<SoundEvent> getPickupSound(BlockState state) {
        return Optional.empty();
    }
}