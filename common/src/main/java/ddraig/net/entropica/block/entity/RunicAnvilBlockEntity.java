package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.block.DoubleWorkstationPart;
import ddraig.net.entropica.block.RunicAnvilBlock;
import ddraig.net.entropica.item.CastingApparatusItem;
import ddraig.net.entropica.item.SpellgemItem;
import ddraig.net.entropica.registry.ModBlockEntities;
import ddraig.net.entropica.registry.ModDataComponents;
import ddraig.net.entropica.registry.ModItems;
import ddraig.net.entropica.spell.apparatus.ApparatusType;
import ddraig.net.entropica.spell.apparatus.CastingApparatusState;
import ddraig.net.entropica.spell.apparatus.SocketedSpellgem;
import ddraig.net.entropica.spell.assembly.SpellAssembly;
import ddraig.net.entropica.spell.assembly.SubSpellSlot;
import ddraig.net.entropica.spell.vfx.SpellParticleHelper;
import ddraig.net.entropica.spell.vfx.SpellWorldVfxManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * BlockEntity backing the 2-block-wide {@link RunicAnvilBlock}.
 *
 * <p>Holds the two input parts ({@link #SLOT_BASE} and {@link #SLOT_CATALYST}), orchestrates the
 * 3D Magic Circle convergence and Ethereal Hammer strike sequence ({@link #TOTAL_FORGE_TICKS}),
 * and keeps the finished forged item floating above the anvil and stored in {@link #SLOT_OUTPUT}.</p>
 */
public class RunicAnvilBlockEntity extends BlockEntity implements WorldlyContainer {

    public static final int SLOT_BASE = 0;
    public static final int SLOT_CATALYST = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOT_RETURNED_GEM = 3;

    public static final int TOTAL_FORGE_TICKS = 68;
    public static final int SMASH_IMPACT_TICK = 48;

    private static final int[] OUTPUT_SLOTS = new int[]{SLOT_OUTPUT, SLOT_RETURNED_GEM};
    private static final int[] INPUT_SLOTS = new int[]{SLOT_BASE, SLOT_CATALYST};

    public final SimpleContainer inventory = new SimpleContainer(4) {
        @Override
        public void setChanged() {
            super.setChanged();
            RunicAnvilBlockEntity.this.markUpdated();
        }
    };

    private int forgeTicks = 0;
    private int primaryRgb = 0x62E8FF;
    private int accentRgb = 0xFFD45C;
    private int essenceOrdinal = EssenceType.REGULAR.ordinal();

    public RunicAnvilBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RUNIC_ANVIL_BE.get(), pos, state);
    }

    public boolean isPrimaryHalf() {
        BlockState state = getBlockState();
        return !state.hasProperty(RunicAnvilBlock.PART)
                || state.getValue(RunicAnvilBlock.PART) == DoubleWorkstationPart.LEFT;
    }

    public boolean isForging() {
        return this.forgeTicks > 0;
    }

    public int getForgeTicks() {
        return this.forgeTicks;
    }

    public int getPrimaryRgb() {
        return this.primaryRgb;
    }

    public int getAccentRgb() {
        return this.accentRgb;
    }

    public EssenceType getVisualEssence() {
        return SpellParticleHelper.fromOrdinal(this.essenceOrdinal);
    }

    public boolean hasCompletedOutput() {
        return !this.inventory.getItem(SLOT_OUTPUT).isEmpty()
                || !this.inventory.getItem(SLOT_RETURNED_GEM).isEmpty();
    }

    public void markUpdated() {
        setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    /**
     * Computes the world-space center of the 2-block Runic Anvil striking deck.
     */
    public Vec3 getAnvilCenterWorldPos() {
        BlockState state = getBlockState();
        Direction facing = state.hasProperty(RunicAnvilBlock.FACING)
                ? state.getValue(RunicAnvilBlock.FACING)
                : Direction.SOUTH;
        Direction partnerDir = RunicAnvilBlock.getPartnerDirection(facing, DoubleWorkstationPart.LEFT);
        return new Vec3(
                this.worldPosition.getX() + 0.5D + partnerDir.getStepX() * 0.5D,
                this.worldPosition.getY() + 1.02D,
                this.worldPosition.getZ() + 0.5D + partnerDir.getStepZ() * 0.5D
        );
    }

    /**
     * Validates whether the given Base (Apparatus/Weapon/Tool) and Catalyst (Inscribed Spellgem / Alloy Ingot)
     * can be forged together, returning {@code null} if valid or an action-bar error message if invalid.
     */
    @Nullable
    public static String validatePair(ItemStack base, ItemStack catalyst) {
        if (base.isEmpty() || catalyst.isEmpty()) {
            return "Hold or place both parts on the Runic Anvil.";
        }
        boolean isDedicated = base.getItem() instanceof CastingApparatusItem;
        boolean isEngravable = !isDedicated && RunicAnvilBlock.isEngravableWeaponOrTool(base);
        if (!isDedicated && !isEngravable) {
            return "Only Casting Apparatuses, Weapons, or Tools can be forged here.";
        }

        float ingotBonus = RunicAnvilBlock.getTemperingAlloyBonus(catalyst);
        if (ingotBonus > 0.0F) {
            ApparatusType targetType = isDedicated
                    ? ((CastingApparatusItem) base.getItem()).getApparatusType()
                    : ApparatusType.ENGRAVED_TOOL;
            CastingApparatusState curState = base.get(ModDataComponents.CASTING_APPARATUS_STATE.get());
            if (curState == null) {
                curState = CastingApparatusState.emptyFor(targetType);
            }
            if (ingotBonus <= curState.materialEfficiencyBonus() + 1.0E-4F) {
                return String.format("Already tempered to %.0f%% Materia efficiency (use a higher-tier alloy).",
                        curState.materialEfficiencyBonus() * 100.0F);
            }
            return null;
        }

        if (SpellgemItem.isInscribed(catalyst) && catalyst.get(ModDataComponents.SPELL_ASSEMBLY.get()) != null) {
            return null;
        }

        return "Requires an Inscribed Spellgem (to socket/engrave) or Entropica Alloy Ingot (to temper).";
    }

    /**
     * Starts the 3D Magic Circle + Ethereal Hammer sequence if both parts are present and valid.
     */
    public boolean tryBeginForging() {
        if (this.forgeTicks > 0 || hasCompletedOutput()) {
            return false;
        }
        ItemStack base = this.inventory.getItem(SLOT_BASE);
        ItemStack catalyst = this.inventory.getItem(SLOT_CATALYST);
        if (validatePair(base, catalyst) != null) {
            return false;
        }

        resolveVisualPalette(catalyst);
        this.forgeTicks = 1;
        if (this.level != null && !this.level.isClientSide()) {
            Vec3 center = getAnvilCenterWorldPos();
            this.level.playSound(null, BlockPos.containing(center), SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 0.75F, 1.45F);
            this.level.playSound(null, BlockPos.containing(center), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 0.85F, 1.15F);
        }
        markUpdated();
        return true;
    }

    private void resolveVisualPalette(ItemStack catalyst) {
        if (SpellgemItem.isInscribed(catalyst)) {
            SpellAssembly spell = catalyst.get(ModDataComponents.SPELL_ASSEMBLY.get());
            EssenceType primary = EssenceType.REGULAR;
            EssenceType secondary = null;
            if (spell != null) {
                if (!spell.subSpells().isEmpty()) {
                    SubSpellSlot first = spell.subSpells().getFirst();
                    primary = first.primaryEffect().materiaType();
                    if (primary == EssenceType.REGULAR) {
                        primary = first.formPart().materiaType();
                    }
                    if (first.secondaryEffect().isPresent()) {
                        secondary = first.secondaryEffect().get().materiaType();
                    }
                }
                if (primary == EssenceType.REGULAR) {
                    primary = spell.flowPart().materiaType();
                }
            }
            long gameTime = this.level != null ? this.level.getGameTime() : 0L;
            this.essenceOrdinal = primary.ordinal();
            this.primaryRgb = SpellParticleHelper.resolveVibrantPrimaryRgb(primary, gameTime);
            this.accentRgb = SpellParticleHelper.resolveVibrantAccentRgb(primary, secondary, gameTime);
            return;
        }

        this.essenceOrdinal = EssenceType.REGULAR.ordinal();
        if (catalyst.is(ModItems.ATHANORITE_INGOT.get()) || catalyst.is(ModItems.CHARGED_ATHANORITE_INGOT.get())) {
            this.primaryRgb = 0xFFD45C;
            this.accentRgb = 0xFFF4B8;
        } else if (catalyst.is(ModItems.SANGUINITE_INGOT.get()) || catalyst.is(ModItems.CHARGED_SANGUINITE_INGOT.get())) {
            this.primaryRgb = 0xFF3355;
            this.accentRgb = 0xFFB36B;
        } else if (catalyst.is(ModItems.MERCURITE_INGOT.get()) || catalyst.is(ModItems.CHARGED_MERCURITE_INGOT.get())) {
            this.primaryRgb = 0xB8F2FF;
            this.accentRgb = 0x7A8CFF;
        } else if (catalyst.is(ModItems.EUCLIDITE_INGOT.get()) || catalyst.is(ModItems.CHARGED_EUCLIDITE_INGOT.get())) {
            this.primaryRgb = 0x4CA6FF;
            this.accentRgb = 0xD478FF;
        } else if (catalyst.is(ModItems.EIDOLITE_INGOT.get()) || catalyst.is(ModItems.CHARGED_EIDOLITE_INGOT.get())) {
            this.primaryRgb = 0xD896FF;
            this.accentRgb = 0x80F5FF;
        } else if (catalyst.is(ModItems.RESONITE_INGOT.get()) || catalyst.is(ModItems.CHARGED_RESONITE_INGOT.get())) {
            this.primaryRgb = 0xB266FF;
            this.accentRgb = 0xFFD166;
        } else if (catalyst.is(ModItems.VISCANITE_INGOT.get()) || catalyst.is(ModItems.CHARGED_VISCANITE_INGOT.get())) {
            this.primaryRgb = 0x38F2B8;
            this.accentRgb = 0x62E8FF;
        } else {
            this.primaryRgb = 0x4CE8FF;
            this.accentRgb = 0xFFD45C;
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, RunicAnvilBlockEntity be) {
        if (!be.isPrimaryHalf()) {
            return;
        }
        if (be.forgeTicks <= 0) {
            return;
        }

        if (level.isClientSide()) {
            if (be.forgeTicks < TOTAL_FORGE_TICKS) {
                be.forgeTicks++;
            }
            return;
        }

        ServerLevel serverLevel = (ServerLevel) level;
        Vec3 center = be.getAnvilCenterWorldPos();
        EssenceType visEssence = be.getVisualEssence();

        // Ambient swirling runes as the Magic Circle forms between the two parts
        if (be.forgeTicks < SMASH_IMPACT_TICK && be.forgeTicks % 4 == 0) {
            SpellParticleHelper.spawnZonePerimeterParticles(
                    serverLevel,
                    center.add(0.0D, 0.28D, 0.0D),
                    0.55D,
                    visEssence,
                    be.forgeTicks * 7
            );
            if (be.forgeTicks == 28) {
                // Ethereal Hammer materializes & winds up!
                serverLevel.playSound(null, BlockPos.containing(center), SoundEvents.TRIDENT_RETURN, SoundSource.BLOCKS, 0.9F, 0.75F);
            }
        }

        // Exact frame of Ethereal Hammer smash!
        if (be.forgeTicks == SMASH_IMPACT_TICK) {
            be.completeForgeFusion(serverLevel, center, visEssence);
        }

        be.forgeTicks++;
        if (be.forgeTicks > TOTAL_FORGE_TICKS) {
            be.forgeTicks = 0;
            be.markUpdated();
        }
    }

    private void completeForgeFusion(ServerLevel serverLevel, Vec3 center, EssenceType materiaEssence) {
        ItemStack base = this.inventory.getItem(SLOT_BASE);
        ItemStack catalyst = this.inventory.getItem(SLOT_CATALYST);
        if (validatePair(base, catalyst) != null) {
            this.forgeTicks = 0;
            markUpdated();
            return;
        }

        ItemStack forged = base.copy();
        forged.setCount(1);
        ItemStack displacedGem = ItemStack.EMPTY;

        boolean isDedicated = forged.getItem() instanceof CastingApparatusItem;
        ApparatusType targetType = isDedicated
                ? ((CastingApparatusItem) forged.getItem()).getApparatusType()
                : ApparatusType.ENGRAVED_TOOL;

        CastingApparatusState curState = forged.get(ModDataComponents.CASTING_APPARATUS_STATE.get());
        if (curState == null) {
            curState = CastingApparatusState.emptyFor(targetType);
        }

        float ingotBonus = RunicAnvilBlock.getTemperingAlloyBonus(catalyst);
        if (ingotBonus > 0.0F) {
            forged.set(ModDataComponents.CASTING_APPARATUS_STATE.get(), curState.withMaterialEfficiencyBonus(ingotBonus));
        } else if (SpellgemItem.isInscribed(catalyst)) {
            SpellAssembly newSpell = catalyst.get(ModDataComponents.SPELL_ASSEMBLY.get());
            if (newSpell != null) {
                int activeIdx = curState.activeSlotIndex();
                SocketedSpellgem prevSocket = curState.getActiveSocket();
                if (!prevSocket.isEmpty() && prevSocket.spell().isPresent()) {
                    displacedGem = SpellgemItem.createInscribedGem(prevSocket.spell().get());
                }
                CastingApparatusState nextState = curState.withSocket(activeIdx, SocketedSpellgem.socketAtWorkstation(newSpell));
                forged.set(ModDataComponents.CASTING_APPARATUS_STATE.get(), nextState);
            }
        }

        this.inventory.removeItemNoUpdate(SLOT_BASE);
        this.inventory.removeItemNoUpdate(SLOT_CATALYST);
        this.inventory.setItem(SLOT_OUTPUT, forged);
        if (!displacedGem.isEmpty()) {
            this.inventory.setItem(SLOT_RETURNED_GEM, displacedGem);
        }

        BlockPos soundPos = BlockPos.containing(center);
        serverLevel.playSound(null, soundPos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0F, 0.95F);
        serverLevel.playSound(null, soundPos, SoundEvents.MACE_SMASH_GROUND_HEAVY, SoundSource.BLOCKS, 0.85F, 1.15F);
        serverLevel.playSound(null, soundPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.2F, 1.3F);

        SpellWorldVfxManager.broadcastRadialNova(serverLevel, center, 1.35F, materiaEssence, null, 3);
        SpellParticleHelper.spawnImpactBurst(serverLevel, center.add(0.0D, 0.12D, 0.0D), materiaEssence, null, 1.35D, 28);
        markUpdated();
    }

    // =========================================================================
    // WORLDLY CONTAINER IMPLEMENTATION
    // =========================================================================

    @Override
    public int[] getSlotsForFace(Direction side) {
        return side == Direction.DOWN ? OUTPUT_SLOTS : INPUT_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        if (this.forgeTicks > 0 || hasCompletedOutput()) {
            return false;
        }
        if (index == SLOT_BASE && this.inventory.getItem(SLOT_BASE).isEmpty()) {
            boolean validBase = stack.getItem() instanceof CastingApparatusItem
                    || RunicAnvilBlock.isEngravableWeaponOrTool(stack);
            if (!validBase) return false;
            ItemStack cat = this.inventory.getItem(SLOT_CATALYST);
            return cat.isEmpty() || validatePair(stack, cat) == null;
        }
        if (index == SLOT_CATALYST && this.inventory.getItem(SLOT_CATALYST).isEmpty()) {
            boolean validCat = SpellgemItem.isInscribed(stack)
                    || RunicAnvilBlock.getTemperingAlloyBonus(stack) > 0.0F;
            if (!validCat) return false;
            ItemStack base = this.inventory.getItem(SLOT_BASE);
            return base.isEmpty() || validatePair(base, stack) == null;
        }
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return this.forgeTicks == 0 && (index == SLOT_OUTPUT || index == SLOT_RETURNED_GEM);
    }

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
        if (this.level != null && !this.level.isClientSide()
                && (slot == SLOT_BASE || slot == SLOT_CATALYST)
                && !this.inventory.getItem(SLOT_BASE).isEmpty()
                && !this.inventory.getItem(SLOT_CATALYST).isEmpty()) {
            tryBeginForging();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player, 8.0F);
    }

    @Override
    public void clearContent() {
        this.inventory.clearContent();
    }

    // =========================================================================
    // PERSISTENCE & CLIENT SYNC
    // =========================================================================

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("ForgeTicks", this.forgeTicks);
        output.putInt("PrimaryRgb", this.primaryRgb);
        output.putInt("AccentRgb", this.accentRgb);
        output.putInt("EssenceOrdinal", this.essenceOrdinal);
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
        this.forgeTicks = input.getIntOr("ForgeTicks", 0);
        this.primaryRgb = input.getIntOr("PrimaryRgb", 0x62E8FF);
        this.accentRgb = input.getIntOr("AccentRgb", 0xFFD45C);
        this.essenceOrdinal = input.getIntOr("EssenceOrdinal", EssenceType.REGULAR.ordinal());
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
