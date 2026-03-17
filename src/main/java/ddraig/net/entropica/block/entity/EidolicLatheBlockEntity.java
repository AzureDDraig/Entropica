package ddraig.net.entropica.block.entity;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.block.AttunementPedestalBlock;
import ddraig.net.entropica.block.EidolicFocalPedestalBlock;
import ddraig.net.entropica.block.VisFumeInputPortBlock;
import ddraig.net.entropica.block.VisIchorInputPortBlock;
import ddraig.net.entropica.registry.ModBlockEntities;
import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

public class EidolicLatheBlockEntity extends BlockEntity {
    public final SimpleContainer inventory = new SimpleContainer(4) {
        @Override
        public void setChanged() {
            super.setChanged();
            EidolicLatheBlockEntity.this.setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    private boolean isFormed = false;
    private int recheckDelay = -1;
    private int tickCount = 0;

    public final List<BlockPos> connectedPedestals = new ArrayList<>();

    public EidolicLatheBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.EIDOLIC_FOCAL_PEDESTAL_BE.get(), pos, state);
    }

    // Omitted @Override to prevent strict compiler errors, but NeoForge will still use this to prevent culling!
    public AABB getRenderBoundingBox() {
        return new AABB(this.worldPosition).inflate(15.0);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide()) return;

        tickCount++;

        // POLLING: If formed, proactively verify the structure every 10 ticks (0.5 seconds).
        // (Change to tickCount % 3 == 0 if you want it to be hyper-responsive!)
        if (this.isFormed && tickCount % 10 == 0) {
            this.attemptFormMultiblock();
        }

        if (this.recheckDelay > 0) {
            this.recheckDelay--;
            if (this.recheckDelay == 0) {
                boolean wasFormed = this.isFormed;
                boolean success = this.attemptFormMultiblock();
                if (wasFormed != success) {
                    level.setBlock(pos, state.setValue(EidolicFocalPedestalBlock.FORMED, success), 3);
                }
            }
        }
    }

    public boolean attemptFormMultiblock() {
        if (this.level == null || this.level.isClientSide()) return false;

        int baseX = this.worldPosition.getX();
        int baseY = this.worldPosition.getY() - 1; // Assuming the base starts 1 block below the pedestal
        int baseZ = this.worldPosition.getZ();

        int attunementCount = 0;
        int portCount = 0;
        List<BlockPos> foundPedestals = new ArrayList<>();

        // Scan the 7x7x5 main structure
        for (int y = 0; y < 5; y++) {
            for (int x = -3; x <= 3; x++) {
                for (int z = -3; z <= 3; z++) {
                    BlockPos scanPos = new BlockPos(baseX + x, baseY + y, baseZ + z);
                    BlockState scanState = this.level.getBlockState(scanPos);
                    Block block = scanState.getBlock();

                    boolean isPort = (block instanceof VisFumeInputPortBlock || block instanceof VisIchorInputPortBlock);
                    boolean isAirOrPort = scanState.isAir() || isPort;

                    if (isPort) portCount++;

                    if (y == 0) { // Layer 1 (Base Floor)
                        if (block != ModBlocks.ARCANE_FORGE_BASE.get()) return failFormation();
                    } else if (y == 1) { // Layer 2 (Pedestals & Pillars)
                        if (x == 0 && z == 0) {
                            if (!scanPos.equals(this.worldPosition)) return failFormation();
                        } else if (Math.abs(x) == 3 && Math.abs(z) == 3) {
                            if (block != ModBlocks.ARCANE_FORGE_BASE.get()) return failFormation();
                        } else {
                            if (block instanceof AttunementPedestalBlock) {
                                attunementCount++;
                                foundPedestals.add(scanPos);
                            } else if (!isAirOrPort) {
                                return failFormation();
                            }
                        }
                    } else if (y == 2 || y == 3) { // Layer 3 & 4 (Pillars Only)
                        if (Math.abs(x) == 3 && Math.abs(z) == 3) {
                            if (block != ModBlocks.ARCANE_FORGE_BASE.get()) return failFormation();
                        } else {
                            if (!isAirOrPort) return failFormation();
                        }
                    } else if (y == 4) { // Layer 5 (Outer Ring)
                        if (Math.abs(x) == 3 || Math.abs(z) == 3) {
                            if (block != ModBlocks.ARCANE_FORGE_BASE.get()) return failFormation();
                        } else {
                            if (!isAirOrPort) return failFormation();
                        }
                    }
                }
            }
        }

        // Layer 6 (Air Check / Floating Ports Check exactly above the structure)
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                BlockPos scanPos = new BlockPos(baseX + x, baseY + 5, baseZ + z);
                Block block = this.level.getBlockState(scanPos).getBlock();
                if (block instanceof VisFumeInputPortBlock || block instanceof VisIchorInputPortBlock) {
                    portCount++;
                }
            }
        }

        if (portCount < 1) return failFormation();
        if (attunementCount < 4 || attunementCount > 8) return failFormation();

        // Check if the pedestals actually changed to avoid unnecessary block updates
        boolean pedestalsChanged = !this.connectedPedestals.equals(foundPedestals);

        if (!this.isFormed || pedestalsChanged) {
            this.connectedPedestals.clear();
            this.connectedPedestals.addAll(foundPedestals);

            this.isFormed = true;
            updateBlockState(true);
            this.setChanged();

            // Immediately notify the client to turn ON the rings and add new connections
            if (this.level != null) {
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            }
        }
        return true;
    }

    private boolean failFormation() {
        if (this.isFormed) {
            this.isFormed = false;
            updateBlockState(false);
            this.setChanged();

            // Immediately notify the client to turn OFF the rings
            if (this.level != null) {
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            }

            // Queue a check just in case the break was temporary (like accidentally replacing a block)
            this.recheckDelay = 5;
        }
        return false;
    }

    private void updateBlockState(boolean formed) {
        if (this.level != null) {
            BlockState state = this.level.getBlockState(this.worldPosition);
            if (state.hasProperty(EidolicFocalPedestalBlock.FORMED) && state.getValue(EidolicFocalPedestalBlock.FORMED) != formed) {
                this.level.setBlock(this.worldPosition, state.setValue(EidolicFocalPedestalBlock.FORMED, formed), 3);
            }
        }
    }

    public boolean isFormed() {
        return isFormed;
    }

    public boolean interactWithPlayer(Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);

        // Extract
        if (heldItem.isEmpty()) {
            for (int i = 3; i >= 0; i--) {
                ItemStack stackInSlot = inventory.getItem(i);
                if (!stackInSlot.isEmpty()) {
                    player.setItemInHand(hand, stackInSlot.copy());
                    inventory.setItem(i, ItemStack.EMPTY);
                    return true;
                }
            }
        }
        // Insert
        else {
            for (int i = 0; i < 4; i++) {
                ItemStack stackInSlot = inventory.getItem(i);
                if (stackInSlot.isEmpty()) {
                    inventory.setItem(i, heldItem.copyWithCount(1));
                    heldItem.shrink(1);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (this.level != null) {
            Containers.dropContents(this.level, pos, this.inventory);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("IsFormed", Codec.BOOL, this.isFormed);
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            output.store("LatheStack_" + i, ItemStack.OPTIONAL_CODEC, inventory.getItem(i));
        }

        // SERIALIZATION FIX: Safely serializes the entire array cleanly for the client
        output.store("ConnectedPedestals", BlockPos.CODEC.listOf(), this.connectedPedestals);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.isFormed = input.read("IsFormed", Codec.BOOL).orElse(false);
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            int finalI = i;
            input.read("LatheStack_" + i, ItemStack.OPTIONAL_CODEC).ifPresent(stack -> inventory.setItem(finalI, stack));
        }

        this.connectedPedestals.clear();
        input.read("ConnectedPedestals", BlockPos.CODEC.listOf()).ifPresent(this.connectedPedestals::addAll);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider p) { return this.saveWithoutMetadata(p); }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}