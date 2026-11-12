package ddraig.net.entropica.client.gui;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.inventory.ArcaneScribingTableMenu;
import ddraig.net.entropica.item.SpellParchmentItem;
import ddraig.net.entropica.item.SpellgemItem;
import ddraig.net.entropica.network.ArcaneScribingActionPayload;
import ddraig.net.entropica.registry.ModDataComponents;
import ddraig.net.entropica.spell.SpellCoreEffect;
import ddraig.net.entropica.spell.SpellDeliveryForm;
import ddraig.net.entropica.spell.SpellFlowModifier;
import ddraig.net.entropica.spell.SpellPartSlotType;
import ddraig.net.entropica.spell.SpellPrefix;
import ddraig.net.entropica.spell.apparatus.CastingApparatusState;
import ddraig.net.entropica.spell.apparatus.SocketedSpellgem;
import ddraig.net.entropica.spell.assembly.*;
import ddraig.net.entropica.spell.fuel.MateriaStageTable;
import ddraig.net.entropica.spell.fuel.SpellCostBreakdown;
import ddraig.net.entropica.spell.fuel.SpellFuelCalculator;
import ddraig.net.entropica.spell.mutation.SpellMutationProfile;
import ddraig.net.entropica.spell.mutation.SpellMutationRegistry;
import ddraig.net.entropica.spell.research.SpellResearchManager;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Interactive top-down Arcane Scribing Table GUI featuring:
 * <ul>
 *     <li>Pull-out <b>Left Drawer</b> (drag or click the brass side handle) with Discipline Wax Seals,
 *         Sub-Spell Roman Tabs ({@code I-VIII}), colored Materia Vials & Crystal Shards, and a physical
 *         Brass-and-Crystal {@code MUTATION: OFF / ON} lever switch.</li>
 *     <li>Central unrolled parchment scroll with <b>Concentric Rotating Symbol Rings</b> (drag tangentially
 *         or scroll the mouse wheel to spin symbols smoothly beneath the 12 o'clock Golden Alignment Needle).</li>
 *     <li>Rich gilded <b>Spell Decision Tooltips</b> when hovering any symbol on the rings or controls.</li>
 *     <li>Pull-out <b>Right Supply Drawer</b> with Blank Parchment, Scribing Ink, Study/Apparatus slots,
 *         glowing Output Scroll Cradle, and an illuminated Golden {@code SCRIBE} Wax Seal ceremony.</li>
 * </ul>
 */
public class ArcaneScribingTableScreen extends AbstractContainerScreen<ArcaneScribingTableMenu> {

    private static final SpellPrefix[] CRAFTABLE_PREFIXES = {
            SpellPrefix.COMBAT, SpellPrefix.UTILITY, SpellPrefix.MISC
    };
    private static final String[] ROMAN = {
            "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"
    };

    // Wheel geometry relative to (leftPos, topPos)
    private static final int WHEEL_REL_CX = 158;
    private static final int WHEEL_REL_CY = 90;
    private static final float RING_FLOW_IN = 45.0F;
    private static final float RING_FLOW_OUT = 56.5F;
    private static final float RING_FORM_IN = 32.0F;
    private static final float RING_FORM_OUT = 43.5F;
    private static final float RING_PRIM_IN = 19.0F;
    private static final float RING_PRIM_OUT = 30.5F;
    private static final float RING_DUAL_OUT = 17.5F;

    private static final class MutableSubSlot {
        SpellDeliveryForm form = SpellDeliveryForm.PROJECTILE;
        EssenceType formMateria = EssenceType.REGULAR;
        boolean formMutation = false;

        SpellCoreEffect primaryEffect = SpellCoreEffect.HARM;
        EssenceType primaryMateria = EssenceType.REGULAR;
        boolean primaryMutation = false;

        @Nullable
        SpellCoreEffect secondaryEffect = null;
        EssenceType secondaryMateria = EssenceType.REGULAR;
        boolean secondaryMutation = false;

        SubSpellSlot toImmutable() {
            FormPartBinding formPart = FormPartBinding.of(form, formMateria, formMutation);
            EffectPartBinding primPart = EffectPartBinding.of(primaryEffect, primaryMateria, primaryMutation);
            if (secondaryEffect != null) {
                EffectPartBinding secPart = EffectPartBinding.of(secondaryEffect, secondaryMateria, secondaryMutation);
                return SubSpellSlot.ofDualVerb(formPart, primPart, secPart);
            }
            return SubSpellSlot.of(formPart, primPart);
        }

        static MutableSubSlot fromImmutable(SubSpellSlot slot) {
            MutableSubSlot m = new MutableSubSlot();
            m.form = slot.formPart().form();
            m.formMateria = slot.formPart().materiaType();
            m.formMutation = slot.formPart().mutationEnabled();
            m.primaryEffect = slot.primaryEffect().effect();
            m.primaryMateria = slot.primaryEffect().materiaType();
            m.primaryMutation = slot.primaryEffect().mutationEnabled();
            if (slot.secondaryEffect().isPresent()) {
                EffectPartBinding sec = slot.secondaryEffect().get();
                m.secondaryEffect = sec.effect();
                m.secondaryMateria = sec.materiaType();
                m.secondaryMutation = sec.mutationEnabled();
            }
            return m;
        }
    }

    // Spell Draft State
    private String customName = "Arcane Bolt";
    private String customLoreLine = "";
    private SpellPrefix prefix = SpellPrefix.COMBAT;
    private boolean materiaSpecific = false;
    private int potencyTier = 1;

    private SpellFlowModifier flow = SpellFlowModifier.SINGLE;
    private EssenceType flowMateria = EssenceType.REGULAR;
    private boolean flowMutation = false;

    private final List<MutableSubSlot> draftSlots = new ArrayList<>();
    private int activeSubSlot = 0;
    private int alignSocketIndex = 0;

    // 0 = Outer Flow Ring, 1 = Middle Form Ring, 2 = Inner Primary Ring, 3 = Center Dual-Verb Medallion
    private int focusedRing = 1;

    private List<EssenceType> availableMateria = List.of(EssenceType.REGULAR);
    private int materiaPage = 0;
    private static final int MATERIA_PER_PAGE = 6;

    // Smooth Side Drawer Animation & Drag State
    private float leftDrawerOpen = 0.0F;
    private float leftDrawerTarget = 0.0F;
    private boolean draggingLeftDrawer = false;
    private double dragStartMouseX = 0.0D;
    private float dragStartDrawerOpen = 0.0F;
    private boolean dragMovedSignificantly = false;

    private float rightDrawerOpen = 0.0F;
    private float rightDrawerTarget = 0.0F;
    private boolean draggingRightDrawer = false;
    private boolean hadWorkstationItemsLastFrame = false;

    // Smooth Concentric Ring Rotation State
    private final float[] ringAngleOffset = new float[4];
    private int draggingRing = -1;
    private float lastRingDragMouseAngle = 0.0F;
    private double ringClickStartX = 0.0D;
    private double ringClickStartY = 0.0D;

    // State 4 Scribe Ceremony Flash & Title Editor Popup
    private int scribeCeremonyTicks = 0;
    private boolean editingTitlePopup = false;
    private EditBox nameBox;
    private EditBox loreBox;

    // Hover Tooltip Lines Built Each Frame
    private List<Component> pendingTooltip = null;

    public ArcaneScribingTableScreen(ArcaneScribingTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = ArcaneScribingTableMenu.IMAGE_WIDTH;
        this.imageHeight = ArcaneScribingTableMenu.IMAGE_HEIGHT;
        for (int i = 0; i < 8; i++) {
            this.draftSlots.add(new MutableSubSlot());
        }
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        rebuildAvailableMateriaList();
        normalizeDraftForPrefixAndResearch();

        if (this.menu.hasAnyWorkstationItem()) {
            this.rightDrawerOpen = 1.0F;
            this.rightDrawerTarget = 1.0F;
            this.hadWorkstationItemsLastFrame = true;
        }
        this.menu.setSupplyDrawerExposed(this.rightDrawerOpen > 0.72F);

        int popupX = this.leftPos + 92;
        int popupY = this.topPos + 46;
        this.nameBox = new EditBox(this.font, popupX + 8, popupY + 16, 116, 14, Component.literal("Spell Name"));
        this.nameBox.setMaxLength(40);
        this.nameBox.setValue(this.customName);
        this.nameBox.setHint(Component.literal("Spell Name..."));
        this.nameBox.setResponder(val -> this.customName = val);
        this.nameBox.visible = this.editingTitlePopup;
        this.addRenderableWidget(this.nameBox);

        this.loreBox = new EditBox(this.font, popupX + 8, popupY + 42, 116, 14, Component.literal("Incantation Lore"));
        this.loreBox.setMaxLength(64);
        this.loreBox.setValue(this.customLoreLine);
        this.loreBox.setHint(Component.literal("Optional lore line..."));
        this.loreBox.setResponder(val -> this.customLoreLine = val);
        this.loreBox.visible = this.editingTitlePopup;
        this.addRenderableWidget(this.loreBox);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (this.scribeCeremonyTicks > 0) {
            this.scribeCeremonyTicks--;
        }
        boolean hasItemsNow = this.menu.hasAnyWorkstationItem();
        if (hasItemsNow && !this.hadWorkstationItemsLastFrame && this.rightDrawerTarget < 0.5F) {
            this.rightDrawerTarget = 1.0F;
            playWoodenSlideSound(1.15F);
        }
        this.hadWorkstationItemsLastFrame = hasItemsNow;
    }

    private void setEditingTitlePopup(boolean open) {
        this.editingTitlePopup = open;
        if (this.nameBox != null) {
            this.nameBox.visible = open;
            this.nameBox.setFocused(open);
        }
        if (this.loreBox != null) {
            this.loreBox.visible = open;
            if (!open) this.loreBox.setFocused(false);
        }
    }

    private void rebuildAvailableMateriaList() {
        List<EssenceType> list = new ArrayList<>();
        list.add(EssenceType.REGULAR);
        Player player = Minecraft.getInstance().player;
        for (EssenceType t : EssenceType.values()) {
            if (t == EssenceType.REGULAR) continue;
            if (player == null || SpellResearchManager.isMateriaKnown(player, t)) {
                list.add(t);
            }
        }
        this.availableMateria = list;
        int maxPage = Math.max(0, (this.availableMateria.size() - 1) / MATERIA_PER_PAGE);
        this.materiaPage = Mth.clamp(this.materiaPage, 0, maxPage);
    }

    private MutableSubSlot currentSub() {
        int count = getActiveSlotCount();
        this.activeSubSlot = Math.clamp(this.activeSubSlot, 0, count - 1);
        return this.draftSlots.get(this.activeSubSlot);
    }

    private int getActiveSlotCount() {
        return Math.clamp(this.flow.getDefaultSubSpellCount(), 1, 8);
    }

    private List<SpellCoreEffect> allowedEffects() {
        List<SpellCoreEffect> list = new ArrayList<>();
        for (SpellCoreEffect eff : SpellCoreEffect.values()) {
            if (eff.isAllowedForPrefix(this.prefix)) {
                list.add(eff);
            }
        }
        if (list.isEmpty()) {
            list.add(SpellCoreEffect.CONJURE);
        }
        return list;
    }

    private void cycleRing(int ringIndex, int delta) {
        switch (ringIndex) {
            case 0 -> {
                SpellFlowModifier[] vals = SpellFlowModifier.values();
                int next = Math.floorMod(this.flow.ordinal() + delta, vals.length);
                this.flow = vals[next];
                if (this.activeSubSlot >= getActiveSlotCount()) {
                    this.activeSubSlot = getActiveSlotCount() - 1;
                }
            }
            case 1 -> {
                SpellDeliveryForm[] vals = SpellDeliveryForm.values();
                MutableSubSlot s = currentSub();
                int next = Math.floorMod(s.form.ordinal() + delta, vals.length);
                SpellDeliveryForm chosen = vals[next];
                s.form = chosen;
                if (chosen.isSummonForm()) {
                    for (int i = 0; i < getActiveSlotCount(); i++) {
                        if (this.draftSlots.get(i).form.isSummonForm()) {
                            this.draftSlots.get(i).form = chosen;
                        }
                    }
                }
            }
            case 2 -> {
                List<SpellCoreEffect> allowed = allowedEffects();
                MutableSubSlot s = currentSub();
                int cur = Math.max(0, allowed.indexOf(s.primaryEffect));
                s.primaryEffect = allowed.get(Math.floorMod(cur + delta, allowed.size()));
            }
            case 3 -> {
                List<SpellCoreEffect> allowed = allowedEffects();
                MutableSubSlot s = currentSub();
                int total = allowed.size() + 1;
                int cur = s.secondaryEffect == null ? 0 : (allowed.indexOf(s.secondaryEffect) + 1);
                int next = Math.floorMod(cur + delta, total);
                s.secondaryEffect = next == 0 ? null : allowed.get(next - 1);
            }
        }
        normalizeDraftForPrefixAndResearch();
    }

    private int getRingOptionCount(int ringIndex) {
        return switch (ringIndex) {
            case 0 -> SpellFlowModifier.values().length;
            case 1 -> SpellDeliveryForm.values().length;
            case 2 -> allowedEffects().size();
            default -> allowedEffects().size() + 1;
        };
    }

    private int getRingSelectedIndex(int ringIndex) {
        MutableSubSlot s = currentSub();
        return switch (ringIndex) {
            case 0 -> this.flow.ordinal();
            case 1 -> s.form.ordinal();
            case 2 -> Math.max(0, allowedEffects().indexOf(s.primaryEffect));
            default -> s.secondaryEffect == null ? 0 : (allowedEffects().indexOf(s.secondaryEffect) + 1);
        };
    }

    private void selectRingIndexAnimated(int ringIndex, int targetIdx) {
        int count = getRingOptionCount(ringIndex);
        if (count <= 0) return;
        int cur = getRingSelectedIndex(ringIndex);
        if (cur == targetIdx) return;

        int diff = targetIdx - cur;
        if (diff > count / 2) diff -= count;
        if (diff < -count / 2) diff += count;

        float stepRad = Mth.TWO_PI / count;
        cycleRing(ringIndex, diff);
        this.ringAngleOffset[ringIndex] = diff * stepRad;
        playRingTickSound(1.15F);
    }

    private EssenceType getRingMateria(int ringIndex) {
        MutableSubSlot s = currentSub();
        return switch (ringIndex) {
            case 0 -> this.flowMateria;
            case 1 -> s.formMateria;
            case 2 -> s.primaryMateria;
            default -> s.secondaryMateria;
        };
    }

    private void setRingMateria(int ringIndex, EssenceType type) {
        MutableSubSlot s = currentSub();
        switch (ringIndex) {
            case 0 -> this.flowMateria = type;
            case 1 -> s.formMateria = type;
            case 2 -> s.primaryMateria = type;
            default -> s.secondaryMateria = type;
        }
        if (type != EssenceType.REGULAR) {
            this.materiaSpecific = true;
        }
        normalizeDraftForPrefixAndResearch();
    }

    private boolean getRingMutation(int ringIndex) {
        MutableSubSlot s = currentSub();
        return switch (ringIndex) {
            case 0 -> this.flowMutation;
            case 1 -> s.formMutation;
            case 2 -> s.primaryMutation;
            default -> s.secondaryMutation;
        };
    }

    private boolean canEnableRingMutation(int ringIndex) {
        MutableSubSlot s = currentSub();
        EssenceType mat = getRingMateria(ringIndex);
        if (mat == null || mat == EssenceType.REGULAR) return false;
        String nodeId = switch (ringIndex) {
            case 0 -> SpellResearchManager.nodeId(this.flow);
            case 1 -> SpellResearchManager.nodeId(s.form);
            case 2 -> SpellResearchManager.nodeId(s.primaryEffect);
            default -> s.secondaryEffect == null ? null : SpellResearchManager.nodeId(s.secondaryEffect);
        };
        if (nodeId == null) return false;
        Player player = Minecraft.getInstance().player;
        return player == null || (SpellResearchManager.isNodeUnlocked(player, nodeId)
                && SpellResearchManager.isMutationUnlocked(player, nodeId, mat, true));
    }

    private void toggleRingMutation(int ringIndex) {
        if (!canEnableRingMutation(ringIndex)) {
            playClickSound(0.7F);
            return;
        }
        MutableSubSlot s = currentSub();
        switch (ringIndex) {
            case 0 -> this.flowMutation = !this.flowMutation;
            case 1 -> s.formMutation = !s.formMutation;
            case 2 -> s.primaryMutation = !s.primaryMutation;
            default -> s.secondaryMutation = !s.secondaryMutation;
        }
        this.materiaSpecific = true;
        normalizeDraftForPrefixAndResearch();
        playRingTickSound(1.35F);
    }

    private void normalizeDraftForPrefixAndResearch() {
        Player player = Minecraft.getInstance().player;
        List<SpellCoreEffect> allowed = allowedEffects();

        String flowNode = SpellResearchManager.nodeId(this.flow);
        if (this.flowMateria == EssenceType.REGULAR || (player != null
                && (!SpellResearchManager.isNodeUnlocked(player, flowNode)
                || !SpellResearchManager.isMutationUnlocked(player, flowNode, this.flowMateria, true)))) {
            this.flowMutation = false;
        }

        for (MutableSubSlot s : this.draftSlots) {
            if (!allowed.contains(s.primaryEffect)) {
                s.primaryEffect = allowed.getFirst();
            }
            if (s.secondaryEffect != null && !allowed.contains(s.secondaryEffect)) {
                s.secondaryEffect = null;
            }

            String formNode = SpellResearchManager.nodeId(s.form);
            if (s.formMateria == EssenceType.REGULAR || (player != null
                    && (!SpellResearchManager.isNodeUnlocked(player, formNode)
                    || !SpellResearchManager.isMutationUnlocked(player, formNode, s.formMateria, true)))) {
                s.formMutation = false;
            }

            String primNode = SpellResearchManager.nodeId(s.primaryEffect);
            if (s.primaryMateria == EssenceType.REGULAR || (player != null
                    && (!SpellResearchManager.isNodeUnlocked(player, primNode)
                    || !SpellResearchManager.isMutationUnlocked(player, primNode, s.primaryMateria, true)))) {
                s.primaryMutation = false;
            }

            if (s.secondaryEffect == null || s.secondaryMateria == EssenceType.REGULAR) {
                s.secondaryMutation = false;
            } else {
                String secNode = SpellResearchManager.nodeId(s.secondaryEffect);
                if (player != null && (!SpellResearchManager.isNodeUnlocked(player, secNode)
                        || !SpellResearchManager.isMutationUnlocked(player, secNode, s.secondaryMateria, true))) {
                    s.secondaryMutation = false;
                }
            }
        }
    }

    private boolean isCryptic(String nodeId) {
        Player player = Minecraft.getInstance().player;
        return player != null
                && !SpellResearchManager.isNodeUnlocked(player, nodeId)
                && !SpellResearchManager.isNodeObserved(player, nodeId);
    }

    private boolean isObservedOnly(String nodeId) {
        Player player = Minecraft.getInstance().player;
        return player != null
                && !SpellResearchManager.isNodeUnlocked(player, nodeId)
                && SpellResearchManager.isNodeObserved(player, nodeId);
    }

    private int resolveEssenceRgb(@Nullable EssenceType type, int fallbackRgb) {
        if (type == null || type == EssenceType.REGULAR) {
            return fallbackRgb;
        }
        double gt = Minecraft.getInstance().level != null
                ? Minecraft.getInstance().level.getGameTime()
                : (System.currentTimeMillis() / 50.0D);
        int[] c = type.getCurrentRGB(gt);
        int raw = (c[0] << 16) | (c[1] << 8) | c[2];
        double lum = 0.299 * c[0] + 0.587 * c[1] + 0.114 * c[2];
        if (lum < 85.0D) {
            return ArcaneScribingGlyphRenderer.liftRgb(raw, (int) (95 - lum)) & 0xFFFFFF;
        }
        return raw;
    }

    private void loadFromStudySlot() {
        ItemStack stack = this.menu.getSlot(2).getItem();
        if (stack.isEmpty()) return;

        SpellAssembly found = null;
        if (stack.getItem() instanceof SpellParchmentItem || stack.getItem() instanceof SpellgemItem) {
            found = stack.get(ModDataComponents.SPELL_ASSEMBLY.get());
        } else {
            CastingApparatusState app = stack.get(ModDataComponents.CASTING_APPARATUS_STATE.get());
            if (app != null && !app.sockets().isEmpty()) {
                SocketedSpellgem sock = app.sockets().get(Math.clamp(this.alignSocketIndex, 0, app.sockets().size() - 1));
                if (sock.isEmpty()) {
                    sock = app.getActiveSocket();
                }
                found = sock.spell().orElse(null);
            }
        }
        if (found == null || found.prefix() == SpellPrefix.UNIQUE) {
            playClickSound(0.7F);
            return;
        }

        this.customName = found.customName();
        this.customLoreLine = found.customLore().isEmpty() ? "" : found.customLore().getFirst();
        this.prefix = found.prefix();
        this.materiaSpecific = found.materiaSpecific();
        this.potencyTier = found.getEffectiveTier();
        this.flow = found.flowPart().flow();
        this.flowMateria = found.flowPart().materiaType();
        this.flowMutation = found.flowPart().mutationEnabled();

        List<SubSpellSlot> subs = found.subSpells();
        for (int i = 0; i < 8; i++) {
            if (i < subs.size()) {
                this.draftSlots.set(i, MutableSubSlot.fromImmutable(subs.get(i)));
            } else {
                this.draftSlots.set(i, new MutableSubSlot());
            }
        }
        this.activeSubSlot = 0;
        if (this.nameBox != null) this.nameBox.setValue(this.customName);
        if (this.loreBox != null) this.loreBox.setValue(this.customLoreLine);
        normalizeDraftForPrefixAndResearch();
        playPageTurnSound();
    }

    private SpellAssembly buildDraftAssembly() {
        String safeName = (this.customName == null || this.customName.isBlank()) ? "Unnamed Incantation" : this.customName.trim();
        List<String> lore = (this.customLoreLine == null || this.customLoreLine.isBlank())
                ? List.of()
                : List.of(this.customLoreLine.trim());
        int count = getActiveSlotCount();
        FlowPartBinding flowBinding = new FlowPartBinding(this.flow, this.flowMateria, this.flowMutation, count, 0.0F);
        List<SubSpellSlot> subList = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            subList.add(this.draftSlots.get(i).toImmutable());
        }
        return new SpellAssembly(
                safeName,
                lore,
                this.materiaSpecific,
                this.prefix,
                this.potencyTier,
                flowBinding,
                subList,
                SpellFlawProfile.PERFECT,
                0
        );
    }

    // =========================================================================
    // RENDER PIPELINE
    // =========================================================================

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Custom tabletop & plaques handle all labels in screen coordinates cleanly!
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        updateSmoothAnimations();
        this.menu.setSupplyDrawerExposed(this.rightDrawerOpen > 0.72F);
        this.pendingTooltip = null;

        int x = this.leftPos;
        int y = this.topPos;

        // 1. Under-Desk Satchel & Live Readout Plaques (y = 155..235)
        renderUnderDeskTray(g, x, y, mouseX, mouseY);

        // 2. Main Top-Down Arcane Scribing Tabletop (y = 0..154)
        renderTabletopChassis(g, x, y, mouseX, mouseY);

        // 3. Left Wing: Open Grimoire when tucked, plus Sliding Velvet Left Drawer (State 1 & State 2)
        renderLeftWingAndDrawer(g, x, y, mouseX, mouseY);

        // 4. Right Wing Interior: Velvet Supply Cradles & Golden SCRIBE Wax Seal (State 4)
        renderRightSupplyInterior(g, x, y, mouseX, mouseY);

        // 5. Central Unrolled Parchment Scroll & Concentric Rotating Symbol Rings (State 1, 2, 3, 4)
        renderCentralScrollAndRings(g, x, y, mouseX, mouseY, partialTick);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g, mouseX, mouseY, partialTick);
        super.render(g, mouseX, mouseY, partialTick);

        // Draw sliding wooden/brass/cyan Right Drawer Cover over the Right Bay when closed/sliding (State 1 & 3)
        if (this.rightDrawerOpen < 0.99F) {
            renderRightSlidingCover(g, this.leftPos, this.topPos, mouseX, mouseY);
        } else {
            renderRightOpenHandle(g, this.leftPos, this.topPos, mouseX, mouseY);
        }

        // Title & Lore Editor Parchment Popup (if open)
        if (this.editingTitlePopup) {
            renderTitleEditorBackdrop(g, this.leftPos, this.topPos);
            if (this.nameBox != null) this.nameBox.render(g, mouseX, mouseY, partialTick);
            if (this.loreBox != null) this.loreBox.render(g, mouseX, mouseY, partialTick);
        }

        // Tooltips: Item tooltips take priority when hovering a populated container slot; otherwise Gilded Spell Tooltip!
        if (this.hoveredSlot != null && this.hoveredSlot.hasItem() && !this.editingTitlePopup) {
            this.renderTooltip(g, mouseX, mouseY);
        } else if (this.pendingTooltip != null && !this.editingTitlePopup) {
            ArcaneScribingGlyphRenderer.drawGildedTooltipBox(
                    g, this.font, this.pendingTooltip, mouseX, mouseY, this.width, this.height
            );
        }
    }

    private void updateSmoothAnimations() {
        if (!this.draggingLeftDrawer) {
            this.leftDrawerOpen += (this.leftDrawerTarget - this.leftDrawerOpen) * 0.24F;
            if (Math.abs(this.leftDrawerTarget - this.leftDrawerOpen) < 0.005F) {
                this.leftDrawerOpen = this.leftDrawerTarget;
            }
        }
        if (!this.draggingRightDrawer) {
            this.rightDrawerOpen += (this.rightDrawerTarget - this.rightDrawerOpen) * 0.24F;
            if (Math.abs(this.rightDrawerTarget - this.rightDrawerOpen) < 0.005F) {
                this.rightDrawerOpen = this.rightDrawerTarget;
            }
        }
        for (int r = 0; r < 4; r++) {
            if (this.draggingRing != r) {
                this.ringAngleOffset[r] *= 0.72F;
                if (Math.abs(this.ringAngleOffset[r]) < 0.003F) {
                    this.ringAngleOffset[r] = 0.0F;
                }
            }
        }
    }

    private void renderTabletopChassis(GuiGraphics g, int x, int y, int mouseX, int mouseY) {
        int deskW = this.imageWidth;
        int deskH = 154;

        // Dark walnut wooden tabletop + subtle pixel plank grain
        g.fill(x, y + 6, x + deskW, y + deskH, 0xFF23160F);
        g.fill(x + 2, y + 8, x + deskW - 2, y + deskH - 2, 0xFF2B1B12);
        for (int py = y + 20; py < y + deskH - 8; py += 16) {
            g.fill(x + 6, py, x + deskW - 6, py + 2, 0xFF1E120B);
        }

        // Top & Bottom Cyan Arcanite + Brass Trim Rails
        g.fill(x + 14, y + 6, x + deskW - 14, y + 9, 0xFFD4AF37);
        g.fill(x + 24, y + 7, x + 82, y + 8, 0xFF2CE6FF);
        g.fill(x + 234, y + 7, x + deskW - 24, y + 8, 0xFF2CE6FF);

        g.fill(x + 14, y + deskH - 4, x + deskW - 14, y + deskH - 1, 0xFFD4AF37);
        g.fill(x + 22, y + deskH - 3, x + 82, y + deskH - 2, 0xFF2CE6FF);
        g.fill(x + 234, y + deskH - 3, x + deskW - 22, y + deskH - 2, 0xFF2CE6FF);

        // 4 Ornate Gilded Brass Corner Brackets (matching all 4 concept images!)
        drawCornerBracket(g, x, y + 4, true, true);
        drawCornerBracket(g, x + deskW - 16, y + 4, false, true);
        drawCornerBracket(g, x, y + deskH - 16, true, false);
        drawCornerBracket(g, x + deskW - 16, y + deskH - 16, false, false);

        // Glowing Cyan Arcanite Conduits feeding from left & right bays into the Central Scroll
        g.fill(x + 68, y + 79, x + 86, y + 93, 0xFFD4AF37);
        g.fill(x + 68, y + 81, x + 86, y + 85, 0xFF2CE6FF);
        g.fill(x + 68, y + 87, x + 86, y + 91, 0xFF2CE6FF);

        g.fill(x + 230, y + 79, x + 244, y + 93, 0xFFD4AF37);
        g.fill(x + 230, y + 81, x + 244, y + 85, 0xFF2CE6FF);
        g.fill(x + 230, y + 87, x + 244, y + 91, 0xFF2CE6FF);

        // Top Inkwell & Cryptic Tome Shelf (x = 86..230, y = 0..22)
        g.fill(x + 86, y, x + 230, y + 22, 0xFF1A100A);
        g.fill(x + 86, y + 20, x + 230, y + 23, 0xFFD4AF37);
        g.fill(x + 92, y + 21, x + 224, y + 22, 0xFF2CE6FF);

        int[] defaultBottlePalette = {0x22D3EE, 0xF59E0B, 0xEF4444, 0xA855F7};
        String[] ringNames = {"Outer Flow Ring", "Middle Delivery Form Ring", "Inner Primary Effect Ring", "Center Dual-Verb Core"};
        for (int r = 0; r < 4; r++) {
            int ix = x + 94 + r * 26;
            int iy = y + 1;
            int rgb = resolveEssenceRgb(getRingMateria(r), defaultBottlePalette[r]);
            boolean hovered = isInside(mouseX, mouseY, ix, iy, 16, 19);
            ArcaneScribingGlyphRenderer.drawInkwell(g, ix, iy, rgb, this.focusedRing == r || hovered);
            if (hovered) {
                EssenceType mat = getRingMateria(r);
                this.pendingTooltip = List.of(
                        Component.literal("§6" + ringNames[r] + " Inkwell"),
                        Component.literal("§7Infused Materia: " + (mat == EssenceType.REGULAR ? "§fUnattuned / Dynamic" : mat.getFormattedName())),
                        Component.literal("§8• Click to focus this ring for Materia Vials & Mutation")
                );
            }
        }

        // Top-Right '?' Tome Pedestal Cube (toggles Spell Name & Lore popup!)
        int px = x + 202;
        int py = y + 1;
        boolean pedHover = isInside(mouseX, mouseY, px, py, 20, 20);
        ArcaneScribingGlyphRenderer.drawCrypticPedestal(g, this.font, px, py, this.editingTitlePopup, pedHover);
        if (pedHover) {
            this.pendingTooltip = List.of(
                    Component.literal("§6Incantation Title & Lore Seal"),
                    Component.literal("§7Current Name: §f" + (this.customName.isBlank() ? "Unnamed Incantation" : this.customName)),
                    Component.literal("§8• Click to edit custom Spell Name & Lore inscription")
            );
        }
    }

    private void drawCornerBracket(GuiGraphics g, int bx, int by, boolean left, boolean top) {
        g.fill(bx, by, bx + 16, by + 16, 0xFF8C6B1F);
        g.fill(bx + 1, by + 1, bx + 15, by + 15, 0xFFD4AF37);
        int cutX = left ? bx + 7 : bx;
        int cutY = top ? by + 7 : by;
        g.fill(cutX, cutY, cutX + 9, cutY + 9, 0xFF2B1B12);
    }

    private void renderLeftWingAndDrawer(GuiGraphics g, int x, int y, int mouseX, int mouseY) {
        // 1. Open Pixel Grimoire on the left desk bay when the drawer isn't fully covering it
        if (this.leftDrawerOpen < 0.96F) {
            int gx = x + 8;
            int gy = y + 34;
            boolean grimHover = this.leftDrawerOpen < 0.25F && isInside(mouseX, mouseY, gx, gy, 70, 72);
            ArcaneScribingGlyphRenderer.drawOpenGrimoire(g, gx, gy, grimHover);
            if (grimHover) {
                this.pendingTooltip = List.of(
                        Component.literal("§6Arcane Scribing Grimoire & Side Drawer"),
                        Component.literal("§7Holds Discipline Wax Seals, Sub-Spell Tabs, Materia Vials & Mutation Switch."),
                        Component.literal("§b• Click here or drag the Left Brass Handle to slide open!")
                );
            }
        }

        // 2. Closed Left Brass Handle (visible when drawer is mostly tucked)
        if (this.leftDrawerOpen <= 0.02F) {
            int hx = x - 8;
            int hy = y + 64;
            boolean hHover = isInside(mouseX, mouseY, hx - 2, hy - 2, 12, 44);
            int brass = hHover ? 0xFFFDE047 : 0xFFD4AF37;
            g.fill(hx, hy, hx + 8, hy + 40, 0xFF785917);
            g.fill(hx + 1, hy + 1, hx + 7, hy + 39, brass);
            g.fill(hx + 3, hy + 8, hx + 5, hy + 32, 0xFF2CE6FF);
            if (hHover) {
                this.pendingTooltip = List.of(
                        Component.literal("§6Left Materia & Discipline Drawer"),
                        Component.literal("§7Drag outward or click to open the Velvet Materia Drawer!")
                );
            }
            return;
        }

        // 3. Sliding Purple-Velvet Left Drawer (State 2: open spans x - 38 .. x + 86, width = 124)
        int drawerW = 124;
        int fullLeft = x - 38;
        int slideOffset = Math.round((1.0F - this.leftDrawerOpen) * drawerW);
        int dx = fullLeft + slideOffset;
        int dy = y + 25;
        int dh = 124;

        // Brass Handle attached to the outer left edge of the sliding drawer
        int hx = dx - 9;
        int hy = dy + 39;
        boolean handleHover = isInside(mouseX, mouseY, hx - 2, hy - 2, 11, 46);
        g.fill(hx, hy, hx + 9, hy + 42, 0xFF785917);
        g.fill(hx + 1, hy + 1, hx + 8, hy + 41, handleHover ? 0xFFFDE047 : 0xFFD4AF37);
        g.fill(hx + 2, hy + 6, hx + 5, hy + 36, 0xFF23160F);
        g.fill(hx + 5, hy + 10, hx + 7, hy + 32, 0xFF2CE6FF);
        if (handleHover) {
            this.pendingTooltip = List.of(
                    Component.literal("§6Left Drawer Brass Handle"),
                    Component.literal("§7Drag or click to tuck the drawer back into the desk.")
            );
        }

        // Clip drawer body cleanly at the Central Scroll Frame edge (x + 86) so it slides out from inside!
        g.enableScissor(dx - 2, dy - 3, x + 86, dy + dh + 3);

        // Top & bottom golden brass drawer slide rails (matching State 2)
        g.fill(dx, dy, dx + drawerW, dy + 4, 0xFFD4AF37);
        g.fill(dx, dy + dh - 4, dx + drawerW, dy + dh, 0xFFD4AF37);
        g.fill(dx, dy + 4, dx + drawerW, dy + dh - 4, 0xFF24140D);
        // Royal purple velvet lining
        g.fill(dx + 4, dy + 5, dx + drawerW - 2, dy + dh - 5, 0xFF2B124C);

        // --- Compartment A (dy + 7 .. dy + 47): Discipline Wax Seals + Lock Seal + Sub-Spell Tabs I..VIII ---
        int compAY = dy + 7;
        String[] sealTitles = {"Combat", "Utility", "Misc", this.materiaSpecific ? "Locked" : "Dynamic"};
        int[] sealColors = {0x991B1B, 0x0284C7, 0xD97706, this.materiaSpecific ? 0x7E22CE : 0x475569};
        for (int s = 0; s < 4; s++) {
            int cardX = dx + 6 + s * 28;
            boolean activeSeal = (s < 3) ? (this.prefix == CRAFTABLE_PREFIXES[s]) : this.materiaSpecific;
            boolean hoverSeal = this.leftDrawerOpen > 0.7F && isInside(mouseX, mouseY, cardX, compAY, 26, 28);
            // Parchment seal tag
            g.fill(cardX, compAY, cardX + 26, compAY + 27, activeSeal ? 0xFFF3E5C8 : 0xFFD8C39E);
            g.fill(cardX, compAY + 26, cardX + 26, compAY + 27, 0xFFA68A5B);
            ArcaneScribingGlyphRenderer.drawWaxSeal(g, cardX + 13, compAY + 16, 7, sealColors[s], activeSeal, hoverSeal, s);
            if (hoverSeal) {
                if (s < 3) {
                    SpellPrefix p = CRAFTABLE_PREFIXES[s];
                    this.pendingTooltip = List.of(
                            Component.literal("§6" + p.getDisplayName() + " Discipline Wax Seal"),
                            Component.literal("§7Filters the Inner Ring to " + p.getDisplayName() + " Core Effects."),
                            Component.literal("§8• Click to stamp this spell with the " + p.getDisplayName() + " discipline")
                    );
                } else {
                    this.pendingTooltip = List.of(
                            Component.literal(this.materiaSpecific ? "§dMateria-Locked Seal (-15% Fuel Cost!)" : "§bDynamic Materia Seal"),
                            Component.literal(this.materiaSpecific
                                    ? "§7Locks your chosen Materia colors & Mutations onto the blueprint with a §a15% fuel discount§7!"
                                    : "§7Adapts to whatever Universal Materia Cell is loaded in your Staff or Wand."),
                            Component.literal("§8• Click to toggle Locked (-15% cost) vs Dynamic")
                    );
                }
            }
        }
        // Sub-Spell Roman Parchment Tabs [I .. VIII]
        int tabY = compAY + 28;
        int slotCount = getActiveSlotCount();
        for (int i = 0; i < 8; i++) {
            int tx = dx + 6 + i * 14;
            boolean enabledTab = i < slotCount;
            boolean selectedTab = i == this.activeSubSlot;
            boolean hoverTab = this.leftDrawerOpen > 0.7F && isInside(mouseX, mouseY, tx, tabY, 13, 11);
            int tabBg = selectedTab ? 0xFFFDE68A : (enabledTab ? 0xFFE5D1A7 : 0xFF3B235A);
            g.fill(tx, tabY, tx + 13, tabY + 11, tabBg);
            int txtCol = selectedTab ? 0xFF1E1B4B : (enabledTab ? 0xFF3E2723 : 0xFF7E6A9E);
            g.drawCenteredString(this.font, ROMAN[i], tx + 6, tabY + 2, txtCol);
            if (hoverTab) {
                this.pendingTooltip = List.of(
                        Component.literal("§6Sub-Spell Node " + ROMAN[i] + (enabledTab ? "" : " §8(Inactive for " + this.flow.getDisplayName() + ")")),
                        Component.literal(enabledTab
                                ? "§7Click to configure Form, Effect & Materia for Sub-Spell #" + (i + 1)
                                : "§8Rotate Outer Flow Ring to a multi-spell Flow (Double..Octuple) to unlock more sub-spells!")
                );
            }
        }

        // Velvet shelf divider
        g.fill(dx + 4, dy + 49, dx + drawerW - 2, dy + 51, 0xFF4C1D95);

        // --- Compartment B (dy + 52 .. dy + 89): Ring Focus Pills + Materia Vials & Shards ---
        String[] pillLabels = {"Flow", "Form", "Eff", "+Verb"};
        for (int r = 0; r < 4; r++) {
            int px = dx + 6 + r * 28;
            int py = dy + 53;
            boolean sel = this.focusedRing == r;
            boolean hov = this.leftDrawerOpen > 0.7F && isInside(mouseX, mouseY, px, py, 26, 10);
            g.fill(px, py, px + 26, py + 10, sel ? 0xFFD4AF37 : (hov ? 0xFF5B21B6 : 0xFF1E0B36));
            g.drawCenteredString(this.font, pillLabels[r], px + 13, py + 1, sel ? 0xFF111827 : 0xFFE2E8F0);
        }

        int vialY = dy + 65;
        int startIdx = this.materiaPage * MATERIA_PER_PAGE;
        EssenceType currentRingMat = getRingMateria(this.focusedRing);
        for (int slot = 0; slot < MATERIA_PER_PAGE; slot++) {
            int listIdx = startIdx + slot;
            if (listIdx >= this.availableMateria.size()) break;
            EssenceType mat = this.availableMateria.get(listIdx);
            int vx = dx + 13 + slot * 16;
            boolean hov = this.leftDrawerOpen > 0.7F && isInside(mouseX, mouseY, vx - 1, vialY - 1, 16, 22);
            int rgb = resolveEssenceRgb(mat, 0xCBD5E1);
            boolean shardStyle = listIdx >= 2; // First 2 as classic glass vials, rest as faceted Materia shards (State 2!)
            ArcaneScribingGlyphRenderer.drawMateriaVialOrShard(
                    g, vx, vialY, rgb, shardStyle, currentRingMat == mat, hov, mat == EssenceType.REGULAR
            );
            if (hov) {
                this.pendingTooltip = buildMateriaVialTooltip(mat);
            }
        }
        // Page arrows if > 6 Materia known
        if (this.availableMateria.size() > MATERIA_PER_PAGE) {
            boolean canPrev = this.materiaPage > 0;
            boolean canNext = (this.materiaPage + 1) * MATERIA_PER_PAGE < this.availableMateria.size();
            g.drawString(this.font, "<", dx + 5, vialY + 6, canPrev ? 0xFFFDE047 : 0xFF6B7280, false);
            g.drawString(this.font, ">", dx + drawerW - 11, vialY + 6, canNext ? 0xFFFDE047 : 0xFF6B7280, false);
        }

        // Velvet shelf divider
        g.fill(dx + 4, dy + 88, dx + drawerW - 2, dy + 90, 0xFF4C1D95);

        // --- Compartment C (dy + 92 .. dy + 118): Brass & Crystal Mutation Switch (State 2) ---
        boolean mutOn = getRingMutation(this.focusedRing);
        boolean canMut = canEnableRingMutation(this.focusedRing);
        int mutRgb = resolveEssenceRgb(currentRingMat, 0x38BDF8);
        boolean mutHover = this.leftDrawerOpen > 0.7F && isInside(mouseX, mouseY, dx + 6, dy + 91, drawerW - 12, 27);
        ArcaneScribingGlyphRenderer.drawMutationSwitch(g, this.font, dx + 6, dy + 92, drawerW - 12, mutOn, canMut, mutRgb, mutHover);
        if (mutHover) {
            this.pendingTooltip = buildMutationSwitchTooltip(canMut, mutOn, currentRingMat);
        }

        g.disableScissor();
    }

    private void renderRightSupplyInterior(GuiGraphics g, int x, int y, int mouseX, int mouseY) {
        int rx = x + 236;
        int ry = y + 25;
        int rw = 74;
        int rh = 124;

        // Gilded brass cradle & royal purple velvet recess
        g.fill(rx, ry, rx + rw, ry + rh, 0xFFD4AF37);
        g.fill(rx + 2, ry + 2, rx + rw - 2, ry + rh - 2, 0xFF2B124C);
        g.fill(rx + 35, ry + 4, rx + 39, ry + rh - 4, 0xFF2CE6FF);

        // 4 Workstation Slot Cradles (matching ArcaneScribingTableMenu positions!)
        drawWorkstationSlotCradle(g, x + 245, y + 33, 0, mouseX, mouseY); // Blank Parchment
        drawWorkstationSlotCradle(g, x + 281, y + 33, 1, mouseX, mouseY); // Scribing Ink
        drawWorkstationSlotCradle(g, x + 245, y + 115, 2, mouseX, mouseY); // Study / Apparatus
        drawWorkstationSlotCradle(g, x + 281, y + 115, 3, mouseX, mouseY); // Output Cradle

        // Golden Halo around Output Cradle when an output item is ready or Scribe flash is active (State 4!)
        if (this.menu.getSlot(3).hasItem() || this.scribeCeremonyTicks > 0) {
            ArcaneScribingGlyphRenderer.drawRingBand(g, x + 290, y + 124, 10.5F, 13.0F, 0xAAFFD700);
        }

        // Central Golden Wax Seal "SCRIBE" Button (x + 248..298, y + 54..90)
        int sealCx = x + 273;
        int sealCy = y + 68;
        boolean hasSupplies = this.menu.getSlot(0).hasItem() && this.menu.getSlot(1).hasItem();
        boolean scribeHover = this.rightDrawerOpen > 0.72F && isInside(mouseX, mouseY, x + 248, y + 54, 50, 35);
        g.fill(x + 249, y + 54, x + 297, y + 90, 0xFF1B0D33);
        g.fill(x + 249, y + 54, x + 297, y + 55, 0xFFD4AF37);
        g.fill(x + 249, y + 89, x + 297, y + 90, 0xFFD4AF37);
        ArcaneScribingGlyphRenderer.drawWaxSeal(g, sealCx, sealCy, 11, 0xEAB308, hasSupplies || this.scribeCeremonyTicks > 0, scribeHover, 4);
        g.drawCenteredString(this.font, "SCRIBE", sealCx, y + 80, scribeHover || hasSupplies ? 0xFFFDE047 : 0xFFD4AF37);
        if (scribeHover) {
            SpellAssembly preview = buildDraftAssembly();
            SpellCostBreakdown cost = SpellFuelCalculator.calculateCastCost(preview, null, 0.0F, EssenceType.REGULAR);
            this.pendingTooltip = List.of(
                    Component.literal("§6✦ Inscribe Spell Parchment ✦"),
                    Component.literal("§7Spell: §f" + preview.customName() + " §8(Tier " + ROMAN[this.potencyTier - 1] + " " + this.prefix.getDisplayName() + ")"),
                    Component.literal("§7Requires: §f1 Blank Parchment §7+ §b1 Arcane Scribing Ink"),
                    Component.literal("§7Cast Fuel: §e" + cost.formatTypeCostSummary()),
                    Component.literal(hasSupplies ? "§a• Click the Golden Wax Seal to inscribe!" : "§c• Load Blank Parchment & Scribing Ink above first")
            );
        }

        // Compact Gilded Utility Strip (y + 94..108): [Load] [Copy] [Scrub / S#]
        ItemStack studyStack = this.menu.getSlot(2).getItem();
        boolean isInscribedGem = studyStack.getItem() instanceof SpellgemItem && SpellgemItem.isInscribed(studyStack);
        String[] miniBtns = {"Load", "Copy", isInscribedGem ? "Scrub" : ("S#" + (this.alignSocketIndex + 1))};
        for (int b = 0; b < 3; b++) {
            int bx = x + 239 + b * 23;
            int by = y + 95;
            boolean hov = this.rightDrawerOpen > 0.72F && isInside(mouseX, mouseY, bx, by, 22, 13);
            g.fill(bx, by, bx + 22, by + 13, hov ? 0xFFFDE047 : 0xFF9E7B23);
            g.fill(bx + 1, by + 1, bx + 21, by + 12, hov ? 0xFF3B1B68 : 0xFF1E0E38);
            g.drawCenteredString(this.font, miniBtns[b], bx + 11, by + 3, hov ? 0xFFFDE047 : 0xFFE2E8F0);
            if (hov) {
                this.pendingTooltip = switch (b) {
                    case 0 -> List.of(
                            Component.literal("§6Load Spell Blueprint"),
                            Component.literal("§7Reads the Spell Parchment, Spellgem, or Apparatus in the bottom-left slot into the rings.")
                    );
                    case 1 -> List.of(
                            Component.literal("§6Duplicate Researched Parchment"),
                            Component.literal("§7Copies a researched Spell Parchment in the bottom-left slot using 1 Blank Parchment + 1 Ink.")
                    );
                    default -> List.of(
                            Component.literal(isInscribedGem ? "§bScrub Inscribed Spellgem" : "§aSafe Apparatus Socket Align / Extract"),
                            Component.literal(isInscribedGem
                                    ? "§7Cleans a non-Unique researched Spellgem back into a Blank Spellgem."
                                    : "§7Left-click: Safely extract/align Socket #" + (this.alignSocketIndex + 1) + " • Right-click: Cycle Socket #1-#9!")
                    );
                };
            }
        }
    }

    private void drawWorkstationSlotCradle(GuiGraphics g, int sx, int sy, int idx, int mouseX, int mouseY) {
        g.fill(sx, sy, sx + 18, sy + 18, idx == 3 ? 0xFFFDE047 : 0xFFD4AF37);
        g.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF160B28);
        if (!this.menu.getSlot(idx).hasItem()) {
            // Subtle ghost silhouettes so players immediately know what goes where!
            switch (idx) {
                case 0 -> { // Blank Parchment silhouette
                    g.fill(sx + 5, sy + 4, sx + 13, sy + 14, 0x44E6D2A8);
                    g.fill(sx + 4, sy + 4, sx + 14, sy + 6, 0x55D4AF37);
                }
                case 1 -> { // Ink bottle silhouette
                    g.fill(sx + 7, sy + 3, sx + 11, sy + 6, 0x55D4AF37);
                    g.fill(sx + 5, sy + 6, sx + 13, sy + 14, 0x4438BDF8);
                }
                case 2 -> g.drawCenteredString(this.font, "✦", sx + 9, sy + 5, 0x44A855F7);
                case 3 -> g.drawCenteredString(this.font, "◈", sx + 9, sy + 5, 0x55FDE047);
            }
            if (this.rightDrawerOpen > 0.72F && isInside(mouseX, mouseY, sx, sy, 18, 18)) {
                this.pendingTooltip = switch (idx) {
                    case 0 -> List.of(Component.literal("§6Blank Parchment Slot"), Component.literal("§7Place §fBlank Parchment §7here to inscribe or duplicate spells."));
                    case 1 -> List.of(Component.literal("§bArcane Scribing Ink Slot"), Component.literal("§7Place §bArcane Scribing Ink §7here to fuel your quill."));
                    case 2 -> List.of(Component.literal("§dStudy / Copy / Scrub / Apparatus Slot"), Component.literal("§7Insert a Spell Parchment, Inscribed Spellgem, or Casting Apparatus."));
                    default -> List.of(Component.literal("§eFinished Output Cradle"), Component.literal("§7Completed Spell Parchments and extracted Spellgems appear here."));
                };
            }
        }
    }

    private void renderRightSlidingCover(GuiGraphics g, int x, int y, int mouseX, int mouseY) {
        int slide = Math.round(this.rightDrawerOpen * 78.0F);
        int cx = x + 236 + slide;
        int cy = y + 25;
        int cw = 74;
        int ch = 124;

        g.enableScissor(x + 236, cy - 2, x + 356, cy + ch + 2);
        // Dark walnut & gilded brass lid with glowing cyan Arcanite emblem (matching State 1 & State 3!)
        g.fill(cx, cy, cx + cw, cy + ch, 0xFF23160F);
        g.fill(cx + 4, cy + 4, cx + cw - 4, cy + ch - 4, 0xFF2B124C);
        g.fill(cx + 4, cy + 4, cx + 8, cy + ch - 4, 0xFFD4AF37);
        g.fill(cx + cw - 8, cy + 4, cx + cw - 4, cy + ch - 4, 0xFFD4AF37);
        g.fill(cx + 4, cy + 42, cx + 8, cy + 82, 0xFF2CE6FF);
        g.fill(cx + cw - 8, cy + 42, cx + cw - 4, cy + 82, 0xFF2CE6FF);

        // Center cyan & gold emblem plaque
        int ex = cx + 18;
        int ey = cy + 38;
        g.fill(ex, ey, ex + 38, ey + 48, 0xFFD4AF37);
        g.fill(ex + 2, ey + 2, ex + 36, ey + 46, 0xFF241438);
        g.fill(ex + 16, ey + 10, ex + 22, ey + 38, 0xFFD4AF37);
        g.fill(ex + 15, ey + 16, ex + 23, ey + 32, 0xFF2CE6FF);
        g.disableScissor();

        // Right Brass Pull-Handle
        int hx = Math.min(x + 346, x + 314 + Math.round(this.rightDrawerOpen * 32.0F));
        int hy = y + 64;
        boolean hov = isInside(mouseX, mouseY, x + 236, cy, 88, ch) || isInside(mouseX, mouseY, hx - 2, hy - 2, 12, 44);
        g.fill(hx, hy, hx + 8, hy + 40, 0xFF785917);
        g.fill(hx + 1, hy + 1, hx + 7, hy + 39, hov ? 0xFFFDE047 : 0xFFD4AF37);
        g.fill(hx + 3, hy + 8, hx + 5, hy + 32, 0xFF2CE6FF);

        if (hov) {
            this.pendingTooltip = List.of(
                    Component.literal("§6Right Supply & Scribing Drawer"),
                    Component.literal("§7Holds Blank Parchment, Scribing Ink, Study Slot & the §eSCRIBE §7Wax Seal."),
                    Component.literal("§b• Click or drag the Right Brass Handle to slide open!")
            );
        }
    }

    private void renderRightOpenHandle(GuiGraphics g, int x, int y, int mouseX, int mouseY) {
        int hx = x + 312;
        int hy = y + 64;
        boolean hov = isInside(mouseX, mouseY, hx - 1, hy - 2, 11, 44);
        g.fill(hx, hy, hx + 8, hy + 40, 0xFF785917);
        g.fill(hx + 1, hy + 1, hx + 7, hy + 39, hov ? 0xFFFDE047 : 0xFFD4AF37);
        g.fill(hx + 3, hy + 8, hx + 5, hy + 32, 0xFF2CE6FF);
        if (hov) {
            this.pendingTooltip = List.of(
                    Component.literal("§6Right Supply Drawer Handle"),
                    Component.literal("§7Click or drag leftward to close the Supply Cover.")
            );
        }
    }

    // =========================================================================
    // CENTRAL UNROLLED PARCHMENT & CONCENTRIC ROTATING RINGS
    // =========================================================================

    private void renderCentralScrollAndRings(GuiGraphics g, int x, int y, int mouseX, int mouseY, float partialTick) {
        int fx = x + 84;
        int fy = y + 23;
        int fw = 148;
        int fh = 129;

        // Gilded outer frame with cyan Arcanite striped trim (matching all 4 approved renders!)
        g.fill(fx, fy, fx + fw, fy + fh, 0xFFD4AF37);
        for (int sx = fx + 8; sx < fx + fw - 8; sx += 14) {
            g.fill(sx, fy + fh - 6, sx + 5, fy + fh - 1, 0xFF2CE6FF);
        }
        g.fill(fx + 5, fy + 5, fx + fw - 5, fy + fh - 7, 0xFF2B124C);

        // Unrolled Parchment Scroll + Left/Right Wooden Scroll Rollers
        int px = fx + 12;
        int py = fy + 8;
        int pw = fw - 24; // 124
        int ph = fh - 20; // 109
        g.fill(px + 6, py + 2, px + pw - 6, py + ph - 2, 0xFFE5CFA3);
        g.fill(px + 10, py + 5, px + pw - 10, py + ph - 5, 0xFFECDAB4);
        // Left & Right Scroll Rollers
        g.fill(px, py, px + 8, py + ph, 0xFFC5A878);
        g.fill(px + 2, py - 2, px + 6, py + ph + 2, 0xFFA68859);
        g.fill(px + pw - 8, py, px + pw, py + ph, 0xFFC5A878);
        g.fill(px + pw - 6, py - 2, px + pw - 2, py + ph + 2, 0xFFA68859);

        int cx = x + WHEEL_REL_CX;
        int cy = y + WHEEL_REL_CY;

        float dx = mouseX - cx;
        float dy = mouseY - cy;
        float dist = Mth.sqrt(dx * dx + dy * dy);
        int hoveredRing = -1;
        if (!this.editingTitlePopup && isInside(mouseX, mouseY, px, py, pw, ph)) {
            if (dist <= RING_DUAL_OUT) hoveredRing = 3;
            else if (dist <= RING_PRIM_OUT + 1.0F) hoveredRing = 2;
            else if (dist <= RING_FORM_OUT + 1.0F) hoveredRing = 1;
            else if (dist <= RING_FLOW_OUT + 2.0F) hoveredRing = 0;
        }

        boolean ceremony = this.scribeCeremonyTicks > 0;
        int flowRgb = resolveEssenceRgb(this.flowMateria, 0x38BDF8);
        int formRgb = resolveEssenceRgb(currentSub().formMateria, 0x38BDF8);
        int primRgb = resolveEssenceRgb(currentSub().primaryMateria, 0x38BDF8);
        int dualRgb = resolveEssenceRgb(currentSub().secondaryMateria, 0xFACC15);

        // Draw the 3 Concentric Ring Bands + Inner Dual-Verb Core Medallion
        drawSingleWheelBand(g, cx, cy, RING_FLOW_IN, RING_FLOW_OUT, flowRgb, this.focusedRing == 0, hoveredRing == 0 || this.draggingRing == 0, ceremony);
        drawSingleWheelBand(g, cx, cy, RING_FORM_IN, RING_FORM_OUT, formRgb, this.focusedRing == 1, hoveredRing == 1 || this.draggingRing == 1, ceremony);
        drawSingleWheelBand(g, cx, cy, RING_PRIM_IN, RING_PRIM_OUT, primRgb, this.focusedRing == 2, hoveredRing == 2 || this.draggingRing == 2, ceremony);

        // Center Dual-Verb Medallion
        int coreFill = currentSub().secondaryEffect != null
                ? ArcaneScribingGlyphRenderer.scaleRgb(dualRgb, 0.42F)
                : 0xFF6B4F28;
        ArcaneScribingGlyphRenderer.drawFilledCircle(g, cx, cy, RING_DUAL_OUT, 0xFFD4AF37);
        ArcaneScribingGlyphRenderer.drawFilledCircle(g, cx, cy, RING_DUAL_OUT - 2.0F, coreFill);
        if (this.focusedRing == 3 || hoveredRing == 3 || ceremony) {
            ArcaneScribingGlyphRenderer.drawRingBand(g, cx, cy, RING_DUAL_OUT - 2.6F, RING_DUAL_OUT - 0.8F, 0xFF000000 | dualRgb);
        }

        // State 4 Harmonic Star Circuit Overlay when SCRIBE is pressed!
        if (ceremony) {
            for (int k = 0; k < 8; k++) {
                float a1 = k * (Mth.TWO_PI / 8.0F);
                float a2 = ((k + 3) % 8) * (Mth.TWO_PI / 8.0F);
                ArcaneScribingGlyphRenderer.drawPixelLine(
                        g,
                        cx + Mth.cos(a1) * 53.0F, cy + Mth.sin(a1) * 53.0F,
                        cx + Mth.cos(a2) * 53.0F, cy + Mth.sin(a2) * 53.0F,
                        0xCC67E8F9
                );
            }
            ArcaneScribingGlyphRenderer.drawPixelLine(g, cx - 54, cy, cx + 54, cy, 0xEEFEF08A);
            ArcaneScribingGlyphRenderer.drawPixelLine(g, cx, cy - 56, cx, cy + 54, 0xEEFEF08A);
        }

        // Render Rotating Symbols on Outer Flow Ring, Middle Form Ring, Inner Primary Ring, and Dual-Verb Core
        renderRingSymbols(g, cx, cy, 0, (RING_FLOW_IN + RING_FLOW_OUT) * 0.5F, flowRgb, mouseX, mouseY);
        renderRingSymbols(g, cx, cy, 1, (RING_FORM_IN + RING_FORM_OUT) * 0.5F, formRgb, mouseX, mouseY);
        renderRingSymbols(g, cx, cy, 2, (RING_PRIM_IN + RING_PRIM_OUT) * 0.5F, primRgb, mouseX, mouseY);

        // Center Dual-Verb Glyph
        SpellCoreEffect secEff = currentSub().secondaryEffect;
        boolean secCryptic = secEff != null && isCryptic(SpellResearchManager.nodeId(secEff));
        ArcaneScribingGlyphRenderer.drawEffectSymbol(g, this.font, cx, cy, secEff, secCryptic, 0xFF000000 | dualRgb);
        if (hoveredRing == 3 && this.pendingTooltip == null) {
            this.pendingTooltip = buildEffectDecisionTooltip(secEff, currentSub().secondaryMateria, currentSub().secondaryMutation, true, true);
        }

        // Glowing Cyan Directional Rotation Arcs when hovering or dragging a ring (matching State 1 & State 3!)
        int activeArcRing = this.draggingRing >= 0 ? this.draggingRing : hoveredRing;
        if (activeArcRing >= 0 && activeArcRing <= 2) {
            float arcR = switch (activeArcRing) {
                case 0 -> RING_FLOW_OUT + 3.2F;
                case 1 -> (RING_FORM_OUT + RING_FLOW_IN) * 0.5F;
                default -> (RING_PRIM_OUT + RING_FORM_IN) * 0.5F;
            };
            ArcaneScribingGlyphRenderer.drawArcWithArrows(g, cx, cy, arcR, 2.35F, 3.92F, 0xEE67E8F9);
            ArcaneScribingGlyphRenderer.drawArcWithArrows(g, cx, cy, arcR, -0.78F, 0.78F, 0xEE67E8F9);
        }

        // Ornate 12 o'clock Golden Alignment Needle pointing straight down through all 3 rings!
        renderGoldenAlignmentNeedle(g, cx, y + 25, cy, mouseX, mouseY, ceremony);

        // State 4 Sweeping Feather Quill Animation
        if (ceremony) {
            float prog = 1.0F - (this.scribeCeremonyTicks - partialTick) / 36.0F;
            ArcaneScribingGlyphRenderer.drawQuillInscribing(g, cx, cy, Mth.clamp(prog, 0.0F, 1.0F));
        }
    }

    private void drawSingleWheelBand(GuiGraphics g, int cx, int cy, float inR, float outR,
                                     int matRgb, boolean focused, boolean hovered, boolean ceremony) {
        int bandDark = ceremony ? 0xFF3E3216 : (hovered ? 0xFF6B5028 : 0xFF563F1E);
        int rimCol = ceremony ? 0xFFFEF08A : ((focused || hovered) ? (0xFF000000 | matRgb) : 0xFFC59B27);
        ArcaneScribingGlyphRenderer.drawRingBand(g, cx, cy, inR, outR, bandDark);
        ArcaneScribingGlyphRenderer.drawRingBand(g, cx, cy, outR - 1.2F, outR, rimCol);
        ArcaneScribingGlyphRenderer.drawRingBand(g, cx, cy, inR, inR + 1.1F, rimCol);
    }

    private void renderRingSymbols(GuiGraphics g, int cx, int cy, int ringIdx, float midR, int matRgb, int mouseX, int mouseY) {
        int count = getRingOptionCount(ringIdx);
        if (count <= 0) return;
        int selIdx = getRingSelectedIndex(ringIdx);
        float stepRad = Mth.TWO_PI / count;
        float offset = this.ringAngleOffset[ringIdx];

        for (int i = 0; i < count; i++) {
            float ang = -Mth.HALF_PI + (i - selIdx) * stepRad + offset;
            // Radial divider tick halfway between adjacent symbols
            float divAng = ang + stepRad * 0.5F;
            ArcaneScribingGlyphRenderer.drawPixelLine(
                    g,
                    cx + Mth.cos(divAng) * (midR - 4.8F),
                    cy + Mth.sin(divAng) * (midR - 4.8F),
                    cx + Mth.cos(divAng) * (midR + 4.8F),
                    cy + Mth.sin(divAng) * (midR + 4.8F),
                    0x66C59B27
            );

            int sx = Math.round(cx + Mth.cos(ang) * midR);
            int sy = Math.round(cy + Mth.sin(ang) * midR);
            boolean isSelectedAtNeedle = (i == selIdx && Math.abs(offset) < stepRad * 0.35F);
            boolean symHover = !this.editingTitlePopup && Math.hypot(mouseX - sx, mouseY - sy) <= 5.6D;

            if (isSelectedAtNeedle) {
                ArcaneScribingGlyphRenderer.drawFilledCircle(g, sx, sy, 5.2F, 0xCC1E1B4B);
                ArcaneScribingGlyphRenderer.drawRingBand(g, sx, sy, 4.3F, 5.5F, 0xFFFDE047);
            } else if (symHover) {
                ArcaneScribingGlyphRenderer.drawFilledCircle(g, sx, sy, 4.8F, 0xAA0F172A);
            }

            int symCol = isSelectedAtNeedle
                    ? ArcaneScribingGlyphRenderer.liftRgb(matRgb, 75)
                    : (symHover ? 0xFFFFFFFF : (0xFF000000 | matRgb));

            switch (ringIdx) {
                case 0 -> {
                    SpellFlowModifier fl = SpellFlowModifier.values()[i];
                    boolean cryp = isCryptic(SpellResearchManager.nodeId(fl));
                    ArcaneScribingGlyphRenderer.drawFlowSymbol(g, this.font, sx, sy, fl, cryp, symCol);
                    if (symHover) {
                        this.pendingTooltip = buildFlowDecisionTooltip(fl, this.flowMateria, this.flowMutation, i == selIdx);
                    }
                }
                case 1 -> {
                    SpellDeliveryForm fm = SpellDeliveryForm.values()[i];
                    boolean cryp = isCryptic(SpellResearchManager.nodeId(fm));
                    ArcaneScribingGlyphRenderer.drawFormSymbol(g, this.font, sx, sy, fm, cryp, symCol);
                    if (symHover) {
                        this.pendingTooltip = buildFormDecisionTooltip(fm, currentSub().formMateria, currentSub().formMutation, i == selIdx);
                    }
                }
                case 2 -> {
                    List<SpellCoreEffect> allowed = allowedEffects();
                    SpellCoreEffect ef = allowed.get(i);
                    boolean cryp = isCryptic(SpellResearchManager.nodeId(ef));
                    ArcaneScribingGlyphRenderer.drawEffectSymbol(g, this.font, sx, sy, ef, cryp, symCol);
                    if (symHover) {
                        this.pendingTooltip = buildEffectDecisionTooltip(ef, currentSub().primaryMateria, currentSub().primaryMutation, false, i == selIdx);
                    }
                }
            }
        }
    }

    private void renderGoldenAlignmentNeedle(GuiGraphics g, int cx, int topY, int cy, int mouseX, int mouseY, boolean ceremony) {
        // Ornate Fleur-de-lis Pommel above the 12 o'clock slot (matching State 1, 3, 4!)
        g.fill(cx - 1, topY, cx + 2, topY + 12, 0xFFFDE047);
        g.fill(cx - 4, topY + 4, cx + 5, topY + 7, 0xFFD4AF37);
        g.fill(cx - 2, topY + 2, cx + 3, topY + 9, 0xFFFACC15);
        if (ceremony) {
            g.fill(cx - 1, topY + 1, cx + 2, topY + 8, 0xFF67E8F9);
        }
        // Golden pointer tip & slender translucent alignment blade down to center
        ArcaneScribingGlyphRenderer.drawPixelLine(g, cx - 3, topY + 9, cx, topY + 14, 0xFFFDE047);
        ArcaneScribingGlyphRenderer.drawPixelLine(g, cx + 3, topY + 9, cx, topY + 14, 0xFFFDE047);
        g.fill(cx - 2, topY + 14, cx - 1, cy - 17, 0xAAFFD700);
        g.fill(cx + 2, topY + 14, cx + 3, cy - 17, 0xAAFFD700);

        boolean needleHover = !this.editingTitlePopup && isInside(mouseX, mouseY, cx - 5, topY, 11, 16);
        if (needleHover && this.pendingTooltip == null) {
            SpellAssembly built = buildDraftAssembly();
            SpellCostBreakdown cost = SpellFuelCalculator.calculateCastCost(built, null, 0.0F, EssenceType.REGULAR);
            this.pendingTooltip = List.of(
                    Component.literal("§6✦ Aligned Incantation Summary ✦"),
                    Component.literal("§f" + built.customName() + " §8• §eTier " + ROMAN[this.potencyTier - 1] + " " + this.prefix.getDisplayName()),
                    Component.literal("§7Flow: §b" + formatResearchAwareName(SpellResearchManager.nodeId(this.flow), this.flow.getDisplayName())
                            + " §8(" + getActiveSlotCount() + " Sub-Spell" + (getActiveSlotCount() > 1 ? "s" : "") + ")"),
                    Component.literal("§7Cast Fuel: §e" + cost.formatTypeCostSummary()),
                    Component.literal(String.format("§7Cooldown: §b%.1fs §8• Hover any rune below for full stats!", built.getBaseCooldownTicks() / 20.0F))
            );
        }
    }

    // =========================================================================
    // UNDER-DESK INVENTORY TRAY & QUICK PLAQUES (y = 155..235)
    // =========================================================================

    private void renderUnderDeskTray(GuiGraphics g, int x, int y, int mouseX, int mouseY) {
        // Center Player Inventory Satchel Frame (x + 73 .. x + 243, y + 155 .. y + 235)
        g.fill(x + 73, y + 155, x + 243, y + 235, 0xFFD4AF37);
        g.fill(x + 74, y + 156, x + 242, y + 234, 0xF20B132B);
        for (int i = 4; i < this.menu.slots.size(); i++) {
            Slot s = this.menu.slots.get(i);
            int sx = x + s.x - 1;
            int sy = y + s.y - 1;
            g.fill(sx, sy, sx + 18, sy + 18, 0xFF3A4B6E);
            g.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF141E33);
        }

        // Left Plaque: Tier Stepper (I..X), Lock Badge & Spell Name Button (x + 4 .. x + 71, y + 155 .. y + 235)
        g.fill(x + 4, y + 155, x + 71, y + 235, 0xFFD4AF37);
        g.fill(x + 5, y + 156, x + 70, y + 234, 0xF20B132B);

        // Tier [-] Tier X [+]
        boolean minusHov = isInside(mouseX, mouseY, x + 8, y + 160, 13, 13);
        boolean plusHov = isInside(mouseX, mouseY, x + 54, y + 160, 13, 13);
        boolean tierHov = isInside(mouseX, mouseY, x + 21, y + 160, 33, 13);
        g.fill(x + 8, y + 160, x + 21, y + 173, minusHov ? 0xFFFDE047 : 0xFF334155);
        g.drawCenteredString(this.font, "-", x + 14, y + 162, minusHov ? 0xFF0F172A : 0xFFFFFFFF);
        g.drawCenteredString(this.font, "T-" + ROMAN[this.potencyTier - 1], x + 37, y + 162, 0xFF67E8F9);
        g.fill(x + 54, y + 160, x + 67, y + 173, plusHov ? 0xFFFDE047 : 0xFF334155);
        g.drawCenteredString(this.font, "+", x + 60, y + 162, plusHov ? 0xFF0F172A : 0xFFFFFFFF);
        if (minusHov || plusHov || tierHov) {
            this.pendingTooltip = List.of(
                    Component.literal("§6Potency Tier " + ROMAN[this.potencyTier - 1] + " §8(Stage " + MateriaStageTable.getStageUnitCode(this.potencyTier) + ")"),
                    Component.literal(String.format("§7Base Combat Power: §c%.0f HP", MateriaStageTable.getBaseCombatDamageHp(this.potencyTier))),
                    Component.literal(String.format("§7Base Ward Shield: §b%.0f HP §8| §7Heal: §a%.0f HP",
                            MateriaStageTable.getBaseWardShieldHp(this.potencyTier),
                            MateriaStageTable.getBaseRestorationHealHp(this.potencyTier))),
                    Component.literal("§8• Click - / + (or scroll here) to choose Spell Potency Tier I–X")
            );
        }

        // Quick Rename Ribbon Button
        boolean nameHov = isInside(mouseX, mouseY, x + 8, y + 177, 59, 14);
        g.fill(x + 8, y + 177, x + 67, y + 191, nameHov ? 0xFFD4AF37 : 0xFF1E293B);
        g.drawCenteredString(this.font, trimLabel(this.customName, 9), x + 37, y + 180, nameHov ? 0xFF0F172A : 0xFFFDE68A);
        if (nameHov) {
            this.pendingTooltip = List.of(
                    Component.literal("§6Spell Name: §f" + this.customName),
                    Component.literal("§8• Click to rename or add custom incantation lore")
            );
        }

        // Quick Left Drawer Toggle Button
        boolean lDrawBtnHov = isInside(mouseX, mouseY, x + 8, y + 194, 59, 14);
        g.fill(x + 8, y + 194, x + 67, y + 208, lDrawBtnHov ? 0xFF7E22CE : 0xFF2B124C);
        g.drawCenteredString(this.font, this.leftDrawerTarget > 0.5F ? "◀ Tuck" : "▶ Drawer", x + 37, y + 197, 0xFFE9D5FF);

        // Cooldown readout
        SpellAssembly preview = buildDraftAssembly();
        g.drawCenteredString(this.font, String.format("CD: %.1fs", preview.getBaseCooldownTicks() / 20.0F), x + 37, y + 215, 0xFF94A3B8);

        // Right Plaque: Live Fuel Cost & Quick Supply Drawer Toggle (x + 245 .. x + 312, y + 155 .. y + 235)
        g.fill(x + 245, y + 155, x + 312, y + 235, 0xFFD4AF37);
        g.fill(x + 246, y + 156, x + 311, y + 234, 0xF20B132B);
        g.drawCenteredString(this.font, "Fuel Cost", x + 278, y + 160, 0xFFFDE047);

        SpellCostBreakdown cost = SpellFuelCalculator.calculateCastCost(preview, null, 0.0F, EssenceType.REGULAR);
        String costStr = cost.formatTypeCostSummary();
        g.drawCenteredString(this.font, trimLabel(costStr, 10), x + 278, y + 173, 0xFF67E8F9);
        g.drawCenteredString(this.font, this.materiaSpecific ? "-15% Lock" : "Dynamic", x + 278, y + 185,
                this.materiaSpecific ? 0xFF4ADE80 : 0xFFCBD5E1);

        boolean rDrawBtnHov = isInside(mouseX, mouseY, x + 249, y + 198, 59, 14);
        g.fill(x + 249, y + 198, x + 308, y + 212, rDrawBtnHov ? 0xFFD97706 : 0xFF3B2314);
        g.drawCenteredString(this.font, this.rightDrawerTarget > 0.5F ? "Cover ▶" : "◀ Scribe", x + 278, y + 201, 0xFFFDE68A);

        boolean ready = this.menu.getSlot(0).hasItem() && this.menu.getSlot(1).hasItem();
        g.drawCenteredString(this.font, ready ? "Ink Ready" : "Need Ink", x + 278, y + 217, ready ? 0xFF4ADE80 : 0xFFF87171);

        if (isInside(mouseX, mouseY, x + 246, y + 158, 65, 36)) {
            this.pendingTooltip = List.of(
                    Component.literal("§6Live Cast Fuel Breakdown"),
                    Component.literal("§7Total per Cast: §e" + costStr),
                    Component.literal(this.materiaSpecific
                            ? "§aIncludes 15% Materia-Locked efficiency discount!"
                            : "§7Dynamic mode: draws from your active Universal Materia Cell.")
            );
        }
    }

    private void renderTitleEditorBackdrop(GuiGraphics g, int x, int y) {
        int px = x + 92;
        int py = y + 46;
        int pw = 132;
        int ph = 76;
        g.fill(px - 2, py - 2, px + pw + 2, py + ph + 2, 0x99000000);
        g.fill(px, py, px + pw, py + ph, 0xFFD4AF37);
        g.fill(px + 1, py + 1, px + pw - 1, py + ph - 1, 0xF80B132B);
        g.drawString(this.font, "§6Spell Name:", px + 8, py + 5, 0xFFFFFFFF, false);
        g.drawString(this.font, "§7Custom Lore:", px + 8, py + 32, 0xFFFFFFFF, false);
        g.fill(px + 42, py + 59, px + 90, py + 72, 0xFFD4AF37);
        g.fill(px + 43, py + 60, px + 89, py + 71, 0xFF1E293B);
        g.drawCenteredString(this.font, "Done", px + 66, py + 62, 0xFFFDE047);
    }

    // =========================================================================
    // RICH SPELL DECISION TOOLTIP BUILDERS (STATE 3)
    // =========================================================================

    private List<Component> buildFlowDecisionTooltip(SpellFlowModifier fl, EssenceType mat, boolean mutEnabled, boolean isCurrent) {
        List<Component> lines = new ArrayList<>();
        String nodeId = SpellResearchManager.nodeId(fl);
        if (isCryptic(nodeId)) {
            lines.add(Component.literal("§dCryptic Unknown Flow Rune [?]"));
            lines.add(Component.literal("§7Outer Ring: §dUnresearched Flow Modifier"));
            lines.add(Component.literal("§7• Align & Scribe to test this mysterious rune in the wild!"));
            lines.add(Component.literal("§8• Observing or casting it unlocks its true name & Mutations."));
            return lines;
        }
        Player p = Minecraft.getInstance().player;
        boolean canMut = mat != EssenceType.REGULAR && (p == null || (SpellResearchManager.isNodeUnlocked(p, nodeId)
                && SpellResearchManager.isMutationUnlocked(p, nodeId, mat, true)));
        SpellMutationProfile prof = SpellMutationRegistry.resolveFlow(fl, mat, mutEnabled && canMut);

        String titleCol = mat == EssenceType.REGULAR ? "§b" : mat.getColorCode();
        lines.add(Component.literal(titleCol + (prof.mutated() ? prof.signatureTitle() : fl.getDisplayName())
                + (isObservedOnly(nodeId) ? " §e(Observed)" : "")));
        lines.add(Component.literal("§6Flow: §b" + fl.getDisplayName() + " §8• §6Materia: " + formatTooltipMateriaState(mat, prof.mutated())));

        int count = fl.getDefaultSubSpellCount();
        String flowTrait = count > 1
                ? ("Unleashes " + count + " Sub-Spell streams" + (fl.usesDynamicTriggerBilling() ? " (25% Upfront / 75% Trigger)" : ""))
                : (fl.getVelocityMultiplier() > 1.2F ? "High-speed dart (+85% projectile velocity)"
                : (fl.getVelocityMultiplier() > 0.0F && fl.getVelocityMultiplier() < 0.7F ? "Heavy deliberate drift (+0.40 Materia Advantage)"
                : "Focused single-channel spell trajectory"));
        lines.add(Component.literal("§7• " + flowTrait));
        appendMutationTraitBullets(lines, prof);

        SpellAssembly preview = buildDraftAssembly();
        SpellCostBreakdown cost = SpellFuelCalculator.calculateCastCost(preview, null, 0.0F, EssenceType.REGULAR);
        lines.add(Component.literal("§7• Spell Cast Fuel: §b" + cost.formatTypeCostSummary()));
        lines.add(Component.literal(String.format("§7• Cooldown: §f%.1fs §8• %s",
                preview.getBaseCooldownTicks() / 20.0F,
                isCurrent ? "Aligned under Needle!" : "Click or Drag Ring to Align")));
        return lines;
    }

    private List<Component> buildFormDecisionTooltip(SpellDeliveryForm fm, EssenceType mat, boolean mutEnabled, boolean isCurrent) {
        List<Component> lines = new ArrayList<>();
        String nodeId = SpellResearchManager.nodeId(fm);
        if (isCryptic(nodeId)) {
            lines.add(Component.literal("§dCryptic Unknown Form Rune [?]"));
            lines.add(Component.literal("§7Middle Ring: §dUnresearched Delivery Shape"));
            lines.add(Component.literal("§7• Align & Scribe for blind experimentation!"));
            lines.add(Component.literal("§8• Mutations stay locked until the base rune is researched."));
            return lines;
        }
        Player p = Minecraft.getInstance().player;
        boolean canMut = mat != EssenceType.REGULAR && (p == null || (SpellResearchManager.isNodeUnlocked(p, nodeId)
                && SpellResearchManager.isMutationUnlocked(p, nodeId, mat, true)));
        SpellMutationProfile prof = SpellMutationRegistry.resolveForm(fm, mat, mutEnabled && canMut);

        String titleCol = mat == EssenceType.REGULAR ? "§b" : mat.getColorCode();
        lines.add(Component.literal(titleCol + (prof.mutated() ? prof.signatureTitle() : fm.getDisplayName())
                + (isObservedOnly(nodeId) ? " §e(Observed)" : "")));
        lines.add(Component.literal("§6Form: §b" + fm.getDisplayName() + " §8• §6Materia: " + formatTooltipMateriaState(mat, prof.mutated())));

        String shapeDesc = switch (fm) {
            case PROJECTILE -> "Fires an aimed arcane bolt toward your target";
            case BEAM -> "Channels a " + SpellDeliveryForm.getBeamReachBlocks(this.potencyTier, false) + "-block continuous energy beam";
            case RADIAL_BURST -> "Detonates an expanding nova ring around impact/caster";
            case TOUCH_STRIKE -> "Close-range arcane cleave right in front of you";
            case AURA -> "Wraps you in a sustained protective/reactive shroud";
            case GLYPH -> "Inscribes a triggered ground rune circle";
            case CONE -> "Sweeps a wide elemental fan spray forward";
            case SKYFALL -> "Calls down celestial pillars from the sky";
            case SUMMON_AGENT -> "Conjures a mobile Eidolon companion that hunts foes";
            case SUMMON_SENTRY -> "Conjures a stationary Eidolon turret at target spot";
            case SUMMON_DECOY -> "Conjures an aggro-drawing Phantom Eidolon decoy";
        };
        lines.add(Component.literal("§7• " + shapeDesc));
        appendMutationTraitBullets(lines, prof);

        lines.add(Component.literal(String.format("§7• Base Power: §f%.0f HP §8(Tier %s)",
                MateriaStageTable.getBaseCombatDamageHp(this.potencyTier), ROMAN[this.potencyTier - 1])));
        SpellAssembly preview = buildDraftAssembly();
        SpellCostBreakdown cost = SpellFuelCalculator.calculateCastCost(preview, null, 0.0F, EssenceType.REGULAR);
        lines.add(Component.literal("§7• Fuel Cost: §b" + cost.formatTypeCostSummary()));
        lines.add(Component.literal(String.format("§7• Cooldown: §f%.1fs", preview.getBaseCooldownTicks() / 20.0F)));
        return lines;
    }

    private List<Component> buildEffectDecisionTooltip(@Nullable SpellCoreEffect ef, EssenceType mat, boolean mutEnabled,
                                                       boolean isDualVerb, boolean isCurrent) {
        List<Component> lines = new ArrayList<>();
        if (ef == null) {
            lines.add(Component.literal("§6Dual-Verb Core: §7None (Single Effect)"));
            lines.add(Component.literal("§7• Click or scroll the center medallion to weave a 2nd Core Effect!"));
            lines.add(Component.literal("§8• Dual-Verb spells fire both effects together (+100% sub-spell fuel)."));
            return lines;
        }
        String nodeId = SpellResearchManager.nodeId(ef);
        if (isCryptic(nodeId)) {
            lines.add(Component.literal("§dCryptic Unknown Effect Rune [?]"));
            lines.add(Component.literal("§7" + (isDualVerb ? "Center Medallion" : "Inner Ring") + ": §dUnresearched Core Effect"));
            lines.add(Component.literal("§7• Scribe & cast this rune to discover what it does!"));
            return lines;
        }

        SpellPartSlotType slotType = isDualVerb ? SpellPartSlotType.SECONDARY_EFFECT : SpellPartSlotType.CORE_EFFECT;
        Player p = Minecraft.getInstance().player;
        boolean canMut = mat != EssenceType.REGULAR && (p == null || (SpellResearchManager.isNodeUnlocked(p, nodeId)
                && SpellResearchManager.isMutationUnlocked(p, nodeId, mat, true)));
        SpellMutationProfile prof = SpellMutationRegistry.resolveEffect(slotType, ef, mat, mutEnabled && canMut);

        String titleCol = mat == EssenceType.REGULAR ? "§e" : mat.getColorCode();
        lines.add(Component.literal(titleCol + (prof.mutated() ? prof.signatureTitle() : ef.getDisplayName())
                + (isDualVerb ? " §6[+Dual-Verb]" : "")));
        lines.add(Component.literal("§6" + (isDualVerb ? "Dual-Verb" : "Effect") + ": §b" + ef.getDisplayName()
                + " §8• §6Materia: " + formatTooltipMateriaState(mat, prof.mutated())));

        String powerLine = switch (ef.getRootFamily()) {
            case HARM -> String.format("Deals %.0f HP elemental damage (Tier %s)",
                    MateriaStageTable.getBaseCombatDamageHp(this.potencyTier) * prof.potencyMultiplier(), ROMAN[this.potencyTier - 1]);
            case WARD -> String.format("Grants %.0f HP arcane ward barrier (Tier %s)",
                    MateriaStageTable.getBaseWardShieldHp(this.potencyTier) * prof.potencyMultiplier(), ROMAN[this.potencyTier - 1]);
            case RESTORATION -> String.format("Restores %.0f HP vitality & mends allies (Tier %s)",
                    MateriaStageTable.getBaseRestorationHealHp(this.potencyTier) * prof.potencyMultiplier(), ROMAN[this.potencyTier - 1]);
            case KINETIC -> "Applies forceful knockback, pull, flight, or grapple impulse";
            case BLINK -> "Teleports or phase-steps across space instantly";
            case TRANSMUTE -> "Breaks, harvests, or transmutes blocks and matter";
            case AFFLICTION -> "Inflicts lingering elemental hexes & status debuffs";
            case CONJURE -> "Conjures arcane light, constructs, sonar, or weather shifts";
        };
        lines.add(Component.literal("§7• " + powerLine));
        appendMutationTraitBullets(lines, prof);

        SpellAssembly preview = buildDraftAssembly();
        SpellCostBreakdown cost = SpellFuelCalculator.calculateCastCost(preview, null, 0.0F, EssenceType.REGULAR);
        lines.add(Component.literal("§7• Fuel Cost: §b" + cost.formatTypeCostSummary()));
        lines.add(Component.literal(String.format("§7• Cooldown: §f%.1fs", preview.getBaseCooldownTicks() / 20.0F)));
        return lines;
    }

    private void appendMutationTraitBullets(List<Component> lines, SpellMutationProfile prof) {
        if (!prof.mutated()) return;
        List<String> perks = new ArrayList<>();
        if (prof.bonusPierceTargets() > 0) perks.add("+" + prof.bonusPierceTargets() + " Target Pierce");
        if (prof.wallPhasing()) perks.add("Phases Through Walls");
        if (prof.armorBypassFraction() > 0.01F) perks.add(Math.round(prof.armorBypassFraction() * 100) + "% Armor Bypass");
        if (prof.armorShredPoints() > 0) perks.add("+" + prof.armorShredPoints() + " Armor Shred");
        if (Math.abs(prof.velocityMultiplier() - 1.0F) > 0.04F) {
            int pct = Math.round((prof.velocityMultiplier() - 1.0F) * 100);
            perks.add((pct > 0 ? "+" : "") + pct + "% Speed");
        }
        if (prof.rangeMultiplier() > 1.04F) perks.add("+" + Math.round((prof.rangeMultiplier() - 1.0F) * 100) + "% Reach");
        if (prof.radiusMultiplier() > 1.04F) perks.add("+" + Math.round((prof.radiusMultiplier() - 1.0F) * 100) + "% AoE Radius");
        if (prof.lifestealFraction() > 0.01F) perks.add(Math.round(prof.lifestealFraction() * 100) + "% Lifesteal");
        if (prof.silkTouchInherited()) perks.add("Silk-Touch Harvest");
        if (prof.autoSmeltInherited()) perks.add("Auto-Smelts Drops");
        if (!perks.isEmpty()) {
            lines.add(Component.literal("§a• " + String.join(" • ", perks)));
        }
    }

    private List<Component> buildMateriaVialTooltip(EssenceType mat) {
        String[] ringLabels = {"Outer Flow Ring", "Middle Delivery Form Ring", "Inner Primary Effect Ring", "Center Dual-Verb Core"};
        if (mat == EssenceType.REGULAR) {
            return List.of(
                    Component.literal("§fClear Unattuned Vial (Dynamic)"),
                    Component.literal("§7Target: §e" + ringLabels[this.focusedRing]),
                    Component.literal("§7Leaves this ring unattuned so it adapts dynamically to your Staff/Wand fuel.")
            );
        }
        return List.of(
                Component.literal(mat.getFormattedName() + " Materia Infusion"),
                Component.literal("§7Target: §e" + ringLabels[this.focusedRing]),
                Component.literal("§7 Epithet: §f" + SpellMutationRegistry.getEpithet(mat)),
                Component.literal("§b• Click to infuse " + mat.getDisplayName() + " into this ring!")
        );
    }

    private List<Component> buildMutationSwitchTooltip(boolean canEnable, boolean mutOn, EssenceType mat) {
        if (mat == EssenceType.REGULAR) {
            return List.of(
                    Component.literal("§7Signature Mutation Lever (Inactive)"),
                    Component.literal("§7Select a colored §bMateria Vial or Shard §7above first to unlock elemental mutations!")
            );
        }
        if (!canEnable) {
            return List.of(
                    Component.literal("§cSignature Mutation Locked"),
                    Component.literal("§7Research the aligned spell rune & its " + mat.getFormattedName() + " §7combination first."),
                    Component.literal("§8(Cryptic '?' runes always scribe with Mutation OFF for safe blind testing.)")
            );
        }
        return List.of(
                Component.literal(mutOn ? "§aSignature Mutation: ENABLED" : "§eSignature Mutation: OFF (Pure Tinted)"),
                Component.literal("§7Toggle ON to transform this part's behavior with " + mat.getFormattedName() + "§7's unique traits,"),
                Component.literal("§7or leave OFF to keep clean baseline behavior with " + mat.getDisplayName() + " color & fuel!")
        );
    }

    private static String formatTooltipMateriaState(EssenceType mat, boolean mutated) {
        if (mat == null || mat == EssenceType.REGULAR) return "§fDynamic";
        return mat.getFormattedName() + (mutated ? " §a(Mutated)" : " §7(Tinted)");
    }

    private String formatResearchAwareName(String nodeId, String realName) {
        return isCryptic(nodeId) ? "§dCryptic [?]" : realName;
    }

    // =========================================================================
    // INTERACTIVE INPUT: DRAG DRAWERS, SPIN RINGS, SCROLL & CLICK
    // =========================================================================

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x();
        double my = event.y();
        int btn = event.button();
        int x = this.leftPos;
        int y = this.topPos;

        // 0. Title & Lore Popup
        if (this.editingTitlePopup) {
            int px = x + 92;
            int py = y + 46;
            if (isInside(mx, my, px + 42, py + 59, 48, 13) || !isInside(mx, my, px, py, 132, 76)) {
                setEditingTitlePopup(false);
                playClickSound(1.1F);
                return true;
            }
            return super.mouseClicked(event, doubleClick);
        }

        // 1. Top Shelf Inkwells & '?' Tome Pedestal
        for (int r = 0; r < 4; r++) {
            if (isInside(mx, my, x + 94 + r * 26, y + 1, 16, 19)) {
                this.focusedRing = r;
                playClickSound(1.25F);
                return true;
            }
        }
        if (isInside(mx, my, x + 202, y + 1, 20, 20)) {
            setEditingTitlePopup(true);
            playPageTurnSound();
            return true;
        }

        // 2. Left Drawer Handle & Open Drawer Controls
        int leftHandleX = this.leftDrawerOpen <= 0.02F
                ? (x - 8)
                : (x - 38 + Math.round((1.0F - this.leftDrawerOpen) * 124.0F) - 9);
        if (isInside(mx, my, leftHandleX - 3, y + 60, 14, 48)) {
            this.draggingLeftDrawer = true;
            this.dragStartMouseX = mx;
            this.dragStartDrawerOpen = this.leftDrawerOpen;
            this.dragMovedSignificantly = false;
            return true;
        }
        if (this.leftDrawerOpen < 0.25F && isInside(mx, my, x + 8, y + 34, 70, 72)) {
            this.leftDrawerTarget = 1.0F;
            playWoodenSlideSound(1.1F);
            return true;
        }
        if (this.leftDrawerOpen > 0.7F && handleOpenLeftDrawerClick(mx, my, x, y)) {
            return true;
        }

        // 3. Right Supply Drawer Handle / Closed Cover / Open Controls
        int rightHandleX = this.rightDrawerOpen >= 0.99F
                ? (x + 312)
                : Math.min(x + 346, x + 314 + Math.round(this.rightDrawerOpen * 32.0F));
        if (isInside(mx, my, rightHandleX - 3, y + 60, 14, 48)
                || (this.rightDrawerOpen < 0.85F && isInside(mx, my, x + 236, y + 25, 76, 124))) {
            this.draggingRightDrawer = true;
            this.dragStartMouseX = mx;
            this.dragStartDrawerOpen = this.rightDrawerOpen;
            this.dragMovedSignificantly = false;
            return true;
        }
        if (this.rightDrawerOpen > 0.72F && handleOpenRightDrawerClick(mx, my, btn, x, y)) {
            return true;
        }

        // 4. Concentric Symbol Rings (Click-and-Drag Rotation or Direct Symbol Click!)
        int cx = x + WHEEL_REL_CX;
        int cy = y + WHEEL_REL_CY;
        if (isInside(mx, my, x + 96, y + 31, 124, 109)) {
            float dist = (float) Math.hypot(mx - cx, my - cy);
            if (dist <= RING_DUAL_OUT) {
                this.focusedRing = 3;
                cycleRing(3, btn == 1 ? -1 : 1);
                playRingTickSound(1.25F);
                return true;
            } else if (dist <= RING_FLOW_OUT + 3.0F) {
                int ring = dist <= RING_PRIM_OUT + 1.0F ? 2 : (dist <= RING_FORM_OUT + 1.0F ? 1 : 0);
                this.focusedRing = ring;
                this.draggingRing = ring;
                this.lastRingDragMouseAngle = (float) Mth.atan2(my - cy, mx - cx);
                this.ringClickStartX = mx;
                this.ringClickStartY = my;
                this.dragMovedSignificantly = false;
                return true;
            }
        }

        // 5. Under-Desk Plaques
        if (isInside(mx, my, x + 8, y + 160, 13, 13)) {
            this.potencyTier = Math.max(1, this.potencyTier - 1);
            playClickSound(0.95F);
            return true;
        }
        if (isInside(mx, my, x + 54, y + 160, 13, 13)) {
            this.potencyTier = Math.min(10, this.potencyTier + 1);
            playClickSound(1.15F);
            return true;
        }
        if (isInside(mx, my, x + 8, y + 177, 59, 14)) {
            setEditingTitlePopup(true);
            playPageTurnSound();
            return true;
        }
        if (isInside(mx, my, x + 8, y + 194, 59, 14)) {
            this.leftDrawerTarget = this.leftDrawerTarget > 0.5F ? 0.0F : 1.0F;
            playWoodenSlideSound(1.05F);
            return true;
        }
        if (isInside(mx, my, x + 249, y + 198, 59, 14)) {
            this.rightDrawerTarget = this.rightDrawerTarget > 0.5F ? 0.0F : 1.0F;
            playWoodenSlideSound(1.05F);
            return true;
        }

        return super.mouseClicked(event, doubleClick);
    }

    private boolean handleOpenLeftDrawerClick(double mx, double my, int x, int y) {
        int dx = x - 38 + Math.round((1.0F - this.leftDrawerOpen) * 124.0F);
        int dy = y + 25;
        if (!isInside(mx, my, dx, dy, 124, 124)) return false;

        // Wax Seals (Combat, Utility, Misc, Lock)
        int compAY = dy + 7;
        for (int s = 0; s < 4; s++) {
            int cardX = dx + 6 + s * 28;
            if (isInside(mx, my, cardX, compAY, 26, 27)) {
                if (s < 3) {
                    this.prefix = CRAFTABLE_PREFIXES[s];
                    normalizeDraftForPrefixAndResearch();
                } else {
                    this.materiaSpecific = !this.materiaSpecific;
                }
                playClickSound(1.1F);
                return true;
            }
        }
        // Sub-Spell Roman Tabs I..VIII
        int tabY = compAY + 28;
        int slotCount = getActiveSlotCount();
        for (int i = 0; i < slotCount; i++) {
            if (isInside(mx, my, dx + 6 + i * 14, tabY, 13, 11)) {
                this.activeSubSlot = i;
                playPageTurnSound();
                return true;
            }
        }
        // Ring Focus Pills
        for (int r = 0; r < 4; r++) {
            if (isInside(mx, my, dx + 6 + r * 28, dy + 53, 26, 10)) {
                this.focusedRing = r;
                playClickSound(1.25F);
                return true;
            }
        }
        // Materia Vials & Page Arrows
        int vialY = dy + 65;
        if (this.availableMateria.size() > MATERIA_PER_PAGE) {
            if (isInside(mx, my, dx + 2, vialY + 2, 10, 16) && this.materiaPage > 0) {
                this.materiaPage--;
                playClickSound(1.1F);
                return true;
            }
            if (isInside(mx, my, dx + 110, vialY + 2, 12, 16)
                    && (this.materiaPage + 1) * MATERIA_PER_PAGE < this.availableMateria.size()) {
                this.materiaPage++;
                playClickSound(1.2F);
                return true;
            }
        }
        int startIdx = this.materiaPage * MATERIA_PER_PAGE;
        for (int slot = 0; slot < MATERIA_PER_PAGE; slot++) {
            int listIdx = startIdx + slot;
            if (listIdx >= this.availableMateria.size()) break;
            int vx = dx + 13 + slot * 16;
            if (isInside(mx, my, vx - 1, vialY - 1, 16, 22)) {
                setRingMateria(this.focusedRing, this.availableMateria.get(listIdx));
                playRingTickSound(1.3F);
                return true;
            }
        }
        // Mutation Switch
        if (isInside(mx, my, dx + 6, dy + 91, 112, 27)) {
            toggleRingMutation(this.focusedRing);
            return true;
        }
        return true; // Consume clicks on drawer velvet body
    }

    private boolean handleOpenRightDrawerClick(double mx, double my, int btn, int x, int y) {
        // Golden SCRIBE Wax Seal
        if (isInside(mx, my, x + 248, y + 54, 50, 36)) {
            SpellAssembly built = buildDraftAssembly();
            NetworkManager.sendToServer(new ArcaneScribingActionPayload(
                    this.menu.getTablePos(),
                    ArcaneScribingActionPayload.ACTION_INSCRIBE,
                    this.alignSocketIndex,
                    Optional.of(built)
            ));
            this.scribeCeremonyTicks = 36;
            playScribeCeremonySound();
            return true;
        }
        // Mini Utility Seals: [Load] [Copy] [Scrub / S#]
        for (int b = 0; b < 3; b++) {
            int bx = x + 239 + b * 23;
            int by = y + 95;
            if (isInside(mx, my, bx, by, 22, 13)) {
                if (b == 0) {
                    loadFromStudySlot();
                } else if (b == 1) {
                    NetworkManager.sendToServer(new ArcaneScribingActionPayload(
                            this.menu.getTablePos(),
                            ArcaneScribingActionPayload.ACTION_DUPLICATE,
                            this.alignSocketIndex,
                            Optional.empty()
                    ));
                    this.scribeCeremonyTicks = 24;
                    playScribeCeremonySound();
                } else {
                    if (btn == 1) {
                        this.alignSocketIndex = (this.alignSocketIndex + 1) % 9;
                        playClickSound(1.25F);
                    } else {
                        ItemStack studyStack = this.menu.getSlot(2).getItem();
                        int action = (studyStack.getItem() instanceof SpellgemItem && SpellgemItem.isInscribed(studyStack))
                                ? ArcaneScribingActionPayload.ACTION_RECYCLE_GEM
                                : ArcaneScribingActionPayload.ACTION_SAFE_ALIGN;
                        NetworkManager.sendToServer(new ArcaneScribingActionPayload(
                                this.menu.getTablePos(),
                                action,
                                this.alignSocketIndex,
                                Optional.empty()
                        ));
                        playRingTickSound(1.15F);
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        double mx = event.x();
        double my = event.y();

        if (this.draggingLeftDrawer) {
            double delta = mx - this.dragStartMouseX;
            if (Math.abs(delta) > 3.0D) this.dragMovedSignificantly = true;
            // Pulling outward to the left (-delta) opens; pushing rightward (+delta) closes!
            // Also if closed and player drags inward from edge, open intuitively if dragStart < 0.1
            float change = (float) (-delta / 90.0D);
            if (this.dragStartDrawerOpen < 0.05F && delta > 6.0D) {
                change = (float) (delta / 90.0D);
            }
            this.leftDrawerOpen = Mth.clamp(this.dragStartDrawerOpen + change, 0.0F, 1.0F);
            this.leftDrawerTarget = this.leftDrawerOpen;
            return true;
        }

        if (this.draggingRightDrawer) {
            double delta = mx - this.dragStartMouseX;
            if (Math.abs(delta) > 3.0D) this.dragMovedSignificantly = true;
            float change = (float) (delta / 76.0D);
            if (this.dragStartDrawerOpen < 0.05F && delta < -6.0D) {
                change = (float) (-delta / 76.0D);
            }
            this.rightDrawerOpen = Mth.clamp(this.dragStartDrawerOpen + change, 0.0F, 1.0F);
            this.rightDrawerTarget = this.rightDrawerOpen;
            this.menu.setSupplyDrawerExposed(this.rightDrawerOpen > 0.72F);
            return true;
        }

        if (this.draggingRing >= 0 && this.draggingRing <= 2) {
            if (Math.hypot(mx - this.ringClickStartX, my - this.ringClickStartY) > 3.5D) {
                this.dragMovedSignificantly = true;
            }
            int cx = this.leftPos + WHEEL_REL_CX;
            int cy = this.topPos + WHEEL_REL_CY;
            float curAng = (float) Mth.atan2(my - cy, mx - cx);
            float dAng = Mth.wrapDegrees((curAng - this.lastRingDragMouseAngle) * Mth.RAD_TO_DEG) * Mth.DEG_TO_RAD;
            this.lastRingDragMouseAngle = curAng;

            int r = this.draggingRing;
            int count = getRingOptionCount(r);
            if (count > 0) {
                float stepRad = Mth.TWO_PI / count;
                this.ringAngleOffset[r] += dAng;
                while (this.ringAngleOffset[r] >= stepRad * 0.5F) {
                    this.ringAngleOffset[r] -= stepRad;
                    cycleRing(r, -1);
                    playRingTickSound(1.05F + r * 0.12F);
                }
                while (this.ringAngleOffset[r] <= -stepRad * 0.5F) {
                    this.ringAngleOffset[r] += stepRad;
                    cycleRing(r, 1);
                    playRingTickSound(1.05F + r * 0.12F);
                }
            }
            return true;
        }

        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (this.draggingLeftDrawer) {
            this.draggingLeftDrawer = false;
            if (!this.dragMovedSignificantly) {
                this.leftDrawerTarget = this.dragStartDrawerOpen < 0.5F ? 1.0F : 0.0F;
            } else {
                this.leftDrawerTarget = this.leftDrawerOpen >= 0.38F ? 1.0F : 0.0F;
            }
            playWoodenSlideSound(this.leftDrawerTarget > 0.5F ? 1.15F : 0.9F);
            return true;
        }

        if (this.draggingRightDrawer) {
            this.draggingRightDrawer = false;
            if (!this.dragMovedSignificantly) {
                this.rightDrawerTarget = this.dragStartDrawerOpen < 0.5F ? 1.0F : 0.0F;
            } else {
                this.rightDrawerTarget = this.rightDrawerOpen >= 0.38F ? 1.0F : 0.0F;
            }
            playWoodenSlideSound(this.rightDrawerTarget > 0.5F ? 1.15F : 0.9F);
            return true;
        }

        if (this.draggingRing >= 0) {
            int r = this.draggingRing;
            this.draggingRing = -1;
            if (!this.dragMovedSignificantly) {
                // Direct click on a visible symbol around the ring spins it smoothly under the needle!
                int cx = this.leftPos + WHEEL_REL_CX;
                int cy = this.topPos + WHEEL_REL_CY;
                int count = getRingOptionCount(r);
                int selIdx = getRingSelectedIndex(r);
                float midR = r == 0 ? (RING_FLOW_IN + RING_FLOW_OUT) * 0.5F
                        : (r == 1 ? (RING_FORM_IN + RING_FORM_OUT) * 0.5F : (RING_PRIM_IN + RING_PRIM_OUT) * 0.5F);
                float stepRad = Mth.TWO_PI / count;
                for (int i = 0; i < count; i++) {
                    float ang = -Mth.HALF_PI + (i - selIdx) * stepRad;
                    float sx = cx + Mth.cos(ang) * midR;
                    float sy = cy + Mth.sin(ang) * midR;
                    if (Math.hypot(event.x() - sx, event.y() - sy) <= 6.5D) {
                        selectRingIndexAnimated(r, i);
                        break;
                    }
                }
            }
            return true;
        }

        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.editingTitlePopup || scrollY == 0.0D) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        int delta = scrollY > 0 ? -1 : 1;
        int x = this.leftPos;
        int y = this.topPos;

        // Scroll Materia Vials page when hovering open Left Drawer
        if (this.leftDrawerOpen > 0.7F) {
            int dx = x - 38 + Math.round((1.0F - this.leftDrawerOpen) * 124.0F);
            if (isInside(mouseX, mouseY, dx, y + 75, 124, 38) && this.availableMateria.size() > MATERIA_PER_PAGE) {
                int maxPage = (this.availableMateria.size() - 1) / MATERIA_PER_PAGE;
                this.materiaPage = Mth.clamp(this.materiaPage + delta, 0, maxPage);
                playClickSound(1.15F);
                return true;
            }
        }

        // Scroll Potency Tier I..X when hovering Tier box
        if (isInside(mouseX, mouseY, x + 6, y + 158, 63, 17)) {
            this.potencyTier = Mth.clamp(this.potencyTier - delta, 1, 10);
            playClickSound(1.1F);
            return true;
        }

        // Scroll Concentric Symbol Rings directly!
        int cx = x + WHEEL_REL_CX;
        int cy = y + WHEEL_REL_CY;
        float dist = (float) Math.hypot(mouseX - cx, mouseY - cy);
        if (dist <= RING_FLOW_OUT + 3.0F && isInside(mouseX, mouseY, x + 96, y + 31, 124, 109)) {
            int r = dist <= RING_DUAL_OUT ? 3 : (dist <= RING_PRIM_OUT + 1.0F ? 2 : (dist <= RING_FORM_OUT + 1.0F ? 1 : 0));
            this.focusedRing = r;
            int count = Math.max(1, getRingOptionCount(r));
            cycleRing(r, delta);
            if (r < 3) {
                this.ringAngleOffset[r] = delta * (Mth.TWO_PI / count);
            }
            playRingTickSound(1.12F + r * 0.1F);
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (this.editingTitlePopup) {
            if (event.key() == GLFW.GLFW_KEY_ESCAPE || event.key() == GLFW.GLFW_KEY_ENTER) {
                setEditingTitlePopup(false);
                return true;
            }
            if (this.nameBox != null && this.nameBox.isFocused() && this.nameBox.keyPressed(event)) return true;
            if (this.loreBox != null && this.loreBox.isFocused() && this.loreBox.keyPressed(event)) return true;
            return true;
        }
        return super.keyPressed(event);
    }

    private static boolean isInside(double mx, double my, int rx, int ry, int rw, int rh) {
        return mx >= rx && mx < rx + rw && my >= ry && my < ry + rh;
    }

    private static String trimLabel(String text, int maxChars) {
        if (text == null) return "";
        return text.length() <= maxChars ? text : text.substring(0, maxChars - 1) + "…";
    }

    private void playClickSound(float pitch) {
        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.45F, pitch);
        }
    }

    private void playRingTickSound(float pitch) {
        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.playSound(SoundEvents.AMETHYST_BLOCK_STEP, 0.55F, pitch);
        }
    }

    private void playPageTurnSound() {
        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.playSound(SoundEvents.BOOK_PAGE_TURN, 0.7F, 1.15F);
        }
    }

    private void playWoodenSlideSound(float pitch) {
        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.playSound(SoundEvents.CHISELED_BOOKSHELF_PICKUP, 0.65F, pitch);
        }
    }

    private void playScribeCeremonySound() {
        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.playSound(SoundEvents.ENCHANTMENT_TABLE_USE, 0.75F, 1.2F);
        }
    }
}
