package ddraig.net.entropica.item;

import ddraig.net.entropica.block.AutocastPedestalBlock;
import ddraig.net.entropica.block.entity.AutocastPedestalBlockEntity;
import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModDataComponents;
import ddraig.net.entropica.spell.SpellTooltipFormatter;
import ddraig.net.entropica.spell.apparatus.ApparatusType;
import ddraig.net.entropica.spell.apparatus.CastingApparatusState;
import ddraig.net.entropica.spell.apparatus.SocketedSpellgem;
import ddraig.net.entropica.spell.assembly.SpellAssembly;
import ddraig.net.entropica.spell.engine.SpellExecutionEngine;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.Consumer;

/**
 * Dedicated Casting Apparatus item ({@code Arcanum Focus = 1}, {@code Wand = 2},
 * {@code Staff = 4}, {@code Grimoire = 9}).
 *
 * <ul>
 *     <li><b>Right-Click in Air/Block</b>: Casts the Spellgem in the active socket.</li>
 *     <li><b>Sneak + Mouse Scroll Wheel</b>: Cycles active Spellgem slot instantly (0s lockout).</li>
 *     <li><b>Right-Click on Arcane Anvil / Drafting Table / Research Bench</b> with a Spellgem in Off-Hand:
 *         Safe Workstation Swap (0s lockout, 0% backfire).</li>
 *     <li><b>Inventory Cursor Right-Click</b> with a Spellgem: Emergency In-Field Swap
 *         (60s stabilization lockout + 45% backfire chance on the first cast after lockout ends).</li>
 * </ul>
 */
public class CastingApparatusItem extends Item {

    private final ApparatusType apparatusType;

    public CastingApparatusItem(Properties properties, ApparatusType apparatusType) {
        super(properties.stacksTo(1)
                .component(ModDataComponents.CASTING_APPARATUS_STATE.get(), CastingApparatusState.emptyFor(apparatusType)));
        this.apparatusType = apparatusType;
    }

    public ApparatusType getApparatusType() {
        return this.apparatusType;
    }

    public static CastingApparatusState getOrCreateState(ItemStack stack, ApparatusType fallbackType) {
        CastingApparatusState existing = stack.get(ModDataComponents.CASTING_APPARATUS_STATE.get());
        if (existing != null) return existing;
        CastingApparatusState created = CastingApparatusState.emptyFor(fallbackType);
        stack.set(ModDataComponents.CASTING_APPARATUS_STATE.get(), created);
        return created;
    }

    public static CastingApparatusState cycleSlot(ItemStack stack, int delta) {
        ApparatusType fallback = stack.getItem() instanceof CastingApparatusItem c
                ? c.getApparatusType()
                : ApparatusType.ARCANUM_FOCUS;
        CastingApparatusState state = getOrCreateState(stack, fallback);
        CastingApparatusState cycled = state.cycleActiveSlot(delta);
        stack.set(ModDataComponents.CASTING_APPARATUS_STATE.get(), cycled);
        return cycled;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (state.is(ModBlocks.RUNIC_ANVIL.get())) {
            return ddraig.net.entropica.block.RunicAnvilBlock.handlePlayerInteraction(level, state, pos, player);
        }
        boolean isSafeWorkstation = state.is(ModBlocks.ARCANE_ANVIL.get())
                || state.is(ModBlocks.ARCANE_SCRIBING_TABLE.get())
                || state.is(ModBlocks.DRAFTING_TABLE.get())
                || state.is(ModBlocks.RESEARCH_BENCH.get())
                || state.is(ModBlocks.ENRICHMENT_TABLE.get());

        if (isSafeWorkstation && player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                performWorkstationSocketOrExtract(level, player, context.getItemInHand());
            }
            return InteractionResult.SUCCESS;
        }

        // Section 2.1.7 & 8.4: Inventory Linking onto Autocast Pedestals
        if (player.isShiftKeyDown()) {
            ItemStack held = context.getItemInHand();
            if (state.getBlock() instanceof AutocastPedestalBlock
                    && level.getBlockEntity(pos) instanceof AutocastPedestalBlockEntity pedestal) {
                CompoundTag tag = held.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
                if (tag.getBoolean("HasPendingLink").orElse(false)) {
                    if (!level.isClientSide()) {
                        BlockPos linkedPos = new BlockPos(
                                tag.getInt("PendingLinkX").orElse(0),
                                tag.getInt("PendingLinkY").orElse(0),
                                tag.getInt("PendingLinkZ").orElse(0)
                        );
                        if (pedestal.addLinkedInventory(linkedPos)) {
                            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F, 1.4F);
                            player.displayClientMessage(Component.literal("§a[Autocast Pedestal] §fLinked container ("
                                    + linkedPos.getX() + ", " + linkedPos.getY() + ", " + linkedPos.getZ()
                                    + ") [" + pedestal.getLinkedInventories().size() + "/"
                                    + AutocastPedestalBlockEntity.MAX_LINKED_INVENTORIES + "]!"), true);
                        } else {
                            player.displayClientMessage(Component.literal("§cContainer is out of range (max 16 blocks from Pedestal)!"), true);
                        }
                    }
                    return InteractionResult.SUCCESS;
                }
            } else if (level.getBlockEntity(pos) instanceof Container
                    && !(level.getBlockEntity(pos) instanceof AutocastPedestalBlockEntity)) {
                if (!level.isClientSide()) {
                    CompoundTag tag = held.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
                    tag.putBoolean("HasPendingLink", true);
                    tag.putInt("PendingLinkX", pos.getX());
                    tag.putInt("PendingLinkY", pos.getY());
                    tag.putInt("PendingLinkZ", pos.getZ());
                    held.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                    level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.7F, 1.5F);
                    player.displayClientMessage(Component.literal("§b[Inventory Link] §fMarked container ("
                            + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()
                            + ") — Sneak + Right-Click an Autocast Pedestal to bind!"), true);
                }
                return InteractionResult.SUCCESS;
            }
        }

        return super.useOn(context);
    }

    private void performWorkstationSocketOrExtract(Level level, Player player, ItemStack apparatusStack) {
        CastingApparatusState state = getOrCreateState(apparatusStack, this.apparatusType);
        int activeIdx = state.activeSlotIndex();
        SocketedSpellgem currentSocket = state.getActiveSocket();
        ItemStack offhand = player.getOffhandItem();

        if (SpellgemItem.isInscribed(offhand)) {
            SpellAssembly newSpell = offhand.get(ModDataComponents.SPELL_ASSEMBLY.get());
            if (newSpell == null) return;

            if (!currentSocket.isEmpty() && currentSocket.spell().isPresent()) {
                ItemStack returned = SpellgemItem.createInscribedGem(currentSocket.spell().get());
                if (!player.getInventory().add(returned)) {
                    player.drop(returned, false);
                }
            }

            offhand.shrink(1);
            CastingApparatusState updated = state.withSocket(activeIdx, SocketedSpellgem.socketAtWorkstation(newSpell));
            apparatusStack.set(ModDataComponents.CASTING_APPARATUS_STATE.get(), updated);
            level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.35F);
            player.displayClientMessage(Component.literal("§a[Workstation Aligned] §fSocketed §b" + newSpell.customName()
                    + " §fin Slot [" + (activeIdx + 1) + "/" + this.apparatusType.getGemCapacity() + "] (0s Lockout, 0% Backfire)"), true);
        } else if (!currentSocket.isEmpty() && currentSocket.spell().isPresent()) {
            ItemStack extracted = SpellgemItem.createInscribedGem(currentSocket.spell().get());
            CastingApparatusState updated = state.withSocket(activeIdx, SocketedSpellgem.EMPTY);
            apparatusStack.set(ModDataComponents.CASTING_APPARATUS_STATE.get(), updated);
            if (!player.getInventory().add(extracted)) {
                player.drop(extracted, false);
            }
            level.playSound(null, player.blockPosition(), SoundEvents.ARMOR_EQUIP_GOLD.value(), SoundSource.PLAYERS, 0.85F, 1.2F);
            player.displayClientMessage(Component.literal("§eExtracted Spellgem from Slot ["
                    + (activeIdx + 1) + "/" + this.apparatusType.getGemCapacity() + "]."), true);
        } else {
            player.displayClientMessage(Component.literal("§7Hold an Inscribed Spellgem in your Off-Hand and Sneak + Right-Click workstation to socket safely!"), true);
        }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            boolean cast = SpellExecutionEngine.castFromApparatus(serverLevel, player, hand, stack);
            return cast ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        }
        return InteractionResult.SUCCESS;
    }

    // --- Inventory Emergency In-Field Swap (60s Lockout + 45% First-Cast Backfire) ---

    @Override
    public boolean overrideStackedOnOther(ItemStack apparatusStack, Slot slot, ClickAction action, Player player) {
        if (action != ClickAction.SECONDARY) return false;

        CastingApparatusState state = getOrCreateState(apparatusStack, this.apparatusType);
        int activeIdx = state.activeSlotIndex();
        SocketedSpellgem activeSocket = state.getActiveSocket();
        ItemStack slotStack = slot.getItem();

        if (slotStack.isEmpty()) {
            if (!activeSocket.isEmpty() && activeSocket.spell().isPresent()) {
                slot.set(SpellgemItem.createInscribedGem(activeSocket.spell().get()));
                apparatusStack.set(ModDataComponents.CASTING_APPARATUS_STATE.get(), state.withSocket(activeIdx, SocketedSpellgem.EMPTY));
                player.playSound(SoundEvents.ARMOR_EQUIP_IRON.value(), 0.8F, 1.2F);
                return true;
            }
        } else if (SpellgemItem.isInscribed(slotStack) && activeSocket.isEmpty()) {
            SpellAssembly spell = slotStack.get(ModDataComponents.SPELL_ASSEMBLY.get());
            if (spell != null) {
                long now = player.level().getGameTime();
                SocketedSpellgem fieldSocketed = SocketedSpellgem.socketInField(spell, now);
                apparatusStack.set(ModDataComponents.CASTING_APPARATUS_STATE.get(), state.withSocket(activeIdx, fieldSocketed));
                slotStack.shrink(1);
                player.playSound(SoundEvents.RESPAWN_ANCHOR_CHARGE, 0.7F, 0.85F);
                if (!player.level().isClientSide()) {
                    player.displayClientMessage(Component.literal(
                            "§e[Field Swap Warning] §7Socketed §b" + spell.customName()
                                    + " §7in field: §c60s Lockout + 45% First-Cast Backfire risk!"
                    ), true);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack apparatusStack, ItemStack otherStack, Slot slot,
                                            ClickAction action, Player player, SlotAccess access) {
        if (action != ClickAction.SECONDARY || !slot.allowModification(player)) return false;

        CastingApparatusState state = getOrCreateState(apparatusStack, this.apparatusType);
        int activeIdx = state.activeSlotIndex();
        SocketedSpellgem activeSocket = state.getActiveSocket();

        if (otherStack.isEmpty()) {
            if (!activeSocket.isEmpty() && activeSocket.spell().isPresent()) {
                access.set(SpellgemItem.createInscribedGem(activeSocket.spell().get()));
                apparatusStack.set(ModDataComponents.CASTING_APPARATUS_STATE.get(), state.withSocket(activeIdx, SocketedSpellgem.EMPTY));
                player.playSound(SoundEvents.ARMOR_EQUIP_IRON.value(), 0.8F, 1.2F);
                return true;
            }
        } else if (SpellgemItem.isInscribed(otherStack) && activeSocket.isEmpty()) {
            SpellAssembly spell = otherStack.get(ModDataComponents.SPELL_ASSEMBLY.get());
            if (spell != null) {
                long now = player.level().getGameTime();
                SocketedSpellgem fieldSocketed = SocketedSpellgem.socketInField(spell, now);
                apparatusStack.set(ModDataComponents.CASTING_APPARATUS_STATE.get(), state.withSocket(activeIdx, fieldSocketed));
                otherStack.shrink(1);
                player.playSound(SoundEvents.RESPAWN_ANCHOR_CHARGE, 0.7F, 0.85F);
                if (!player.level().isClientSide()) {
                    player.displayClientMessage(Component.literal(
                            "§e[Field Swap Warning] §7Socketed §b" + spell.customName()
                                    + " §7in field: §c60s Lockout + 45% First-Cast Backfire risk!"
                    ), true);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);

        CastingApparatusState state = getOrCreateState(stack, this.apparatusType);
        int cap = this.apparatusType.getGemCapacity();
        int active = state.activeSlotIndex();

        tooltipComponents.accept(Component.literal("Spellgem Capacity: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(state.countSocketedGems() + " / " + cap).withStyle(ChatFormatting.AQUA))
                .append(Component.literal("  (Active Slot: #" + (active + 1) + ")").withStyle(ChatFormatting.GOLD)));

        List<SocketedSpellgem> sockets = state.sockets();
        for (int i = 0; i < cap; i++) {
            SocketedSpellgem s = sockets.get(i);
            String marker = (i == active) ? "▶ " : "  ";
            ChatFormatting idxColor = (i == active) ? ChatFormatting.GOLD : ChatFormatting.DARK_GRAY;
            if (s.isEmpty() || s.spell().isEmpty()) {
                tooltipComponents.accept(Component.literal(marker + "[" + (i + 1) + "] ")
                        .withStyle(idxColor)
                        .append(Component.literal("(Empty Socket)").withStyle(ChatFormatting.DARK_GRAY)));
            } else {
                SpellAssembly sp = s.spell().get();
                String warn = s.pendingFieldBackfireRoll() ? " §c[Field-Unaligned 45%]" : "";
                tooltipComponents.accept(Component.literal(marker + "[" + (i + 1) + "] ")
                        .withStyle(idxColor)
                        .append(Component.literal(sp.customName() + " (T" + sp.getEffectiveTier() + ")" + warn)
                                .withStyle(i == active ? ChatFormatting.AQUA : ChatFormatting.GRAY)));
            }
        }

        SocketedSpellgem activeSocket = state.getActiveSocket();
        if (!activeSocket.isEmpty() && activeSocket.spell().isPresent()) {
            tooltipComponents.accept(Component.empty());
            SpellTooltipFormatter.appendSpellAssemblyTooltip(
                    activeSocket.spell().get(),
                    this.apparatusType,
                    state.materialEfficiencyBonus(),
                    true,
                    tooltipComponents
            );
        }

        if (cap > 1) {
            tooltipComponents.accept(Component.literal("Sneak + Scroll Wheel to cycle active Spellgem slot.")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
        tooltipComponents.accept(Component.literal("Sneak + Right-Click Arcane Anvil / Drafting Table for safe 0s swap.")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }
}
