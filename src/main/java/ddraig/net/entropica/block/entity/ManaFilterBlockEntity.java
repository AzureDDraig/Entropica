package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.api.fumes.IFumeHandler;
import ddraig.net.entropica.api.fumes.VisFumeStack;
import ddraig.net.entropica.config.EntropicaConfig;
import ddraig.net.entropica.item.VisFumeAmpouleItem;
import ddraig.net.entropica.registry.ModBlockEntities;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ManaFilterBlockEntity extends BlockEntity {
    private EssenceType filterType = EssenceType.REGULAR;

    public ManaFilterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MANA_FILTER_BE.get(), pos, state);
    }

    // --- AUTO-PUSH TICK LOGIC ---
    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide()) return;

        // Sync the pumping speed with the rest of the Fume network
        if (level.getGameTime() % EntropicaConfig.VIS_FUME_TICK_RATE.get() != 0) return;

        EntropicCoreBlockEntity master = getMasterCore();
        if (master == null || !master.isFormed()) return;

        // Check how much of the filtered materia the core actually has
        int availableMana = master.getMateriaFumusPool().getOrDefault(this.filterType, 0);
        if (availableMana <= 0) return;

        int amountToPush = Math.min(availableMana, EntropicaConfig.VIS_FUME_TRANSFER_RATE.get());

        for (Direction dir : Direction.values()) {
            if (amountToPush <= 0) break;

            BlockEntity neighbor = level.getBlockEntity(pos.relative(dir));

            // If the neighbor can accept Materia Fumes (like our Pipes or Diverters)
            if (neighbor instanceof IFumeHandler handler) {
                VisFumeStack pushStack = new VisFumeStack(this.filterType, amountToPush);

                // Ask the pipe how much it can take
                int accepted = handler.fill(pushStack, false);

                if (accepted > 0) {
                    // Safely extract the exact amount the pipe accepted from the core
                    master.extractMateriaFumus(this.filterType, accepted);
                    amountToPush -= accepted; // Deduct so we don't over-push if we find a second pipe
                }
            }
        }
    }

    public EssenceType getFilterType() {
        return filterType;
    }

    public EntropicCoreBlockEntity getMasterCore() {
        if (level == null) return null;
        for (Direction dir : Direction.values()) {
            BlockEntity be = level.getBlockEntity(worldPosition.relative(dir));
            if (be instanceof MateriaPlumeBlockEntity plume) {
                return plume.getMaster();
            } else if (be instanceof EntropicCoreBlockEntity core) {
                return core.getMaster();
            }
        }
        return null;
    }

    public void cycleFilter() {
        if (level == null) return;

        List<EssenceType> availableTypes = new ArrayList<>();
        EntropicCoreBlockEntity master = getMasterCore();

        if (master != null && master.isFormed()) {
            // Only add types that have materia > 0
            for (Map.Entry<EssenceType, Integer> entry : master.getMateriaFumusPool().entrySet()) {
                if (entry.getValue() > 0) {
                    availableTypes.add(entry.getKey());
                }
            }
        }

        // Cycle logic based on what's actually in the furnace
        if (availableTypes.isEmpty()) {
            this.filterType = EssenceType.REGULAR;
        } else {
            int currentIndex = availableTypes.indexOf(this.filterType);
            // If current type is no longer available, start at 0. Otherwise, get next.
            int nextIndex = (currentIndex == -1) ? 0 : (currentIndex + 1) % availableTypes.size();
            this.filterType = availableTypes.get(nextIndex);
        }

        this.setChanged();
        if (!level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public int requestManaFromCore(int amount) {
        if (level == null) return 0;
        EntropicCoreBlockEntity master = getMasterCore();
        if (master != null && master.isFormed()) {
            return master.extractMateriaFumus(this.filterType, amount);
        }
        return 0;
    }

    public boolean interactWithAmpoule(Player player, ItemStack handStack, InteractionHand hand) {
        EntropicCoreBlockEntity master = getMasterCore();
        if (master == null || !master.isFormed()) {
            player.displayClientMessage(Component.literal("§cFilter is not connected to a valid core."), true);
            return true;
        }

        if (handStack.is(ModItems.SMALL_AMPOULE_BASE.get()) ||
                handStack.is(ModItems.MEDIUM_AMPOULE_BASE.get()) ||
                handStack.is(ModItems.LARGE_AMPOULE_BASE.get())) {

            int capacity = handStack.is(ModItems.SMALL_AMPOULE_BASE.get()) ? 8 :
                    handStack.is(ModItems.MEDIUM_AMPOULE_BASE.get()) ? 32 : 128;

            VisFumeAmpouleItem targetFilledItem = (VisFumeAmpouleItem) (
                    (capacity == 8) ? ModItems.SMALL_VIS_FUME_AMPOULE.get() :
                            (capacity == 32) ? ModItems.MEDIUM_VIS_FUME_AMPOULE.get() : ModItems.LARGE_VIS_FUME_AMPOULE.get());

            int currentMana = master.getMateriaFumusPool().getOrDefault(this.filterType, 0);

            if (currentMana >= capacity) {
                master.extractMateriaFumus(this.filterType, capacity);

                ItemStack filled = new ItemStack(targetFilledItem);
                VisFumeAmpouleItem.setEssenceType(filled, this.filterType);

                handStack.shrink(1);
                if (handStack.isEmpty()) {
                    player.setItemInHand(hand, filled);
                } else if (!player.getInventory().add(filled)) {
                    player.drop(filled, false);
                }

                player.displayClientMessage(Component.literal("§aFiltered " + capacity + " " + this.filterType.getFormattedName() + " materia into ampoule."), true);
                return true;
            } else {
                player.displayClientMessage(Component.literal("§cCore only has " + currentMana + " / " + capacity + " " + this.filterType.getFormattedName() + " materia."), true);
                return true;
            }
        }
        return false;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("FilterType", this.filterType.ordinal());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        int ord = input.getIntOr("FilterType", 0);
        this.filterType = EssenceType.values()[ord % EssenceType.values().length];
    }
}