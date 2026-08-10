package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.config.EntropicaConfig;
import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class MateriaFurnaceBlockEntity extends BlockEntity {
    private int materia = 0;
    private int essence = 0;

    private boolean isActive = false;
    private int tickCounter = 0;

    public MateriaFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MATERIA_FURNACE_BE.get(), pos, state);
    }

    private int getEssenceValue(ItemStack stack) {
        String itemName = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();

        if (itemName.contains("weak_") || itemName.contains("small_")) {
            return 1;
        } else if (itemName.contains("average_") || itemName.contains("medium_")) {
            return 4;
        } else if (itemName.contains("strong_") || itemName.contains("large_")) {
            return 16;
        }
        return 0;
    }

    public boolean tryInsertEssenceItem(ItemStack stack) {
        int essenceAmount = getEssenceValue(stack);

        if (essenceAmount > 0 && this.essence + essenceAmount <= EntropicaConfig.MATERIA_FURNACE_MAX_ESSENCE.get()) {
            this.essence += essenceAmount;
            this.setChanged();

            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
            return true;
        }
        return false;
    }

    public void toggleFurnace(Player player) {
        if (this.isActive) {
            this.isActive = false;
            player.displayClientMessage(Component.translatable("msg.entropica.materia_furnace_deactivated"), true);
        } else if (this.essence >= 10) {
            this.isActive = true;
            player.displayClientMessage(Component.translatable("msg.entropica.materia_furnace_activated"), true);
        } else {
            player.displayClientMessage(Component.translatable("msg.entropica.materia_furnace_not_enough_essence_to"), true);
        }

        this.setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide()) return;

        if (this.isActive) {
            this.tickCounter++;

            if (this.tickCounter >= 5) {
                this.tickCounter = 0;

                int generatedMateria = EntropicaConfig.MATERIA_PER_ESSENCE.get();

                if (this.essence > 0 && this.materia + generatedMateria <= EntropicaConfig.MATERIA_FURNACE_MAX_MATERIA.get()) {
                    this.essence -= 1;
                    this.materia += generatedMateria;
                    this.setChanged();
                    level.sendBlockUpdated(pos, state, state, 3);
                } else if (this.essence <= 0 || this.materia >= EntropicaConfig.MATERIA_FURNACE_MAX_MATERIA.get()) {
                    // Auto-shutdown if out of fuel or full of materia
                    this.isActive = false;
                    this.setChanged();
                    level.sendBlockUpdated(pos, state, state, 3);
                }
            }
        }
    }

    // ====================================================================
    // 1. DATA SERIALIZATION
    // ====================================================================

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("FurnaceMateria", this.materia);
        output.putInt("FurnaceEssence", this.essence);
        output.putBoolean("FurnaceActive", this.isActive);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.materia = input.getIntOr("FurnaceMateria", 0);
        this.essence = input.getIntOr("FurnaceEssence", 0);
        this.isActive = input.getBooleanOr("FurnaceActive", false);
    }

    // ====================================================================
    // 2. CLIENT SYNCING
    // ====================================================================

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // ====================================================================
    // 3. GETTERS
    // ====================================================================

    public float getManaPercent() {
        return (float) this.materia / EntropicaConfig.MATERIA_FURNACE_MAX_MATERIA.get();
    }

    public int getMaxEssence() {
        return EntropicaConfig.MATERIA_FURNACE_MAX_ESSENCE.get();
    }

    public float getEssencePercent() {
        int max = getMaxEssence();
        return max > 0 ? (float) this.essence / max : 0f;
    }

    public boolean isActive() {
        return this.isActive;
    }
}