package ddraig.net.entropica.block.entity;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.block.AutocastPedestalBlock;
import ddraig.net.entropica.item.*;
import ddraig.net.entropica.registry.ModBlockEntities;
import ddraig.net.entropica.registry.ModDataComponents;
import ddraig.net.entropica.spell.apparatus.ApparatusType;
import ddraig.net.entropica.spell.apparatus.CastingApparatusState;
import ddraig.net.entropica.spell.apparatus.SocketedSpellgem;
import ddraig.net.entropica.spell.assembly.SpellAssembly;
import ddraig.net.entropica.spell.engine.SpellExecutionEngine;
import ddraig.net.entropica.spell.fuel.SpellFuelService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * BlockEntity backing the {@link AutocastPedestalBlock} (Spellcrafting Guide Sections 2.1.7, 8.4).
 *
 * <ul>
 *     <li><b>Slot 0 ({@link #SLOT_SPELL})</b>: Inscribed Spellgem, Casting Apparatus, or Engraved Weapon/Tool.</li>
 *     <li><b>Slot 1 ({@link #SLOT_FUEL})</b>: Orbis Cell, Essence Ampoule, Fume Ampoule, or Raw Essence
 *         (also pulls directly from 6-way adjacent placed Orbis Cells / Calixes / Cores).</li>
 *     <li><b>Cast Interval</b>: Configurable from {@code 20} ticks ({@code 1.0s}) to {@code 1,200} ticks ({@code 60.0s}).</li>
 *     <li><b>Redstone Modes</b>: {@code 0 = Always Active}, {@code 1 = Pause on Redstone}, {@code 2 = Require Redstone}.</li>
 *     <li><b>Inventory Linking</b>: Up to 8 linked container {@link BlockPos} targets within 16 blocks.</li>
 * </ul>
 */
public class AutocastPedestalBlockEntity extends BlockEntity implements Container {

    public static final int SLOT_SPELL = 0;
    public static final int SLOT_FUEL = 1;

    public static final int MIN_INTERVAL_TICKS = 20;     // 1.0s
    public static final int MAX_INTERVAL_TICKS = 1_200;  // 60.0s
    public static final int DEFAULT_INTERVAL_TICKS = 100; // 5.0s

    public static final int REDSTONE_ALWAYS = 0;
    public static final int REDSTONE_PAUSE_WHEN_POWERED = 1;
    public static final int REDSTONE_REQUIRE_POWER = 2;

    public static final int MAX_LINKED_INVENTORIES = 8;
    public static final double MAX_LINK_DISTANCE_SQ = 16.0D * 16.0D;

    private int intervalTicks = DEFAULT_INTERVAL_TICKS;
    private float aimPitch = 0.0F;
    private int redstoneMode = REDSTONE_ALWAYS;
    private int cooldownTicks = 40;
    @Nullable
    private UUID ownerUuid = null;
    private final List<BlockPos> linkedInventories = new ArrayList<>();

    public final SimpleContainer inventory = new SimpleContainer(2) {
        @Override
        public void setChanged() {
            super.setChanged();
            AutocastPedestalBlockEntity.this.markUpdated();
        }
    };

    public AutocastPedestalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.AUTOCAST_PEDESTAL_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AutocastPedestalBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        boolean powered = level.hasNeighborSignal(pos);
        if (be.redstoneMode == REDSTONE_PAUSE_WHEN_POWERED && powered) {
            return;
        }
        if (be.redstoneMode == REDSTONE_REQUIRE_POWER && !powered) {
            return;
        }

        if (be.cooldownTicks > 0) {
            be.cooldownTicks--;
        }
        if (be.cooldownTicks > 0) {
            return;
        }

        if (be.getActiveSpellAssembly().isEmpty()) {
            be.cooldownTicks = Math.min(20, be.intervalTicks);
            return;
        }

        Direction facing = state.hasProperty(AutocastPedestalBlock.FACING)
                ? state.getValue(AutocastPedestalBlock.FACING)
                : Direction.NORTH;

        boolean casted = SpellExecutionEngine.castFromPedestal(serverLevel, pos, facing, be.aimPitch, be);
        be.cooldownTicks = casted ? be.intervalTicks : Math.min(40, be.intervalTicks);
    }

    public static boolean isValidSpellMountStack(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.getItem() instanceof SpellgemItem && SpellgemItem.isInscribed(stack)) {
            return true;
        }
        return stack.has(ModDataComponents.CASTING_APPARATUS_STATE.get());
    }

    public static boolean isValidFuelStack(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.getItem() instanceof OrbisCellItem
                || stack.getItem() instanceof EssenceAmpouleItem
                || stack.getItem() instanceof VisFumeAmpouleItem
                || (stack.getItem() instanceof EssenceItem ess && ess.getTier() >= 1);
    }

    public Optional<SpellAssembly> getActiveSpellAssembly() {
        ItemStack stack = this.inventory.getItem(SLOT_SPELL);
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        if (stack.getItem() instanceof SpellgemItem) {
            return SpellgemItem.getSpell(stack);
        }
        CastingApparatusState appState = stack.get(ModDataComponents.CASTING_APPARATUS_STATE.get());
        if (appState != null) {
            SocketedSpellgem active = appState.getActiveSocket();
            if (!active.isEmpty()) {
                return active.spell();
            }
        }
        return Optional.empty();
    }

    @Nullable
    public ApparatusType getMountedApparatusType() {
        ItemStack stack = this.inventory.getItem(SLOT_SPELL);
        CastingApparatusState appState = stack.get(ModDataComponents.CASTING_APPARATUS_STATE.get());
        return appState != null ? appState.apparatusType() : null;
    }

    public float getMountedMaterialEfficiencyBonus() {
        ItemStack stack = this.inventory.getItem(SLOT_SPELL);
        CastingApparatusState appState = stack.get(ModDataComponents.CASTING_APPARATUS_STATE.get());
        return appState != null ? appState.materialEfficiencyBonus() : 0.0F;
    }

    public boolean hasMountedSpellgem() {
        return getActiveSpellAssembly().isPresent();
    }

    public boolean hasOrbisConnection() {
        if (!this.inventory.getItem(SLOT_FUEL).isEmpty()) {
            return true;
        }
        if (this.level == null) {
            return false;
        }
        for (Direction dir : Direction.values()) {
            BlockEntity adj = this.level.getBlockEntity(this.worldPosition.relative(dir));
            if (adj != null && DecompressionCouplerBlockEntity.getMateriaTier(adj) >= 2) {
                return true;
            }
        }
        return false;
    }

    public int getSpellTintRgb() {
        Optional<SpellAssembly> opt = getActiveSpellAssembly();
        if (opt.isEmpty()) {
            return 0xFFFFFF;
        }
        SpellAssembly spell = opt.get();
        EssenceType primary = null;
        for (EssenceType t : spell.getDistinctMateriaTypes()) {
            if (t != null && t != EssenceType.REGULAR) {
                primary = t;
                break;
            }
        }
        if (primary == null) {
            primary = this.level != null
                    ? SpellFuelService.detectPedestalAvailableMateria(this.level, this.worldPosition, this.inventory, SLOT_FUEL)
                    : EssenceType.REGULAR;
        }
        if (primary == null) {
            primary = EssenceType.REGULAR;
        }
        return primary.getColorInt() & 0xFFFFFF;
    }

    public int getIntervalTicks() {
        return this.intervalTicks;
    }

    public void setIntervalTicks(int intervalTicks) {
        this.intervalTicks = Math.clamp(intervalTicks, MIN_INTERVAL_TICKS, MAX_INTERVAL_TICKS);
        if (this.cooldownTicks > this.intervalTicks) {
            this.cooldownTicks = this.intervalTicks;
        }
        markUpdated();
    }

    public float getAimPitch() {
        return this.aimPitch;
    }

    public void setAimPitch(float aimPitch) {
        this.aimPitch = Math.clamp(aimPitch, -90.0F, 90.0F);
        markUpdated();
    }

    public int getRedstoneMode() {
        return this.redstoneMode;
    }

    public void setRedstoneMode(int redstoneMode) {
        this.redstoneMode = Math.clamp(redstoneMode, REDSTONE_ALWAYS, REDSTONE_REQUIRE_POWER);
        markUpdated();
    }

    @Nullable
    public UUID getOwnerUuid() {
        return this.ownerUuid;
    }

    public void setOwnerUuid(@Nullable UUID ownerUuid) {
        this.ownerUuid = ownerUuid;
        setChanged();
    }

    public List<BlockPos> getLinkedInventories() {
        return Collections.unmodifiableList(this.linkedInventories);
    }

    public boolean addLinkedInventory(BlockPos targetPos) {
        if (targetPos.equals(this.worldPosition)) {
            return false;
        }
        if (this.worldPosition.distSqr(targetPos) > MAX_LINK_DISTANCE_SQ) {
            return false;
        }
        if (this.linkedInventories.contains(targetPos)) {
            return true;
        }
        if (this.linkedInventories.size() >= MAX_LINKED_INVENTORIES) {
            this.linkedInventories.removeFirst();
        }
        this.linkedInventories.add(targetPos.immutable());
        markUpdated();
        return true;
    }

    public void clearLinkedInventories() {
        if (!this.linkedInventories.isEmpty()) {
            this.linkedInventories.clear();
            markUpdated();
        }
    }

    public void applyClientUpdate(int newIntervalTicks, float newAimPitch, int newRedstoneMode, boolean clearLinks) {
        this.intervalTicks = Math.clamp(newIntervalTicks, MIN_INTERVAL_TICKS, MAX_INTERVAL_TICKS);
        this.aimPitch = Math.clamp(newAimPitch, -90.0F, 90.0F);
        this.redstoneMode = Math.clamp(newRedstoneMode, REDSTONE_ALWAYS, REDSTONE_REQUIRE_POWER);
        if (this.cooldownTicks > this.intervalTicks) {
            this.cooldownTicks = this.intervalTicks;
        }
        if (clearLinks) {
            this.linkedInventories.clear();
        }
        markUpdated();
    }

    public void markUpdated() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    // ---- Container delegation ----

    @Override
    public int getContainerSize() {
        return this.inventory.getContainerSize();
    }

    @Override
    public boolean isEmpty() {
        return this.inventory.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.inventory.getItem(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return this.inventory.removeItem(slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return this.inventory.removeItemNoUpdate(slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.inventory.setItem(slot, stack);
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player, 8.0F);
    }

    @Override
    public void clearContent() {
        this.inventory.clearContent();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("IntervalTicks", this.intervalTicks);
        output.putFloat("AimPitch", this.aimPitch);
        output.putInt("RedstoneMode", this.redstoneMode);
        output.putInt("CooldownTicks", this.cooldownTicks);
        if (this.ownerUuid != null) {
            output.store("OwnerUuid", UUIDUtil.CODEC, this.ownerUuid);
        }
        if (!this.linkedInventories.isEmpty()) {
            output.store("LinkedInventories", BlockPos.CODEC.listOf(), this.linkedInventories);
        }
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack stack = this.inventory.getItem(i);
            if (!stack.isEmpty()) {
                output.store("Slot" + i, ItemStack.OPTIONAL_CODEC, stack);
            }
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.intervalTicks = Math.clamp(input.getIntOr("IntervalTicks", DEFAULT_INTERVAL_TICKS), MIN_INTERVAL_TICKS, MAX_INTERVAL_TICKS);
        this.aimPitch = Math.clamp(input.getFloatOr("AimPitch", 0.0F), -90.0F, 90.0F);
        this.redstoneMode = Math.clamp(input.getIntOr("RedstoneMode", REDSTONE_ALWAYS), REDSTONE_ALWAYS, REDSTONE_REQUIRE_POWER);
        this.cooldownTicks = Math.max(0, input.getIntOr("CooldownTicks", 20));
        this.ownerUuid = input.read("OwnerUuid", UUIDUtil.CODEC).orElse(null);

        this.linkedInventories.clear();
        input.read("LinkedInventories", BlockPos.CODEC.listOf()).ifPresent(list -> {
            for (BlockPos p : list) {
                if (this.linkedInventories.size() < MAX_LINKED_INVENTORIES) {
                    this.linkedInventories.add(p.immutable());
                }
            }
        });

        this.inventory.clearContent();
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            final int idx = i;
            input.read("Slot" + i, ItemStack.OPTIONAL_CODEC).ifPresent(s -> this.inventory.setItem(idx, s));
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}
