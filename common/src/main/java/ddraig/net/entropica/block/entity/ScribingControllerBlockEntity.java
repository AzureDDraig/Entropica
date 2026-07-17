package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.api.materia.IVaporHandler;
import ddraig.net.entropica.api.materia.IVaporMultiblockController;
import ddraig.net.entropica.api.materia.MateriaFumusStack;
import ddraig.net.entropica.api.materia.MateriaStack;
import ddraig.net.entropica.block.ScribingControllerBlock;
import ddraig.net.entropica.registry.ModBlockEntities;
import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.mojang.serialization.Codec;

public class ScribingControllerBlockEntity extends BlockEntity implements IVaporHandler, IVaporMultiblockController, Container {
    private boolean isFormed = false;
    private MateriaFumusStack storedMateria = MateriaFumusStack.EMPTY;
    private final NonNullList<ItemStack> inventory = NonNullList.withSize(3, ItemStack.EMPTY);

    private int activeProgress = 0;
    private static final int MAX_PROGRESS = 20; // 20 ticks (1 second)

    public ScribingControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SCRIBING_CONTROLLER_BE.get(), pos, state);
    }

    public BlockPos getRelativePos(int rightOffset, int upOffset, int backOffset) {
        Direction facing = this.getBlockState().getValue(ScribingControllerBlock.FACING);
        Direction right = facing.getClockWise();
        Direction back = facing.getOpposite();
        return this.worldPosition.relative(right, rightOffset)
                                 .relative(Direction.UP, upOffset)
                                 .relative(back, backOffset);
    }

    @Override
    public boolean isFormed() {
        return this.isFormed;
    }

    @Override
    public MateriaFumusStack getStoredMateria() {
        return this.storedMateria;
    }

    @Override
    public int getSafeCapacity() {
        return 2000;
    }

    @Override
    public int getAbsoluteCapacity() {
        return 6000;
    }

    @Override
    public @NotNull MateriaStack getMateriaInTank() {
        return this.storedMateria;
    }

    @Override
    public int fill(MateriaStack resource, boolean simulate) {
        if (!this.isFormed || resource.isEmpty()) return 0;
        if (!(resource instanceof MateriaFumusStack)) return 0;

        if (this.storedMateria.isEmpty()) {
            int accepted = Math.min(resource.getAmount(), getSafeCapacity());
            if (!simulate) {
                this.storedMateria = new MateriaFumusStack(resource.getType(), accepted);
                this.setChanged();
                if (level != null && !level.isClientSide()) {
                    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                }
            }
            return accepted;
        }

        if (this.storedMateria.getType() == resource.getType()) {
            int current = this.storedMateria.getAmount();
            int limit = getSafeCapacity();
            int accepted = Math.min(resource.getAmount(), limit - current);
            if (accepted > 0 && !simulate) {
                this.storedMateria.grow(accepted);
                this.setChanged();
                if (level != null && !level.isClientSide()) {
                    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                }
            }
            return accepted;
        }

        return 0;
    }

    @Override
    public @NotNull MateriaStack drain(int maxDrain, boolean simulate) {
        if (this.storedMateria.isEmpty() || maxDrain <= 0) return MateriaFumusStack.EMPTY;
        int current = this.storedMateria.getAmount();
        int drained = Math.min(maxDrain, current);
        MateriaFumusStack result = new MateriaFumusStack(this.storedMateria.getType(), drained);
        if (!simulate) {
            this.storedMateria.shrink(drained);
            if (this.storedMateria.getAmount() <= 0) {
                this.storedMateria = MateriaFumusStack.EMPTY;
            }
            this.setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
        return result;
    }

    @Override
    public void invalidateMultiblock() {
        if (this.isFormed) {
            this.isFormed = false;
            BlockState state = getBlockState();
            if (state.hasProperty(ScribingControllerBlock.FORMED)) {
                level.setBlock(worldPosition, state.setValue(ScribingControllerBlock.FORMED, false), 3);
            }
            this.setChanged();
            level.sendBlockUpdated(worldPosition, state, state, 3);
        }
    }

    public boolean attemptFormMultiblock() {
        if (this.level == null || this.level.isClientSide()) return false;

        // Bottom layer:
        // Center: Viscanite Synthesizer (0, -1, 1)
        BlockPos bottomCenter = getRelativePos(0, -1, 1);
        if (!this.level.getBlockState(bottomCenter).is(ModBlocks.VISCANITE_SYNTHESIZER.get())) return false;

        // Perimeter (8 blocks): Simple Machine Block or Arcane Forge Base
        for (int x = -1; x <= 1; x++) {
            for (int z = 0; z <= 2; z++) {
                if (x == 0 && z == 1) continue; // Skip center
                BlockPos perimeterPos = getRelativePos(x, -1, z);
                BlockState state = this.level.getBlockState(perimeterPos);
                if (!state.is(ModBlocks.MATERIA_SIMPLE_MACHINE_BLOCK.get()) && !state.is(ModBlocks.ARCANE_FORGE_BASE.get())) {
                    return false;
                }
            }
        }

        // Middle layer:
        // Center: Air/cavity (0, 0, 1)
        BlockPos middleCenter = getRelativePos(0, 0, 1);
        if (!this.level.getBlockState(middleCenter).isAir()) return false;

        // Back center: Input Port (0, 0, 2)
        BlockPos middleBack = getRelativePos(0, 0, 2);
        if (!this.level.getBlockState(middleBack).is(ModBlocks.VAPOR_PNEUMATIC_INPUT_PORT.get())) return false;

        // Sides/Corners (6 blocks): Glass
        for (int x = -1; x <= 1; x++) {
            for (int z = 0; z <= 2; z++) {
                if (x == 0 && (z == 0 || z == 1 || z == 2)) continue; // Skip controller, cavity, port
                BlockPos glassPos = getRelativePos(x, 0, z);
                if (!isGlassBlock(glassPos)) return false;
            }
        }

        // Top layer:
        // Center: Viscanite Piston Press (0, 1, 1)
        BlockPos topCenter = getRelativePos(0, 1, 1);
        if (!this.level.getBlockState(topCenter).is(ModBlocks.VISCANITE_PISTON_PRESS.get())) return false;

        // Sides (4 blocks): Simple Machine Block
        BlockPos[] topSides = {
            getRelativePos(0, 1, 0),
            getRelativePos(-1, 1, 1),
            getRelativePos(1, 1, 1),
            getRelativePos(0, 1, 2)
        };
        for (BlockPos sidePos : topSides) {
            if (!this.level.getBlockState(sidePos).is(ModBlocks.MATERIA_SIMPLE_MACHINE_BLOCK.get())) return false;
        }

        // Corners (4 blocks): Glass
        BlockPos[] topCorners = {
            getRelativePos(-1, 1, 0),
            getRelativePos(1, 1, 0),
            getRelativePos(-1, 1, 2),
            getRelativePos(1, 1, 2)
        };
        for (BlockPos cornerPos : topCorners) {
            if (!isGlassBlock(cornerPos)) return false;
        }

        this.isFormed = true;
        BlockState state = getBlockState();
        if (state.hasProperty(ScribingControllerBlock.FORMED)) {
            level.setBlock(worldPosition, state.setValue(ScribingControllerBlock.FORMED, true), 3);
        }
        this.setChanged();
        level.sendBlockUpdated(worldPosition, state, state, 3);
        return true;
    }

    private boolean isGlassBlock(BlockPos pos) {
        BlockState state = this.level.getBlockState(pos);
        Block block = state.getBlock();
        String path = BuiltInRegistries.BLOCK.getKey(block).getPath();
        return path.contains("glass") || block == net.minecraft.world.level.block.Blocks.GLASS;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ScribingControllerBlockEntity controller) {
        if (level.isClientSide()) return;

        // Every 100 ticks check multiblock layout
        if (level.getGameTime() % 100 == 0 && controller.isFormed) {
            if (!controller.attemptFormMultiblock()) {
                controller.invalidateMultiblock();
            }
        }

        if (!controller.isFormed) return;

        controller.tickProcess(level);
    }

    private void tickProcess(Level level) {
        ItemStack blankStone = inventory.get(0);
        ItemStack stencil = inventory.get(1);
        ItemStack outputSlot = inventory.get(2);

        if (blankStone.is(ModItems.BLANK_STONE.get()) && !stencil.isEmpty() && outputSlot.getCount() < outputSlot.getMaxStackSize()) {
            EssenceType requiredAffinity = getRequiredAffinityFromStencil(stencil);
            ItemStack targetOutput = getOutputFromStencil(stencil);

            if (requiredAffinity != null && !targetOutput.isEmpty() && 
                (outputSlot.isEmpty() || ItemStack.isSameItemSameComponents(outputSlot, targetOutput))) {
                
                if (!this.storedMateria.isEmpty() && this.storedMateria.getType() == requiredAffinity && this.storedMateria.getAmount() >= 100) {
                    // Update Piston Press Block Entity to active status
                    BlockPos pressPos = getRelativePos(0, 1, 1);
                    BlockEntity pressBE = level.getBlockEntity(pressPos);
                    if (pressBE instanceof ViscanitePistonPressBlockEntity press) {
                        press.setActive(true);
                        press.setAnimationTick(this.activeProgress);
                    }

                    this.activeProgress++;
                    if (this.activeProgress >= MAX_PROGRESS) {
                        // Complete process
                        this.activeProgress = 0;
                        blankStone.shrink(1);
                        drain(100, false);

                        if (outputSlot.isEmpty()) {
                            inventory.set(2, targetOutput.copy());
                        } else {
                            outputSlot.grow(1);
                        }

                        // Play sound & particles
                        level.playSound(null, this.worldPosition, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.6f, 0.7f);
                        level.playSound(null, this.worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 1.5f);

                        if (pressBE instanceof ViscanitePistonPressBlockEntity press) {
                            press.setActive(false);
                            press.setAnimationTick(0);
                        }
                    }
                    this.setChanged();
                    return;
                }
            }
        }

        // Reset progress if conditions aren't met
        if (this.activeProgress > 0) {
            this.activeProgress = 0;
            BlockPos pressPos = getRelativePos(0, 1, 1);
            BlockEntity pressBE = level.getBlockEntity(pressPos);
            if (pressBE instanceof ViscanitePistonPressBlockEntity press) {
                press.setActive(false);
                press.setAnimationTick(0);
            }
            this.setChanged();
        }
    }

    private EssenceType getRequiredAffinityFromStencil(ItemStack stencil) {
        if (!stencil.has(net.minecraft.core.component.DataComponents.CUSTOM_DATA)) return EssenceType.REGULAR;
        CompoundTag tag = stencil.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA).copyTag();
        String rune = tag.getString("Rune").orElse("").toLowerCase();
        return switch (rune) {
            case "fehu" -> EssenceType.ARID;
            case "uruz", "berkano" -> EssenceType.NATURE;
            case "thurisaz" -> EssenceType.VOID;
            case "ansuz" -> EssenceType.LIGHTNING;
            case "raido", "ehwaz" -> EssenceType.AIR;
            case "kenaz", "sowilo", "dagaz" -> EssenceType.RADIANT;
            case "nauthiz", "tiwaz", "ingwaz" -> EssenceType.EARTH;
            case "isa" -> EssenceType.FROZEN;
            case "jera", "laguz" -> EssenceType.WATER;
            case "perthro" -> EssenceType.UMBRAL;
            case "hagalaz" -> EssenceType.NETHER;
            default -> EssenceType.REGULAR;
        };
    }

    private ItemStack getOutputFromStencil(ItemStack stencil) {
        if (!stencil.has(net.minecraft.core.component.DataComponents.CUSTOM_DATA)) return ItemStack.EMPTY;
        CompoundTag tag = stencil.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA).copyTag();
        String rune = tag.getString("Rune").orElse("").toLowerCase();
        return switch (rune) {
            case "fehu" -> new ItemStack(ModItems.RUNE_FEHU.get());
            case "uruz" -> new ItemStack(ModItems.RUNE_URUZ.get());
            case "thurisaz" -> new ItemStack(ModItems.RUNE_THURISAZ.get());
            case "ansuz" -> new ItemStack(ModItems.RUNE_ANSUZ.get());
            case "raido" -> new ItemStack(ModItems.RUNE_RAIDO.get());
            case "kenaz" -> new ItemStack(ModItems.RUNE_KENAZ.get());
            case "gebo" -> new ItemStack(ModItems.RUNE_GEBO.get());
            case "wunjo" -> new ItemStack(ModItems.RUNE_WUNJO.get());
            case "hagalaz" -> new ItemStack(ModItems.RUNE_HAGALAZ.get());
            case "nauthiz" -> new ItemStack(ModItems.RUNE_NAUTHIZ.get());
            case "isa" -> new ItemStack(ModItems.RUNE_ISA.get());
            case "jera" -> new ItemStack(ModItems.RUNE_JERA.get());
            case "eihwaz" -> new ItemStack(ModItems.RUNE_EIHWAZ.get());
            case "perthro" -> new ItemStack(ModItems.RUNE_PERTHRO.get());
            case "algiz" -> new ItemStack(ModItems.RUNE_ALGIZ.get());
            case "sowilo" -> new ItemStack(ModItems.RUNE_SOWILO.get());
            case "tiwaz" -> new ItemStack(ModItems.RUNE_TIWAZ.get());
            case "berkano" -> new ItemStack(ModItems.RUNE_BERKANO.get());
            case "ehwaz" -> new ItemStack(ModItems.RUNE_EHWAZ.get());
            case "mannaz" -> new ItemStack(ModItems.RUNE_MANNAZ.get());
            case "laguz" -> new ItemStack(ModItems.RUNE_LAGUZ.get());
            case "ingwaz" -> new ItemStack(ModItems.RUNE_INGWAZ.get());
            case "dagaz" -> new ItemStack(ModItems.RUNE_DAGAZ.get());
            case "othala" -> new ItemStack(ModItems.RUNE_OTHALA.get());
            default -> ItemStack.EMPTY;
        };
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.isFormed = input.read("IsFormed", Codec.BOOL).orElse(false);
        this.activeProgress = input.read("ActiveProgress", Codec.INT).orElse(0);
        String typeStr = input.read("MateriaType", Codec.STRING).orElse("");
        int amt = input.read("MateriaAmount", Codec.INT).orElse(0);
        if (!typeStr.isEmpty() && amt > 0) {
            try {
                this.storedMateria = new MateriaFumusStack(EssenceType.valueOf(typeStr), amt);
            } catch (IllegalArgumentException e) {
                this.storedMateria = MateriaFumusStack.EMPTY;
            }
        } else {
            this.storedMateria = MateriaFumusStack.EMPTY;
        }
        for (int i = 0; i < this.inventory.size(); i++) {
            this.inventory.set(i, input.read("Item_" + i, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY));
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("IsFormed", Codec.BOOL, this.isFormed);
        output.store("ActiveProgress", Codec.INT, this.activeProgress);
        if (!this.storedMateria.isEmpty()) {
            output.store("MateriaType", Codec.STRING, this.storedMateria.getType().name());
            output.store("MateriaAmount", Codec.INT, this.storedMateria.getAmount());
        }
        for (int i = 0; i < this.inventory.size(); i++) {
            output.store("Item_" + i, ItemStack.OPTIONAL_CODEC, this.inventory.get(i));
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    // --- Container Methods ---

    @Override
    public int getContainerSize() {
        return 3;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.inventory) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.inventory.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(this.inventory, slot, amount);
        if (!result.isEmpty()) this.setChanged();
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.inventory, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.inventory.set(slot, stack);
        this.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        this.inventory.clear();
        this.setChanged();
    }
}
