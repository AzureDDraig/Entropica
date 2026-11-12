package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.block.ArcaneScribingTableBlock;
import ddraig.net.entropica.block.DoubleWorkstationPart;
import ddraig.net.entropica.block.RunicAnvilBlock;
import ddraig.net.entropica.item.CastingApparatusItem;
import ddraig.net.entropica.item.SpellParchmentItem;
import ddraig.net.entropica.item.SpellgemItem;
import ddraig.net.entropica.registry.ModBlockEntities;
import ddraig.net.entropica.registry.ModDataComponents;
import ddraig.net.entropica.registry.ModItems;
import ddraig.net.entropica.spell.SpellCoreEffect;
import ddraig.net.entropica.spell.SpellPrefix;
import ddraig.net.entropica.spell.apparatus.ApparatusType;
import ddraig.net.entropica.spell.apparatus.CastingApparatusState;
import ddraig.net.entropica.spell.apparatus.SocketedSpellgem;
import ddraig.net.entropica.spell.assembly.*;
import ddraig.net.entropica.spell.research.SpellResearchManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * BlockEntity backing the 2-block-wide {@link ArcaneScribingTableBlock} (Spellcrafting Guide Sections 3.5, 3.8, 8.2, 8.6).
 *
 * <ul>
 *     <li><b>Slot 0 ({@link #SLOT_PARCHMENT})</b>: Blank Parchment input.</li>
 *     <li><b>Slot 1 ({@link #SLOT_INK})</b>: Scribing Ink input.</li>
 *     <li><b>Slot 2 ({@link #SLOT_STUDY})</b>: Study / Duplicate Spell Parchment, Recycle Inscribed Spellgem, or Align Apparatus.</li>
 *     <li><b>Slot 3 ({@link #SLOT_OUTPUT})</b>: Crafted Spell Parchment, Recycled Blank Spellgem, or Extracted Spellgem.</li>
 * </ul>
 */
public class ArcaneScribingTableBlockEntity extends BlockEntity implements Container {

    public static final int SLOT_PARCHMENT = 0;
    public static final int SLOT_INK = 1;
    public static final int SLOT_STUDY = 2;
    public static final int SLOT_OUTPUT = 3;

    public final SimpleContainer inventory = new SimpleContainer(4) {
        @Override
        public void setChanged() {
            super.setChanged();
            ArcaneScribingTableBlockEntity.this.setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    public ArcaneScribingTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ARCANE_SCRIBING_TABLE_BE.get(), pos, state);
    }

    public boolean isPrimaryHalf() {
        BlockState state = getBlockState();
        return !state.hasProperty(ArcaneScribingTableBlock.PART)
                || state.getValue(ArcaneScribingTableBlock.PART) == DoubleWorkstationPart.LEFT;
    }

    /**
     * Sanitizes a player-submitted {@link SpellAssembly} draft according to Section 3.5 & 8.2:
     * <ul>
     *     <li>Blocks {@link SpellPrefix#UNIQUE} (downgrades to {@link SpellPrefix#COMBAT}).</li>
     *     <li>Allows unresearched ("Cryptic") Flow/Form/Effect nodes for blind experimentation,
     *         while forcing {@code mutationEnabled = false} on any Cryptic node or unresearched mutation!</li>
     *     <li>Validates each Core Effect against {@link SpellCoreEffect#isAllowedForPrefix(SpellPrefix)}.</li>
     * </ul>
     */
    public static SpellAssembly sanitizeDraftForPlayer(Player player, SpellAssembly rawDraft) {
        SpellResearchManager.discoverMateriaFromInventory(player);

        SpellPrefix safePrefix = (rawDraft.prefix() == null || rawDraft.prefix() == SpellPrefix.UNIQUE)
                ? SpellPrefix.COMBAT
                : rawDraft.prefix();
        int safeTier = Math.clamp(rawDraft.potencyTier() > 0 ? rawDraft.potencyTier() : 1, 1, 10);

        FlowPartBinding rf = rawDraft.flowPart();
        String flowNodeId = SpellResearchManager.nodeId(rf.flow());
        boolean flowMutAllowed = rf.mutationEnabled()
                && SpellResearchManager.isNodeUnlocked(player, flowNodeId)
                && SpellResearchManager.isMutationUnlocked(player, flowNodeId, rf.materiaType(), true);
        int safeSubCount = Math.clamp(rf.subSpellCount(), 1, 8);
        FlowPartBinding safeFlow = new FlowPartBinding(
                rf.flow(),
                rawDraft.materiaSpecific() ? rf.materiaType() : EssenceType.REGULAR,
                flowMutAllowed,
                safeSubCount,
                rf.delaySeconds()
        );

        SpellCoreEffect fallbackEffect = switch (safePrefix) {
            case UTILITY -> SpellCoreEffect.BREAK;
            case MISC -> SpellCoreEffect.CONJURE;
            default -> SpellCoreEffect.HARM;
        };

        List<SubSpellSlot> safeSlots = new ArrayList<>();
        List<SubSpellSlot> srcSlots = rawDraft.subSpells();
        int slotTotal = Math.clamp(srcSlots.size(), 1, 8);
        for (int i = 0; i < slotTotal; i++) {
            SubSpellSlot slot = srcSlots.get(i);

            FormPartBinding f = slot.formPart();
            String formNodeId = SpellResearchManager.nodeId(f.form());
            boolean formMutAllowed = f.mutationEnabled()
                    && SpellResearchManager.isNodeUnlocked(player, formNodeId)
                    && SpellResearchManager.isMutationUnlocked(player, formNodeId, f.materiaType(), true);
            FormPartBinding safeForm = new FormPartBinding(
                    f.form(),
                    rawDraft.materiaSpecific() ? f.materiaType() : EssenceType.REGULAR,
                    formMutAllowed,
                    f.subMode()
            );

            EffectPartBinding p = slot.primaryEffect();
            SpellCoreEffect pEff = p.effect().isAllowedForPrefix(safePrefix) ? p.effect() : fallbackEffect;
            String pNodeId = SpellResearchManager.nodeId(pEff);
            boolean pMutAllowed = p.mutationEnabled()
                    && SpellResearchManager.isNodeUnlocked(player, pNodeId)
                    && SpellResearchManager.isMutationUnlocked(player, pNodeId, p.materiaType(), true);
            EffectPartBinding safePrimary = new EffectPartBinding(
                    pEff,
                    rawDraft.materiaSpecific() ? p.materiaType() : EssenceType.REGULAR,
                    pMutAllowed,
                    p.subMode(),
                    p.dialValue()
            );

            Optional<EffectPartBinding> safeSecondary = Optional.empty();
            if (slot.secondaryEffect().isPresent()) {
                EffectPartBinding s = slot.secondaryEffect().get();
                if (s.effect().isAllowedForPrefix(safePrefix)) {
                    String sNodeId = SpellResearchManager.nodeId(s.effect());
                    boolean sMutAllowed = s.mutationEnabled()
                            && SpellResearchManager.isNodeUnlocked(player, sNodeId)
                            && SpellResearchManager.isMutationUnlocked(player, sNodeId, s.materiaType(), true);
                    safeSecondary = Optional.of(new EffectPartBinding(
                            s.effect(),
                            rawDraft.materiaSpecific() ? s.materiaType() : EssenceType.REGULAR,
                            sMutAllowed,
                            s.subMode(),
                            s.dialValue()
                    ));
                }
            }

            safeSlots.add(new SubSpellSlot(safeForm, safePrimary, safeSecondary));
        }

        String safeName = rawDraft.customName() == null ? "Unnamed Incantation" : rawDraft.customName().trim();
        if (safeName.length() > 48) {
            safeName = safeName.substring(0, 48);
        }

        List<String> safeLore = new ArrayList<>();
        for (String line : rawDraft.customLore()) {
            if (line != null && !line.isBlank() && safeLore.size() < 4) {
                safeLore.add(line.length() > 64 ? line.substring(0, 64) : line);
            }
        }

        return new SpellAssembly(
                safeName,
                safeLore,
                rawDraft.materiaSpecific(),
                safePrefix,
                safeTier,
                safeFlow,
                safeSlots,
                SpellFlawProfile.PERFECT,
                0
        );
    }

    /**
     * Action 0: Inscribes a newly designed {@link SpellAssembly} onto a Blank Parchment using Scribing Ink.
     */
    public boolean inscribeBlueprint(Player player, SpellAssembly draft) {
        if (!this.inventory.getItem(SLOT_OUTPUT).isEmpty()) {
            player.displayClientMessage(Component.literal("§c[Arcane Scribing Table] Clear the Output slot first!"), true);
            return false;
        }

        ItemStack parchmentStack = this.inventory.getItem(SLOT_PARCHMENT);
        ItemStack inkStack = this.inventory.getItem(SLOT_INK);
        boolean hasParchment = parchmentStack.is(ModItems.BLANK_PARCHMENT.get());
        boolean hasInk = inkStack.is(ModItems.SCRIBING_INK.get());

        if (!player.isCreative() && (!hasParchment || !hasInk)) {
            player.displayClientMessage(Component.literal(
                    "§c[Arcane Scribing Table] Requires 1x Blank Parchment and 1x Scribing Ink!"
            ), true);
            return false;
        }

        SpellAssembly sanitized = sanitizeDraftForPlayer(player, draft);
        ItemStack result = SpellParchmentItem.createBlueprint(sanitized);

        if (hasParchment && !player.isCreative()) {
            parchmentStack.shrink(1);
            this.inventory.setItem(SLOT_PARCHMENT, parchmentStack.isEmpty() ? ItemStack.EMPTY : parchmentStack);
        }
        if (hasInk && !player.isCreative()) {
            inkStack.shrink(1);
            this.inventory.setItem(SLOT_INK, inkStack.isEmpty() ? ItemStack.EMPTY : inkStack);
        }

        this.inventory.setItem(SLOT_OUTPUT, result);
        playScribingFeedback(1.2F);
        player.displayClientMessage(Component.literal(
                "§a[Arcane Scribing Table] §fInscribed Spell Parchment: §6" + sanitized.customName()
        ), true);
        return true;
    }

    /**
     * Action 1: Duplicates an existing {@link SpellParchmentItem} in Slot 2, strictly gated behind
     * full Codex node & mutation research (Spellcrafting Guide Section 8.2).
     */
    public boolean duplicateParchment(Player player) {
        if (!this.inventory.getItem(SLOT_OUTPUT).isEmpty()) {
            player.displayClientMessage(Component.literal("§c[Arcane Scribing Table] Clear the Output slot first!"), true);
            return false;
        }

        ItemStack studyStack = this.inventory.getItem(SLOT_STUDY);
        if (!studyStack.is(ModItems.SPELL_PARCHMENT.get())) {
            player.displayClientMessage(Component.literal(
                    "§e[Arcane Scribing Table] Place a Spell Parchment in the Study slot to duplicate!"
            ), true);
            return false;
        }

        SpellAssembly blueprint = studyStack.get(ModDataComponents.SPELL_ASSEMBLY.get());
        if (blueprint == null) {
            return false;
        }
        if (blueprint.prefix().isImmutableRelic()) {
            player.displayClientMessage(Component.literal(
                    "§c[Unique Artifact] Unique relic blueprints are sealed and cannot be duplicated!"
            ), true);
            return false;
        }
        if (!SpellResearchManager.canPlayerScribeSpell(player, blueprint)) {
            player.displayClientMessage(Component.literal(
                    "§c[Research Required] Unlock all nodes & mutations on this blueprint at a Research Bench before duplicating!"
            ), true);
            return false;
        }

        ItemStack parchmentStack = this.inventory.getItem(SLOT_PARCHMENT);
        ItemStack inkStack = this.inventory.getItem(SLOT_INK);
        boolean hasParchment = parchmentStack.is(ModItems.BLANK_PARCHMENT.get());
        boolean hasInk = inkStack.is(ModItems.SCRIBING_INK.get());

        if (!player.isCreative() && (!hasParchment || !hasInk)) {
            player.displayClientMessage(Component.literal(
                    "§c[Arcane Scribing Table] Duplication requires 1x Blank Parchment and 1x Scribing Ink!"
            ), true);
            return false;
        }

        if (hasParchment && !player.isCreative()) {
            parchmentStack.shrink(1);
            this.inventory.setItem(SLOT_PARCHMENT, parchmentStack.isEmpty() ? ItemStack.EMPTY : parchmentStack);
        }
        if (hasInk && !player.isCreative()) {
            inkStack.shrink(1);
            this.inventory.setItem(SLOT_INK, inkStack.isEmpty() ? ItemStack.EMPTY : inkStack);
        }

        this.inventory.setItem(SLOT_OUTPUT, SpellParchmentItem.createBlueprint(blueprint));
        playScribingFeedback(1.35F);
        player.displayClientMessage(Component.literal(
                "§a[Arcane Scribing Table] §fDuplicated Spell Parchment: §6" + blueprint.customName()
        ), true);
        return true;
    }

    /**
     * Action 2: Scrubs & recycles an Inscribed {@link SpellgemItem} in Slot 2 back into a
     * {@link ModItems#BLANK_SPELLGEM}, blocking {@link SpellPrefix#UNIQUE} gems and enforcing
     * full Codex research of the gem's parts & mutations (Spellcrafting Guide Sections 3.8, 8.2).
     */
    public boolean recycleSpellgem(Player player) {
        ItemStack studyStack = this.inventory.getItem(SLOT_STUDY);
        if (!SpellgemItem.isInscribed(studyStack)) {
            player.displayClientMessage(Component.literal(
                    "§e[Arcane Scribing Table] Place an Inscribed Spellgem in the Study slot to scrub & recycle!"
            ), true);
            return false;
        }

        SpellAssembly spell = studyStack.get(ModDataComponents.SPELL_ASSEMBLY.get());
        if (spell == null) return false;

        if (spell.prefix().isImmutableRelic()) {
            player.displayClientMessage(Component.literal(
                    "§c[Unique Artifact] Unique boss & dungeon Spellgems are permanently sealed and cannot be recycled!"
            ), true);
            return false;
        }

        if (!SpellResearchManager.canPlayerScribeSpell(player, spell)) {
            player.displayClientMessage(Component.literal(
                    "§c[Research Required] Research all spell nodes & mutations inside this gem before scrubbing it!"
            ), true);
            return false;
        }

        ItemStack curOutput = this.inventory.getItem(SLOT_OUTPUT);
        if (!curOutput.isEmpty() && (!curOutput.is(ModItems.BLANK_SPELLGEM.get()) || curOutput.getCount() >= curOutput.getMaxStackSize())) {
            player.displayClientMessage(Component.literal("§c[Arcane Scribing Table] Clear the Output slot first!"), true);
            return false;
        }

        studyStack.shrink(1);
        this.inventory.setItem(SLOT_STUDY, studyStack.isEmpty() ? ItemStack.EMPTY : studyStack);

        if (curOutput.isEmpty()) {
            this.inventory.setItem(SLOT_OUTPUT, new ItemStack(ModItems.BLANK_SPELLGEM.get(), 1));
        } else {
            curOutput.grow(1);
            this.inventory.setItem(SLOT_OUTPUT, curOutput);
        }

        if (level != null) {
            level.playSound(null, worldPosition, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 0.8F, 1.3F);
            level.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.9F, 1.4F);
        }
        player.displayClientMessage(Component.literal(
                "§a[Spellgem Recycled] §fScrubbed incantation into a pristine Blank Spellgem."
        ), true);
        return true;
    }

    /**
     * Action 3: Safe 0s Lockout / 0% Backfire Spellgem socket or extraction on a Casting Apparatus
     * or Engraved Weapon placed in Slot 2.
     */
    public boolean safeAlignApparatusInSlot(Player player, int socketIndex) {
        ItemStack studyStack = this.inventory.getItem(SLOT_STUDY);
        if (studyStack.isEmpty()) {
            return false;
        }

        boolean isDedicated = studyStack.getItem() instanceof CastingApparatusItem;
        if (!isDedicated && !RunicAnvilBlock.isEngravableWeaponOrTool(studyStack)) {
            return false;
        }

        ApparatusType type = isDedicated
                ? ((CastingApparatusItem) studyStack.getItem()).getApparatusType()
                : ApparatusType.ENGRAVED_TOOL;
        CastingApparatusState curState = studyStack.get(ModDataComponents.CASTING_APPARATUS_STATE.get());
        if (curState == null) {
            curState = CastingApparatusState.emptyFor(type);
        }

        int idx = Math.clamp(socketIndex >= 0 ? socketIndex : curState.activeSlotIndex(), 0, type.getGemCapacity() - 1);
        SocketedSpellgem existing = curState.sockets().get(idx);

        // Check if player holds an Inscribed Spellgem in Off-Hand or Main-Hand to socket
        ItemStack candidateGem = SpellgemItem.isInscribed(player.getOffhandItem())
                ? player.getOffhandItem()
                : (SpellgemItem.isInscribed(player.getMainHandItem()) ? player.getMainHandItem() : ItemStack.EMPTY);

        if (!candidateGem.isEmpty()) {
            SpellAssembly spell = candidateGem.get(ModDataComponents.SPELL_ASSEMBLY.get());
            if (spell == null) return false;

            if (!existing.isEmpty() && existing.spell().isPresent()) {
                ItemStack prevGem = SpellgemItem.createInscribedGem(existing.spell().get());
                if (this.inventory.getItem(SLOT_OUTPUT).isEmpty()) {
                    this.inventory.setItem(SLOT_OUTPUT, prevGem);
                } else if (!player.getInventory().add(prevGem)) {
                    player.drop(prevGem, false);
                }
            }

            if (!player.isCreative()) {
                candidateGem.shrink(1);
            }
            CastingApparatusState updated = curState.withSocket(idx, SocketedSpellgem.socketAtWorkstation(spell));
            studyStack.set(ModDataComponents.CASTING_APPARATUS_STATE.get(), updated);
            this.inventory.setItem(SLOT_STUDY, studyStack);
            playScribingFeedback(1.4F);
            player.displayClientMessage(Component.literal(
                    "§a[Scribing Alignment] §fAligned §b" + spell.customName()
                            + " §fin Slot [" + (idx + 1) + "/" + type.getGemCapacity() + "] (0s Lockout, 0% Backfire)"
            ), true);
            return true;
        }

        if (!existing.isEmpty() && existing.spell().isPresent()) {
            if (!this.inventory.getItem(SLOT_OUTPUT).isEmpty()) {
                player.displayClientMessage(Component.literal("§c[Arcane Scribing Table] Clear the Output slot to extract Spellgem!"), true);
                return false;
            }
            ItemStack extracted = SpellgemItem.createInscribedGem(existing.spell().get());
            CastingApparatusState updated = curState.withSocket(idx, SocketedSpellgem.EMPTY);
            studyStack.set(ModDataComponents.CASTING_APPARATUS_STATE.get(), updated);
            this.inventory.setItem(SLOT_STUDY, studyStack);
            this.inventory.setItem(SLOT_OUTPUT, extracted);
            playScribingFeedback(1.1F);
            player.displayClientMessage(Component.literal(
                    "§e[Scribing Alignment] §fExtracted Spellgem from Slot [" + (idx + 1) + "/" + type.getGemCapacity() + "]."
            ), true);
            return true;
        }

        return false;
    }

    private void playScribingFeedback(float pitch) {
        if (level != null) {
            level.playSound(null, worldPosition, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundSource.BLOCKS, 0.9F, pitch);
            level.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.7F, pitch);
            if (level instanceof ServerLevel srv) {
                srv.sendParticles(ParticleTypes.ENCHANT,
                        worldPosition.getX() + 0.5D, worldPosition.getY() + 1.15D, worldPosition.getZ() + 0.5D,
                        18, 0.45D, 0.25D, 0.45D, 0.12D);
            }
        }
    }

    // --- Container Delegation ---

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
