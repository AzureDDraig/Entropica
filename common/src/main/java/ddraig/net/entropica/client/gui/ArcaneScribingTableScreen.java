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
import ddraig.net.entropica.spell.SpellTravelDistance;
import ddraig.net.entropica.spell.SpellTriggerType;
import ddraig.net.entropica.spell.apparatus.CastingApparatusState;
import ddraig.net.entropica.spell.apparatus.SocketedSpellgem;
import ddraig.net.entropica.spell.assembly.*;
import ddraig.net.entropica.spell.fuel.MateriaStageTable;
import ddraig.net.entropica.spell.fuel.SpellCostBreakdown;
import ddraig.net.entropica.spell.fuel.SpellFuelCalculator;
import ddraig.net.entropica.spell.mutation.MateriaFamilyTier;
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

    // 6-Zone Concentric Spell Circle geometry relative to (leftPos, topPos)
    // Wheel rings: 0 = Outer Rim Distance Circle, 1 = Trigger Logic, 2 = Flow Modifier,
    //              3 = Delivery Form, 4 = Primary Effect, 5 = Center Dual-Verb Medallion
    private static final int WHEEL_REL_CX = 158;
    private static final int WHEEL_REL_CY = 87;
    private static final float RING_DIST_IN = 47.4F;
    private static final float RING_DIST_OUT = 55.6F;
    private static final float RING_TRIG_IN = 38.9F;
    private static final float RING_TRIG_OUT = 46.6F;
    private static final float RING_FLOW_IN = 30.6F;
    private static final float RING_FLOW_OUT = 38.3F;
    private static final float RING_FORM_IN = 22.3F;
    private static final float RING_FORM_OUT = 30.0F;
    private static final float RING_PRIM_IN = 14.1F;
    private static final float RING_PRIM_OUT = 21.8F;
    private static final float RING_DUAL_OUT = 13.1F;

    private static final class MutableSubSlot {
        SpellDeliveryForm form = SpellDeliveryForm.PROJECTILE;
        EssenceType formMateria = EssenceType.REGULAR;
        boolean formMutation = false;

        SpellCoreEffect primaryEffect = SpellCoreEffect.HARM;
        EssenceType primaryMateria = EssenceType.REGULAR;
        boolean primaryMutation = false;
        String primarySubMode = "default";
        int primaryDialValue = 15;

        @Nullable
        SpellCoreEffect secondaryEffect = null;
        EssenceType secondaryMateria = EssenceType.REGULAR;
        boolean secondaryMutation = false;
        String secondarySubMode = "default";
        int secondaryDialValue = 15;

        SubSpellSlot toImmutable() {
            FormPartBinding formPart = FormPartBinding.of(form, formMateria, formMutation);
            String pMode = primaryEffect == SpellCoreEffect.BLINK ? primarySubMode : "default";
            EffectPartBinding primPart = new EffectPartBinding(primaryEffect, primaryMateria, primaryMutation, pMode, primaryDialValue);
            if (secondaryEffect != null) {
                String sMode = secondaryEffect == SpellCoreEffect.BLINK ? secondarySubMode : "default";
                EffectPartBinding secPart = new EffectPartBinding(secondaryEffect, secondaryMateria, secondaryMutation, sMode, secondaryDialValue);
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
            m.primarySubMode = slot.primaryEffect().subMode();
            m.primaryDialValue = slot.primaryEffect().dialValue();
            if (slot.secondaryEffect().isPresent()) {
                EffectPartBinding sec = slot.secondaryEffect().get();
                m.secondaryEffect = sec.effect();
                m.secondaryMateria = sec.materiaType();
                m.secondaryMutation = sec.mutationEnabled();
                m.secondarySubMode = sec.subMode();
                m.secondaryDialValue = sec.dialValue();
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

    private SpellTravelDistance travelDistance = SpellTravelDistance.DIST_16;
    private SpellTriggerType trigger = SpellTriggerType.ON_ENTITY_HIT;
    private SpellFlowModifier flow = SpellFlowModifier.SINGLE;
    private EssenceType flowMateria = EssenceType.REGULAR;
    private boolean flowMutation = false;
    private int subSpellCount = 1;
    private float flowDelaySeconds = 0.0F;

    private final List<MutableSubSlot> draftSlots = new ArrayList<>();
    private int activeSubSlot = 0;
    private int alignSocketIndex = 0;

    // Materia Focus index (0 = Flow Ring, 1 = Delivery Form Ring, 2 = Primary Effect Ring, 3 = Center Dual-Verb Medallion)
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

    // Smooth Concentric Ring Rotation State (0=Distance, 1=Trigger, 2=Flow, 3=Form, 4=Primary)
    private final float[] ringAngleOffset = new float[5];
    private final float[] stoneGrindGlideAccum = new float[5];
    private int draggingRing = -1;
    private float lastRingDragMouseAngle = 0.0F;
    private float stoneGrindDragAccum = 0.0F;
    private long lastStoneGrindSoundMs = 0L;
    private int stoneGrindGrainVariant = 0;
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
        return Math.clamp(this.subSpellCount, 1, 8);
    }

    private float resolveEffectiveFlowDelaySeconds() {
        if (this.trigger == SpellTriggerType.ON_TIMER) {
            return (this.flowDelaySeconds >= 0.5F && this.flowDelaySeconds <= 3.0F) ? this.flowDelaySeconds : 1.5F;
        }
        if (this.trigger == SpellTriggerType.SELF_TIMED) {
            return (this.flowDelaySeconds == 30.0F || this.flowDelaySeconds == 60.0F || this.flowDelaySeconds == 120.0F)
                    ? this.flowDelaySeconds
                    : 30.0F;
        }
        return 0.0F;
    }

    private void cycleFlowTimerInterval() {
        if (this.trigger == SpellTriggerType.ON_TIMER) {
            float cur = resolveEffectiveFlowDelaySeconds();
            float next = cur + 0.5F;
            this.flowDelaySeconds = next > 3.01F ? 0.5F : Math.round(next * 2.0F) / 2.0F;
            playClickSound(1.25F);
        } else if (this.trigger == SpellTriggerType.SELF_TIMED) {
            float cur = resolveEffectiveFlowDelaySeconds();
            this.flowDelaySeconds = cur < 45.0F ? 60.0F : (cur < 90.0F ? 120.0F : 30.0F);
            playClickSound(1.25F);
        }
    }

    private boolean canSelectVoidWarp() {
        if (!MateriaFamilyTier.hasVoidCreationHeritage(this.flowMateria)) {
            return false;
        }
        String warpNode = SpellResearchManager.nodeId(SpellFlowModifier.VOID_WARP);
        Player player = Minecraft.getInstance().player;
        return player == null || (SpellResearchManager.isNodeUnlocked(player, warpNode)
                && SpellResearchManager.isMutationUnlocked(player, warpNode, this.flowMateria, true));
    }

    private boolean isVoidWarpActive() {
        return canSelectVoidWarp() && this.flowMutation;
    }

    private void focusMateriaRingForWheel(int wheelRing) {
        if (wheelRing >= 2 && wheelRing <= 5) {
            this.focusedRing = wheelRing - 2;
        }
    }

    private float getWheelBandMidRadius(int wheelRing) {
        return switch (wheelRing) {
            case 0 -> (RING_DIST_IN + RING_DIST_OUT) * 0.5F;
            case 1 -> (RING_TRIG_IN + RING_TRIG_OUT) * 0.5F;
            case 2 -> (RING_FLOW_IN + RING_FLOW_OUT) * 0.5F;
            case 3 -> (RING_FORM_IN + RING_FORM_OUT) * 0.5F;
            default -> (RING_PRIM_IN + RING_PRIM_OUT) * 0.5F;
        };
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

    private void cycleWheelRing(int wheelRing, int delta) {
        switch (wheelRing) {
            case 0 -> {
                SpellTravelDistance[] vals = SpellTravelDistance.values();
                int next = Math.floorMod(this.travelDistance.ordinal() + delta, vals.length);
                this.travelDistance = vals[next];
            }
            case 1 -> {
                SpellTriggerType[] vals = SpellTriggerType.values();
                int next = Math.floorMod(this.trigger.ordinal() + delta, vals.length);
                this.trigger = vals[next];
            }
            case 2 -> {
                SpellFlowModifier[] vals = SpellFlowModifier.values();
                int step = delta >= 0 ? 1 : -1;
                int stepsToMove = Math.max(1, Math.abs(delta));
                int cur = this.flow.ordinal();
                for (int s = 0; s < stepsToMove; s++) {
                    for (int guard = 0; guard < vals.length; guard++) {
                        cur = Math.floorMod(cur + step, vals.length);
                        if (vals[cur] != SpellFlowModifier.VOID_WARP || canSelectVoidWarp()) {
                            break;
                        }
                    }
                }
                this.flow = vals[cur];
                if (this.flow == SpellFlowModifier.VOID_WARP && canSelectVoidWarp()) {
                    this.flowMutation = true;
                    this.materiaSpecific = true;
                }
                if (this.activeSubSlot >= getActiveSlotCount()) {
                    this.activeSubSlot = getActiveSlotCount() - 1;
                }
            }
            case 3 -> {
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
            case 4 -> {
                List<SpellCoreEffect> allowed = allowedEffects();
                MutableSubSlot s = currentSub();
                int cur = Math.max(0, allowed.indexOf(s.primaryEffect));
                s.primaryEffect = allowed.get(Math.floorMod(cur + delta, allowed.size()));
            }
            case 5 -> {
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

    private int getWheelRingOptionCount(int wheelRing) {
        return switch (wheelRing) {
            case 0 -> SpellTravelDistance.values().length;
            case 1 -> SpellTriggerType.values().length;
            case 2 -> SpellFlowModifier.values().length;
            case 3 -> SpellDeliveryForm.values().length;
            case 4 -> allowedEffects().size();
            default -> allowedEffects().size() + 1;
        };
    }

    private int getWheelRingSelectedIndex(int wheelRing) {
        MutableSubSlot s = currentSub();
        return switch (wheelRing) {
            case 0 -> this.travelDistance.ordinal();
            case 1 -> this.trigger.ordinal();
            case 2 -> this.flow.ordinal();
            case 3 -> s.form.ordinal();
            case 4 -> Math.max(0, allowedEffects().indexOf(s.primaryEffect));
            default -> s.secondaryEffect == null ? 0 : (allowedEffects().indexOf(s.secondaryEffect) + 1);
        };
    }

    private void selectWheelRingIndexAnimated(int wheelRing, int targetIdx) {
        int count = getWheelRingOptionCount(wheelRing);
        if (count <= 0) return;

        if (wheelRing == 2 && SpellFlowModifier.values()[targetIdx] == SpellFlowModifier.VOID_WARP && !canSelectVoidWarp()) {
            this.focusedRing = 0;
            if (this.leftDrawerTarget < 0.5F) {
                this.leftDrawerTarget = 1.0F;
            }
            playClickSound(0.65F);
            return;
        }

        int cur = getWheelRingSelectedIndex(wheelRing);
        if (cur == targetIdx) return;

        int diff = targetIdx - cur;
        if (diff > count / 2) diff -= count;
        if (diff < -count / 2) diff += count;

        float stepRad = Mth.TWO_PI / count;
        if (wheelRing == 2) {
            this.flow = SpellFlowModifier.values()[targetIdx];
            if (this.flow == SpellFlowModifier.VOID_WARP && canSelectVoidWarp()) {
                this.flowMutation = true;
                this.materiaSpecific = true;
            }
            normalizeDraftForPrefixAndResearch();
        } else {
            cycleWheelRing(wheelRing, diff);
        }
        if (wheelRing >= 0 && wheelRing <= 4) {
            this.ringAngleOffset[wheelRing] = diff * stepRad;
            this.stoneGrindGlideAccum[wheelRing] = 0.0F;
        }
        if (Math.abs(diff) > 1) {
            playStoneCircleGrindSound(wheelRing, 0.75F);
            this.lastStoneGrindSoundMs = System.currentTimeMillis();
        } else {
            playStoneCircleDetentSound(wheelRing);
        }
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
        if (this.flow == SpellFlowModifier.VOID_WARP
                && (!this.flowMutation || !MateriaFamilyTier.hasVoidCreationHeritage(this.flowMateria))) {
            this.flow = SpellFlowModifier.SINGLE;
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
        this.travelDistance = found.flowPart().distanceStep();
        this.trigger = found.flowPart().trigger();
        this.flow = found.flowPart().flow();
        this.flowMateria = found.flowPart().materiaType();
        this.flowMutation = found.flowPart().mutationEnabled();
        this.subSpellCount = Math.clamp(Math.max(found.subSpells().size(), found.flowPart().subSpellCount()), 1, 8);
        this.flowDelaySeconds = found.flowPart().delaySeconds();

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
        FlowPartBinding flowBinding = new FlowPartBinding(
                this.flow, this.trigger, this.flowMateria, this.flowMutation, count,
                resolveEffectiveFlowDelaySeconds(), this.travelDistance.meters()
        );
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
        long nowMs = System.currentTimeMillis();
        for (int r = 0; r < 5; r++) {
            if (this.draggingRing != r) {
                float prev = this.ringAngleOffset[r];
                this.ringAngleOffset[r] *= 0.72F;
                float moved = Math.abs(prev - this.ringAngleOffset[r]);
                if (Math.abs(this.ringAngleOffset[r]) > 0.06F) {
                    this.stoneGrindGlideAccum[r] += moved;
                    if (this.stoneGrindGlideAccum[r] >= 0.20F && nowMs - this.lastStoneGrindSoundMs >= 78L) {
                        this.stoneGrindGlideAccum[r] = 0.0F;
                        this.lastStoneGrindSoundMs = nowMs;
                        playStoneCircleGrindSound(r, Mth.clamp(moved / 0.32F, 0.15F, 0.80F));
                    }
                } else if (prev > 0.06F || prev < -0.06F) {
                    // Soft carved-stone settle as an animated spin locks under the needle
                    if (nowMs - this.lastStoneGrindSoundMs >= 55L) {
                        this.lastStoneGrindSoundMs = nowMs;
                        playStoneCircleDetentSound(r);
                    }
                    this.stoneGrindGlideAccum[r] = 0.0F;
                }
                if (Math.abs(this.ringAngleOffset[r]) < 0.003F) {
                    this.ringAngleOffset[r] = 0.0F;
                    this.stoneGrindGlideAccum[r] = 0.0F;
                }
            }
        }
    }

    private void renderTabletopChassis(GuiGraphics g, int x, int y, int mouseX, int mouseY) {
        int deskW = this.imageWidth;
        int deskH = 154;

        // Dark walnut wooden tabletop + fine sub-pixel plank seams & bevels
        ArcaneScribingGlyphRenderer.fillHiRes(g, x, y + 6.0F, x + deskW, y + deskH, 0xFF1E120B);
        ArcaneScribingGlyphRenderer.fillHiRes(g, x + 1.25F, y + 7.25F, x + deskW - 1.25F, y + deskH - 1.25F, 0xFF2B1B12);
        for (float py = y + 19.5F; py < y + deskH - 8.0F; py += 14.5F) {
            ArcaneScribingGlyphRenderer.fillHiRes(g, x + 5.0F, py, x + deskW - 5.0F, py + 0.75F, 0xFF1A0F09);
            ArcaneScribingGlyphRenderer.fillHiRes(g, x + 5.0F, py + 0.75F, x + deskW - 5.0F, py + 1.25F, 0x443E271A);
        }

        // Top & Bottom Cyan Arcanite + Beveled Brass Trim Rails
        ArcaneScribingGlyphRenderer.fillHiRes(g, x + 13.0F, y + 6.0F, x + deskW - 13.0F, y + 8.75F, 0xFF9E7B23);
        ArcaneScribingGlyphRenderer.fillHiRes(g, x + 14.0F, y + 6.25F, x + deskW - 14.0F, y + 8.25F, 0xFFD4AF37);
        ArcaneScribingGlyphRenderer.fillHiRes(g, x + 23.0F, y + 6.75F, x + 82.0F, y + 7.75F, 0xFF2CE6FF);
        ArcaneScribingGlyphRenderer.fillHiRes(g, x + 234.0F, y + 6.75F, x + deskW - 23.0F, y + 7.75F, 0xFF2CE6FF);

        ArcaneScribingGlyphRenderer.fillHiRes(g, x + 13.0F, y + deskH - 3.75F, x + deskW - 13.0F, y + deskH - 1.0F, 0xFF9E7B23);
        ArcaneScribingGlyphRenderer.fillHiRes(g, x + 14.0F, y + deskH - 3.5F, x + deskW - 14.0F, y + deskH - 1.5F, 0xFFD4AF37);
        ArcaneScribingGlyphRenderer.fillHiRes(g, x + 22.0F, y + deskH - 2.75F, x + 82.0F, y + deskH - 1.75F, 0xFF2CE6FF);
        ArcaneScribingGlyphRenderer.fillHiRes(g, x + 234.0F, y + deskH - 2.75F, x + deskW - 22.0F, y + deskH - 1.75F, 0xFF2CE6FF);

        // 4 High-Res Sculpted Gilded Brass Corner Brackets
        ArcaneScribingGlyphRenderer.drawCornerBracket(g, x, y + 4.0F, 16.0F, true, true);
        ArcaneScribingGlyphRenderer.drawCornerBracket(g, x + deskW - 16.0F, y + 4.0F, 16.0F, false, true);
        ArcaneScribingGlyphRenderer.drawCornerBracket(g, x, y + deskH - 16.0F, 16.0F, true, false);
        ArcaneScribingGlyphRenderer.drawCornerBracket(g, x + deskW - 16.0F, y + deskH - 16.0F, 16.0F, false, false);

        // Glowing Cyan Arcanite Conduits feeding from left & right bays into the Central Scroll
        drawArcaniteConduitBridge(g, x + 68.0F, y + 79.5F, 18.0F);
        drawArcaniteConduitBridge(g, x + 230.0F, y + 79.5F, 14.0F);

        // Top Inkwell & Cryptic Tome Shelf (x = 86..230, y = 0..22)
        ArcaneScribingGlyphRenderer.drawBeveledBox(g, x + 86.0F, y, 144.0F, 22.5F, 0xFF1A100A, 0xFFB8922A, 0x55FCE38A);
        ArcaneScribingGlyphRenderer.fillHiRes(g, x + 86.0F, y + 20.5F, x + 230.0F, y + 22.75F, 0xFFD4AF37);
        ArcaneScribingGlyphRenderer.fillHiRes(g, x + 92.0F, y + 21.25F, x + 224.0F, y + 22.0F, 0xFF2CE6FF);

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
        ArcaneScribingGlyphRenderer.drawCrypticPedestal(g, px, py, this.editingTitlePopup, pedHover);
        if (pedHover) {
            this.pendingTooltip = List.of(
                    Component.literal("§6Incantation Title & Lore Seal"),
                    Component.literal("§7Current Name: §f" + (this.customName.isBlank() ? "Unnamed Incantation" : this.customName)),
                    Component.literal("§8• Click to edit custom Spell Name & Lore inscription")
            );
        }
    }

    private void drawArcaniteConduitBridge(GuiGraphics g, float bx, float by, float bw) {
        ArcaneScribingGlyphRenderer.fillHiRes(g, bx, by, bx + bw, by + 13.0F, 0xFF8C6B1F);
        ArcaneScribingGlyphRenderer.fillHiRes(g, bx, by + 0.75F, bx + bw, by + 12.25F, 0xFFD4AF37);
        ArcaneScribingGlyphRenderer.fillHiRes(g, bx, by + 2.25F, bx + bw, by + 5.25F, 0xFF0EA5E9);
        ArcaneScribingGlyphRenderer.fillHiRes(g, bx, by + 2.75F, bx + bw, by + 4.5F, 0xFF67E8F9);
        ArcaneScribingGlyphRenderer.fillHiRes(g, bx, by + 7.75F, bx + bw, by + 10.75F, 0xFF0EA5E9);
        ArcaneScribingGlyphRenderer.fillHiRes(g, bx, by + 8.25F, bx + bw, by + 10.0F, 0xFF67E8F9);
    }

    private void renderLeftWingAndDrawer(GuiGraphics g, int x, int y, int mouseX, int mouseY) {
        // 1. High-Res Open Arcane Grimoire on the left desk bay when the drawer isn't fully covering it
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
            ArcaneScribingGlyphRenderer.drawDrawerHandle(g, hx, hy, 8.5F, 40.0F, hHover);
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

        // High-res Brass Handle attached to the outer left edge of the sliding drawer
        int hx = dx - 9;
        int hy = dy + 39;
        boolean handleHover = isInside(mouseX, mouseY, hx - 2, hy - 2, 11, 46);
        ArcaneScribingGlyphRenderer.drawDrawerHandle(g, hx, hy, 9.0F, 42.0F, handleHover);
        if (handleHover) {
            this.pendingTooltip = List.of(
                    Component.literal("§6Left Drawer Brass Handle"),
                    Component.literal("§7Drag or click to tuck the drawer back into the desk.")
            );
        }

        // Clip drawer body cleanly at the Central Scroll Frame edge (x + 86) so it slides out from inside!
        g.enableScissor(dx - 2, dy - 3, x + 86, dy + dh + 3);

        // Top & bottom beveled golden brass drawer slide rails + royal purple velvet lining
        ArcaneScribingGlyphRenderer.fillHiRes(g, dx, dy, dx + drawerW, dy + dh, 0xFF8C6B1F);
        ArcaneScribingGlyphRenderer.fillHiRes(g, dx + 0.5F, dy + 0.5F, dx + drawerW, dy + 3.5F, 0xFFD4AF37);
        ArcaneScribingGlyphRenderer.fillHiRes(g, dx + 0.5F, dy + dh - 3.5F, dx + drawerW, dy + dh - 0.5F, 0xFFD4AF37);
        ArcaneScribingGlyphRenderer.fillHiRes(g, dx + 1.0F, dy + 3.5F, dx + drawerW, dy + dh - 3.5F, 0xFF1F100B);
        ArcaneScribingGlyphRenderer.fillHiRes(g, dx + 3.5F, dy + 4.5F, dx + drawerW - 1.5F, dy + dh - 4.5F, 0xFF281047);
        ArcaneScribingGlyphRenderer.fillHiRes(g, dx + 4.25F, dy + 5.25F, dx + drawerW - 2.25F, dy + dh - 5.25F, 0xFF2E1352);

        // --- Compartment A (dy + 7 .. dy + 47): Discipline Wax Seals + Lock Seal + Sub-Spell Tabs I..VIII ---
        int compAY = dy + 6;
        int[] sealColors = {0xB91C1C, 0x0284C7, 0xD97706, this.materiaSpecific ? 0x9333EA : 0x475569};
        for (int s = 0; s < 4; s++) {
            int cardX = dx + 6 + s * 28;
            boolean activeSeal = (s < 3) ? (this.prefix == CRAFTABLE_PREFIXES[s]) : this.materiaSpecific;
            boolean hoverSeal = this.leftDrawerOpen > 0.7F && isInside(mouseX, mouseY, cardX, compAY, 26, 28);
            int parchmentFill = activeSeal ? 0xFFF6E8CD : (hoverSeal ? 0xFFEBDCBE : 0xFFD8C39E);
            int parchmentRim = activeSeal ? 0xFFD4AF37 : 0xFF9C7E50;
            ArcaneScribingGlyphRenderer.drawBeveledBox(g, cardX, compAY, 26.0F, 26.5F, parchmentFill, parchmentRim, 0x88FFFDF7);
            ArcaneScribingGlyphRenderer.drawWaxSeal(g, cardX + 13.0F, compAY + 13.5F, 8.2F, sealColors[s], activeSeal, hoverSeal, s);
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

        // Sub-Spell Roman Parchment Tabs [I .. VIII] — directly controls Single (1x) to Octuple (8x) Cast!
        int tabY = compAY + 28;
        int slotCount = getActiveSlotCount();
        for (int i = 0; i < 8; i++) {
            float tx = dx + 6.0F + i * 14.0F;
            boolean enabledTab = i < slotCount;
            boolean selectedTab = i == this.activeSubSlot;
            boolean hoverTab = this.leftDrawerOpen > 0.7F && isInside(mouseX, mouseY, (int) tx, tabY, 13, 11);
            int tabBg = selectedTab ? 0xFFFDE68A : (enabledTab ? (hoverTab ? 0xFFF3E5C8 : 0xFFE5D1A7) : (hoverTab ? 0xFF361D5C : 0xFF23133D));
            int tabRim = selectedTab ? 0xFFD4AF37 : (enabledTab ? 0xFFA68A5B : (hoverTab ? 0xFF8B5CF6 : 0xFF3E2663));
            ArcaneScribingGlyphRenderer.drawBeveledBox(g, tx, tabY, 13.0F, 11.0F, tabBg, tabRim, 0x44FFFFFF);
            int txtCol = selectedTab ? 0xFF1E1B4B : (enabledTab ? 0xFF3E2723 : (hoverTab ? 0xFFE9D5FF : 0xFF8B7AA8));
            ArcaneScribingGlyphRenderer.drawScaledCenteredString(g, this.font, ROMAN[i], tx + 6.5F, tabY + 3.0F, 0.66F, txtCol, false);
            if (hoverTab) {
                String castMode = SpellFlowModifier.getCastCountTitle(slotCount);
                String targetMode = this.trigger.isSelfCentered()
                        ? "centered on yourself"
                        : "at each payload's point of contact";
                this.pendingTooltip = List.of(
                        Component.literal("§6Spell Slot " + ROMAN[i] + " §8• §d" + castMode),
                        Component.literal(enabledTab
                                ? "§7Active! All §f" + slotCount + "§7 slotted spells cast " + targetMode + "."
                                : "§7Left-click to expand to §d" + SpellFlowModifier.getCastCountTitle(i + 1) + "§7!"),
                        Component.literal("§8• Left-click: Select / Expand (I–VIII) • Right-click or Scroll: Shrink")
                );
            }
        }

        // Fine gilded velvet shelf divider
        ArcaneScribingGlyphRenderer.fillHiRes(g, dx + 5.0F, dy + 48.75F, dx + drawerW - 3.0F, dy + 49.5F, 0x88D4AF37);
        ArcaneScribingGlyphRenderer.fillHiRes(g, dx + 5.0F, dy + 49.5F, dx + drawerW - 3.0F, dy + 50.25F, 0xFF1A0933);

        // --- Compartment B (dy + 52 .. dy + 89): Ring Focus Pills + High-Res Materia Vials & Shards ---
        boolean hasTimerDial = this.trigger.hasTimerDial();
        String flowLabel = hasTimerDial
                ? (this.trigger == SpellTriggerType.ON_TIMER
                        ? String.format("%.1fs", resolveEffectiveFlowDelaySeconds())
                        : String.format("%.0fs", resolveEffectiveFlowDelaySeconds()))
                : "Flow";
        String[] pillLabels = {flowLabel, "Form", "Effect", "+Verb"};
        for (int r = 0; r < 4; r++) {
            float px = dx + 6.0F + r * 28.0F;
            float py = dy + 52.5F;
            boolean sel = this.focusedRing == r;
            boolean hov = this.leftDrawerOpen > 0.7F && isInside(mouseX, mouseY, (int) px, (int) py, 26, 11);
            int pillFill = sel ? 0xFFD4AF37 : (hov ? 0xFF4C1D95 : 0xFF1B0A33);
            int pillRim = sel ? 0xFFFEF08A : (hov ? 0xFFA855F7 : 0xFF58337A);
            ArcaneScribingGlyphRenderer.drawBeveledBox(g, px, py, 26.0F, 10.5F, pillFill, pillRim, 0x44FFFFFF);
            ArcaneScribingGlyphRenderer.drawScaledCenteredString(
                    g, this.font, pillLabels[r], px + 13.0F, py + 2.5F, 0.68F, sel ? 0xFF111827 : 0xFFF1F5F9, !sel
            );
            if (hov && r == 0 && hasTimerDial) {
                this.pendingTooltip = List.of(
                        Component.literal("§6Flow Focus §8• §eTrigger Timer: " + flowLabel),
                        Component.literal("§7Left-click to focus Flow Ring • Right-click here or on the Outer Trigger Ring to tune timer!")
                );
            }
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
            boolean shardStyle = listIdx >= 2;
            ArcaneScribingGlyphRenderer.drawMateriaVialOrShard(
                    g, vx, vialY, rgb, shardStyle, currentRingMat == mat, hov, mat == EssenceType.REGULAR
            );
            if (hov) {
                this.pendingTooltip = buildMateriaVialTooltip(mat);
            }
        }
        if (this.availableMateria.size() > MATERIA_PER_PAGE) {
            boolean canPrev = this.materiaPage > 0;
            boolean canNext = (this.materiaPage + 1) * MATERIA_PER_PAGE < this.availableMateria.size();
            ArcaneScribingGlyphRenderer.drawScaledCenteredString(g, this.font, "◀", dx + 7.5F, vialY + 7.0F, 0.72F, canPrev ? 0xFFFDE047 : 0xFF6B7280, true);
            ArcaneScribingGlyphRenderer.drawScaledCenteredString(g, this.font, "▶", dx + drawerW - 7.5F, vialY + 7.0F, 0.72F, canNext ? 0xFFFDE047 : 0xFF6B7280, true);
        }

        // Fine gilded velvet shelf divider
        ArcaneScribingGlyphRenderer.fillHiRes(g, dx + 5.0F, dy + 88.0F, dx + drawerW - 3.0F, dy + 88.75F, 0x88D4AF37);
        ArcaneScribingGlyphRenderer.fillHiRes(g, dx + 5.0F, dy + 88.75F, dx + drawerW - 3.0F, dy + 89.5F, 0xFF1A0933);

        // --- Compartment C (dy + 91 .. dy + 118): High-Res Brass & Crystal Mutation Switch (State 2) ---
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

        // Gilded brass cradle & royal purple velvet recess + glowing cyan center conduit
        ArcaneScribingGlyphRenderer.drawBeveledBox(g, rx, ry, rw, rh, 0xFF2A114B, 0xFFD4AF37, 0x66FEF08A);
        ArcaneScribingGlyphRenderer.fillHiRes(g, rx + 35.5F, ry + 3.0F, rx + 38.5F, ry + rh - 3.0F, 0xFF0EA5E9);
        ArcaneScribingGlyphRenderer.fillHiRes(g, rx + 36.25F, ry + 3.0F, rx + 37.75F, ry + rh - 3.0F, 0xFF67E8F9);

        // 4 Workstation Slot Cradles (matching ArcaneScribingTableMenu positions!)
        drawWorkstationSlotCradle(g, x + 245, y + 33, 0, mouseX, mouseY); // Blank Parchment
        drawWorkstationSlotCradle(g, x + 281, y + 33, 1, mouseX, mouseY); // Scribing Ink
        drawWorkstationSlotCradle(g, x + 245, y + 115, 2, mouseX, mouseY); // Study / Apparatus
        drawWorkstationSlotCradle(g, x + 281, y + 115, 3, mouseX, mouseY); // Output Cradle

        // Golden Halo around Output Cradle when an output item is ready or Scribe flash is active (State 4!)
        if (this.menu.getSlot(3).hasItem() || this.scribeCeremonyTicks > 0) {
            ArcaneScribingGlyphRenderer.drawSelectionHalo(g, x + 290.0F, y + 124.0F, 26.0F, true);
        }

        // Central High-Res Golden Wax Seal "SCRIBE" Plaque (x + 246..300, y + 53..91)
        float sealCx = x + 273.0F;
        float sealCy = y + 67.5F;
        boolean hasSupplies = this.menu.getSlot(0).hasItem() && this.menu.getSlot(1).hasItem();
        boolean scribeHover = this.rightDrawerOpen > 0.72F && isInside(mouseX, mouseY, x + 246, y + 53, 54, 38);
        ArcaneScribingGlyphRenderer.drawBeveledBox(
                g, x + 246.5F, y + 53.5F, 53.0F, 37.5F,
                scribeHover ? 0xFF241145 : 0xFF190B30,
                (scribeHover || hasSupplies) ? 0xFFFDE047 : 0xFFB8922A,
                0x44FFFFFF
        );
        ArcaneScribingGlyphRenderer.drawWaxSeal(g, sealCx, sealCy, 10.5F, 0xEAB308, hasSupplies || this.scribeCeremonyTicks > 0, scribeHover, 4);
        ArcaneScribingGlyphRenderer.drawScaledCenteredString(
                g, this.font, "SCRIBE", sealCx, y + 81.5F, 0.76F,
                scribeHover || hasSupplies ? 0xFFFDE047 : 0xFFD4AF37, true
        );
        if (scribeHover) {
            SpellAssembly preview = buildDraftAssembly();
            SpellCostBreakdown cost = SpellFuelCalculator.calculateCastCost(preview, null, 0.0F, EssenceType.REGULAR);
            this.pendingTooltip = List.of(
                    Component.literal("§6✦ Inscribe Spell Parchment ✦"),
                    Component.literal("§7Spell: §f" + preview.customName() + " §8(Tier " + ROMAN[this.potencyTier - 1] + " " + this.prefix.getDisplayName() + ")"),
                    Component.literal("§7Requires: §f1 Blank Parchment §7+ §b1 Arcane Scribing Ink"),
                    Component.literal((cost.channeledPerSecond() ? "§7Channeled Drain: §e" : "§7Cast Fuel: §e") + cost.formatTypeCostSummary()),
                    Component.literal(hasSupplies ? "§a• Click the Golden Wax Seal to inscribe!" : "§c• Load Blank Parchment & Scribing Ink above first")
            );
        }

        // Crisp Gilded Utility Strip (y + 95..108): [Load]  [Copy]  [Scrub / S#] with clean spacing!
        ItemStack studyStack = this.menu.getSlot(2).getItem();
        boolean isInscribedGem = studyStack.getItem() instanceof SpellgemItem && SpellgemItem.isInscribed(studyStack);
        String[] miniBtns = {"Load", "Copy", isInscribedGem ? "Scrub" : ("S#" + (this.alignSocketIndex + 1))};
        for (int b = 0; b < 3; b++) {
            float bx = x + 239.0F + b * 23.5F;
            float by = y + 95.0F;
            boolean hov = this.rightDrawerOpen > 0.72F && isInside(mouseX, mouseY, (int) bx, (int) by, 21, 13);
            ArcaneScribingGlyphRenderer.drawBeveledBox(
                    g, bx, by, 21.0F, 12.5F,
                    hov ? 0xFF3B1B68 : 0xFF1A0B33,
                    hov ? 0xFFFDE047 : 0xFFAA8628,
                    0x44FFFFFF
            );
            ArcaneScribingGlyphRenderer.drawScaledCenteredString(
                    g, this.font, miniBtns[b], bx + 10.5F, by + 3.5F, 0.68F,
                    hov ? 0xFFFDE047 : 0xFFE2E8F0, true
            );
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
        ArcaneScribingGlyphRenderer.drawBeveledBox(
                g, sx, sy, 18.0F, 18.0F,
                0xFF140A26,
                idx == 3 ? 0xFFFDE047 : 0xFFD4AF37,
                0x44FFFFFF
        );
        if (!this.menu.getSlot(idx).hasItem()) {
            switch (idx) {
                case 0 -> ArcaneScribingGlyphRenderer.blitRect(
                        g, ArcaneScribingGlyphRenderer.TEX_SCROLL_PARCHMENT, 512, 448,
                        sx + 3.0F, sy + 3.5F, 12.0F, 11.0F, 0x55EAD8B1
                );
                case 1 -> ArcaneScribingGlyphRenderer.blitRect(
                        g, ArcaneScribingGlyphRenderer.TEX_INKWELL_GLASS, 128, 128,
                        sx + 3.0F, sy + 2.5F, 12.0F, 13.0F, 0x6667E8F9
                );
                case 2 -> ArcaneScribingGlyphRenderer.blitCentered(
                        g, ArcaneScribingGlyphRenderer.TEX_RUNE_EMPTY_CORE, 128, 128,
                        sx + 9.0F, sy + 9.0F, 11.0F, 11.0F, 0.0F, 0x66C084FC
                );
                default -> ArcaneScribingGlyphRenderer.blitCentered(
                        g, ArcaneScribingGlyphRenderer.TEX_WAX_SEAL_ICON[4], 128, 128,
                        sx + 9.0F, sy + 9.0F, 11.5F, 11.5F, 0.0F, 0x66FDE047
                );
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
        ArcaneScribingGlyphRenderer.drawBeveledBox(g, cx, cy, cw, ch, 0xFF23160F, 0xFF9E7B23, 0x44FCE38A);
        ArcaneScribingGlyphRenderer.drawBeveledBox(g, cx + 3.5F, cy + 3.5F, cw - 7.0F, ch - 7.0F, 0xFF291147, 0xFFD4AF37, 0x44FFFFFF);
        ArcaneScribingGlyphRenderer.fillHiRes(g, cx + 4.0F, cy + 42.0F, cx + 6.5F, cy + 82.0F, 0xFF2CE6FF);
        ArcaneScribingGlyphRenderer.fillHiRes(g, cx + cw - 6.5F, cy + 42.0F, cx + cw - 4.0F, cy + 82.0F, 0xFF2CE6FF);

        // High-res Arcanite Medallion Plaque in center of cover
        float ex = cx + 18.0F;
        float ey = cy + 38.0F;
        ArcaneScribingGlyphRenderer.drawBeveledBox(g, ex, ey, 38.0F, 48.0F, 0xFF1F1135, 0xFFD4AF37, 0x55FFFBEB);
        ArcaneScribingGlyphRenderer.blitCentered(
                g, ArcaneScribingGlyphRenderer.TEX_MATERIA_SHARD, 128, 128,
                ex + 19.0F, ey + 24.0F, 24.0F, 32.0F, 0.0F, 0xFF38E8FF
        );
        g.disableScissor();

        // High-res Right Brass Pull-Handle
        int hx = Math.min(x + 346, x + 314 + Math.round(this.rightDrawerOpen * 32.0F));
        int hy = y + 64;
        boolean hov = isInside(mouseX, mouseY, x + 236, cy, 88, ch) || isInside(mouseX, mouseY, hx - 2, hy - 2, 12, 44);
        ArcaneScribingGlyphRenderer.drawDrawerHandle(g, hx, hy, 8.5F, 40.0F, hov);

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
        ArcaneScribingGlyphRenderer.drawDrawerHandle(g, hx, hy, 8.5F, 40.0F, hov);
        if (hov) {
            this.pendingTooltip = List.of(
                    Component.literal("§6Right Supply Drawer Handle"),
                    Component.literal("§7Click or drag leftward to close the Supply Cover.")
            );
        }
    }

    // =========================================================================
    // CENTRAL HIGH-RESOLUTION UNROLLED PARCHMENT & CONCENTRIC ROTATING RINGS
    // =========================================================================

    private void renderCentralScrollAndRings(GuiGraphics g, int x, int y, int mouseX, int mouseY, float partialTick) {
        int fx = x + 84;
        int fy = y + 23;
        int fw = 148;
        int fh = 129;

        // Beveled gilded outer frame with cyan Arcanite inlay dashes
        ArcaneScribingGlyphRenderer.drawBeveledBox(g, fx, fy, fw, fh, 0xFFD4AF37, 0xFF8C6B1F, 0x88FEF08A);
        for (float sx = fx + 8.5F; sx < fx + fw - 8.5F; sx += 13.5F) {
            ArcaneScribingGlyphRenderer.fillHiRes(g, sx, fy + fh - 5.25F, sx + 5.5F, fy + fh - 1.5F, 0xFF0EA5E9);
            ArcaneScribingGlyphRenderer.fillHiRes(g, sx + 0.5F, fy + fh - 4.75F, sx + 5.0F, fy + fh - 2.0F, 0xFF67E8F9);
        }
        ArcaneScribingGlyphRenderer.fillHiRes(g, fx + 4.5F, fy + 4.5F, fx + fw - 4.5F, fy + fh - 6.5F, 0xFF261044);

        // High-Resolution 512x448 Unrolled Vellum Parchment Scroll + 3D Turned Wooden Scroll Rollers
        int px = fx + 8;
        int py = fy + 5;
        int pw = fw - 16; // 132
        int ph = fh - 12; // 117
        ArcaneScribingGlyphRenderer.drawParchmentScroll(g, px, py, pw, ph);

        int cx = x + WHEEL_REL_CX;
        int cy = y + WHEEL_REL_CY;

        float dx = mouseX - cx;
        float dy = mouseY - cy;
        float dist = Mth.sqrt(dx * dx + dy * dy);
        int hoveredRing = -1;
        if (!this.editingTitlePopup && isInside(mouseX, mouseY, px, py, pw, ph)) {
            if (dist <= RING_DUAL_OUT) hoveredRing = 5;
            else if (dist <= RING_PRIM_OUT + 0.5F) hoveredRing = 4;
            else if (dist <= RING_FORM_OUT + 0.5F) hoveredRing = 3;
            else if (dist <= RING_FLOW_OUT + 0.5F) hoveredRing = 2;
            else if (dist <= RING_TRIG_OUT + 0.5F) hoveredRing = 1;
            else if (dist <= RING_DIST_OUT + 2.5F) hoveredRing = 0;
        }

        boolean ceremony = this.scribeCeremonyTicks > 0;
        int trigRgb = 0x67E8F9;
        int flowRgb = resolveEssenceRgb(this.flowMateria, 0x38BDF8);
        int formRgb = resolveEssenceRgb(currentSub().formMateria, 0x38BDF8);
        int primRgb = resolveEssenceRgb(currentSub().primaryMateria, 0x38BDF8);
        int dualRgb = resolveEssenceRgb(currentSub().secondaryMateria, 0xFACC15);

        // Outermost Ring 0: Astrolabe Travel Distance Circle (0m -> 8m -> 16m -> 24m -> 32m -> 48m -> 64m -> 96m -> 128m)
        ArcaneScribingGlyphRenderer.drawOuterDistanceBand(
                g, cx, cy, this.ringAngleOffset[0], hoveredRing == 0 || this.draggingRing == 0, ceremony
        );

        // Inner 4 Concentric Ring Bands & Tintable Metallic Rims (1=Trigger, 2=Flow, 3=Form, 4=Primary)
        ArcaneScribingGlyphRenderer.drawWheelBand(
                g, 0, cx, cy, this.ringAngleOffset[1], trigRgb,
                false, hoveredRing == 1 || this.draggingRing == 1, ceremony
        );
        ArcaneScribingGlyphRenderer.drawWheelBand(
                g, 1, cx, cy, this.ringAngleOffset[2], flowRgb,
                this.focusedRing == 0, hoveredRing == 2 || this.draggingRing == 2, ceremony
        );
        ArcaneScribingGlyphRenderer.drawWheelBand(
                g, 2, cx, cy, this.ringAngleOffset[3], formRgb,
                this.focusedRing == 1, hoveredRing == 3 || this.draggingRing == 3, ceremony
        );
        ArcaneScribingGlyphRenderer.drawWheelBand(
                g, 3, cx, cy, this.ringAngleOffset[4], primRgb,
                this.focusedRing == 2, hoveredRing == 4 || this.draggingRing == 4, ceremony
        );

        // High-Resolution 256x256 Center Dual-Verb Medallion (wheelRing = 5)
        ArcaneScribingGlyphRenderer.drawWheelCoreMedallion(
                g, cx, cy, dualRgb, currentSub().secondaryEffect != null,
                this.focusedRing == 3, hoveredRing == 5, ceremony
        );

        // State 4 Harmonic Star Circuit Overlay when SCRIBE is pressed!
        if (ceremony) {
            float cerProg = 1.0F - (this.scribeCeremonyTicks - partialTick) / 36.0F;
            ArcaneScribingGlyphRenderer.drawCeremonyStarCircuit(g, cx, cy, cerProg * 0.45F);
        }

        // Sculpted High-Res 12 o'clock Golden Alignment Needle (drawn behind runes/badges so aligned labels stay crystal clear)
        ArcaneScribingGlyphRenderer.drawAlignmentNeedle(g, cx, y + 23, ceremony);

        // Render Crisp Rotating Distance Stops & Spell Symbols across all 5 Concentric Bands
        renderRingSymbols(g, cx, cy, 0, getWheelBandMidRadius(0), 6.8F, 0x7DF4FF, mouseX, mouseY);
        renderRingSymbols(g, cx, cy, 1, getWheelBandMidRadius(1), 6.8F, trigRgb, mouseX, mouseY);
        renderRingSymbols(g, cx, cy, 2, getWheelBandMidRadius(2), 7.0F, flowRgb, mouseX, mouseY);
        renderRingSymbols(g, cx, cy, 3, getWheelBandMidRadius(3), 7.0F, formRgb, mouseX, mouseY);
        float primIconSize = getWheelRingOptionCount(4) > 10 ? 6.2F : 6.8F;
        renderRingSymbols(g, cx, cy, 4, getWheelBandMidRadius(4), primIconSize, primRgb, mouseX, mouseY);

        // Crisp Timer Badge beside the 12 o'clock Trigger Rune when Timed Fuse or Self-Timed is aligned!
        if (this.trigger.hasTimerDial()) {
            String badge = this.trigger == SpellTriggerType.ON_TIMER
                    ? String.format("%.1fs", resolveEffectiveFlowDelaySeconds())
                    : String.format("%.0fs", resolveEffectiveFlowDelaySeconds());
            ArcaneScribingGlyphRenderer.drawBeveledBox(g, cx + 8.5F, cy - 46.5F, 18.0F, 8.0F, 0xEE180A30, 0xFFFDE047, 0x55FFFFFF);
            ArcaneScribingGlyphRenderer.drawScaledCenteredString(g, this.font, badge, cx + 17.5F, cy - 44.6F, 0.56F, 0xFF67E8F9, false);
        }

        // Crisp Transposition Badge when Primary Blink is toggled into Position Swap mode!
        if (currentSub().primaryEffect == SpellCoreEffect.BLINK && "swap".equalsIgnoreCase(currentSub().primarySubMode)) {
            ArcaneScribingGlyphRenderer.drawBeveledBox(g, cx - 24.5F, cy - 22.5F, 17.5F, 7.5F, 0xEE180A30, 0xFFFDE047, 0x55FFFFFF);
            ArcaneScribingGlyphRenderer.drawScaledCenteredString(g, this.font, "SWAP", cx - 15.75F, cy - 20.8F, 0.52F, 0xFFFDE047, false);
        }

        // Center Dual-Verb High-Res Glyph
        SpellCoreEffect secEff = currentSub().secondaryEffect;
        boolean secCryptic = secEff != null && isCryptic(SpellResearchManager.nodeId(secEff));
        ArcaneScribingGlyphRenderer.drawEffectSymbol(g, cx, cy, 11.5F, secEff, secCryptic, 0xFF000000 | dualRgb);
        if (hoveredRing == 5 && this.pendingTooltip == null) {
            this.pendingTooltip = buildEffectDecisionTooltip(secEff, currentSub().secondaryMateria, currentSub().secondaryMutation, true, true);
        }

        // Smooth High-Res Glowing Cyan Directional Rotation Arcs when hovering or dragging a ring (State 3!)
        int activeArcRing = this.draggingRing >= 0 ? this.draggingRing : hoveredRing;
        if (activeArcRing >= 0 && activeArcRing <= 4) {
            float arcR = switch (activeArcRing) {
                case 0 -> RING_DIST_OUT + 1.6F;
                case 1 -> (RING_TRIG_OUT + RING_DIST_IN) * 0.5F;
                case 2 -> (RING_FLOW_OUT + RING_TRIG_IN) * 0.5F;
                case 3 -> (RING_FORM_OUT + RING_FLOW_IN) * 0.5F;
                default -> (RING_PRIM_OUT + RING_FORM_IN) * 0.5F;
            };
            ArcaneScribingGlyphRenderer.drawArcWithArrows(g, cx, cy, arcR, this.ringAngleOffset[activeArcRing] * 0.35F, 0xEE7DF4FF);
        }

        // Needle Finial Summary Tooltip (if hovering the top finial above the Distance Stop badge)
        checkGoldenAlignmentNeedleTooltip(cx, y + 23, mouseX, mouseY);

        // State 4 Sweeping Feather Quill Animation
        if (ceremony) {
            float prog = 1.0F - (this.scribeCeremonyTicks - partialTick) / 36.0F;
            ArcaneScribingGlyphRenderer.drawQuillInscribing(g, cx, cy, Mth.clamp(prog, 0.0F, 1.0F));
        }
    }

    private void renderRingSymbols(GuiGraphics g, int cx, int cy, int wheelRing, float midR, float baseIconSize,
                                   int matRgb, int mouseX, int mouseY) {
        int count = getWheelRingOptionCount(wheelRing);
        if (count <= 0) return;
        int selIdx = getWheelRingSelectedIndex(wheelRing);
        float stepRad = Mth.TWO_PI / count;
        float offset = this.ringAngleOffset[wheelRing];
        float tickHalf = wheelRing == 0 ? 3.8F : 3.3F;
        double hoverRad = wheelRing == 0 ? 5.8D : 4.5D;

        for (int i = 0; i < count; i++) {
            float ang = -Mth.HALF_PI + (i - selIdx) * stepRad + offset;
            // Smooth sub-pixel rotated radial divider tick halfway between adjacent symbols
            float divAng = ang + stepRad * 0.5F;
            ArcaneScribingGlyphRenderer.drawRadialTick(g, cx, cy, divAng, midR - tickHalf, midR + tickHalf, 0.50F, 0x88D4AF37);

            float sx = cx + Mth.cos(ang) * midR;
            float sy = cy + Mth.sin(ang) * midR;
            boolean isSelectedAtNeedle = (i == selIdx && Math.abs(offset) < stepRad * 0.35F);
            boolean symHover = !this.editingTitlePopup && Math.hypot(mouseX - sx, mouseY - sy) <= hoverRad;

            if (wheelRing != 0) {
                if (isSelectedAtNeedle) {
                    ArcaneScribingGlyphRenderer.drawSelectionHalo(g, sx, sy, baseIconSize + 2.4F, true);
                } else if (symHover) {
                    ArcaneScribingGlyphRenderer.drawSelectionHalo(g, sx, sy, baseIconSize + 1.8F, false);
                }
            }

            float drawSize = isSelectedAtNeedle ? (baseIconSize + 1.0F) : (symHover ? (baseIconSize + 0.6F) : baseIconSize);
            int symCol = isSelectedAtNeedle
                    ? ArcaneScribingGlyphRenderer.liftRgb(matRgb, 95)
                    : (symHover ? 0xFFFFFFFF : ArcaneScribingGlyphRenderer.liftRgb(matRgb, 28));

            switch (wheelRing) {
                case 0 -> {
                    SpellTravelDistance step = SpellTravelDistance.values()[i];
                    ArcaneScribingGlyphRenderer.drawDistanceStopBadge(
                            g, this.font, sx, sy, step.shortLabel(), isSelectedAtNeedle, symHover
                    );
                    if (symHover) {
                        this.pendingTooltip = buildDistanceDecisionTooltip(step, i == selIdx);
                    }
                }
                case 1 -> {
                    SpellTriggerType tr = SpellTriggerType.values()[i];
                    boolean cryp = isCryptic(SpellResearchManager.nodeId(tr));
                    int trigCol = isSelectedAtNeedle ? 0xFFFEF08A : (symHover ? 0xFFFFFFFF : 0xFF38BDF8);
                    ArcaneScribingGlyphRenderer.drawTriggerSymbol(g, sx, sy, drawSize, tr, cryp, trigCol);
                    if (symHover) {
                        this.pendingTooltip = buildTriggerDecisionTooltip(tr, i == selIdx);
                    }
                }
                case 2 -> {
                    SpellFlowModifier fl = SpellFlowModifier.values()[i];
                    boolean cryp = isCryptic(SpellResearchManager.nodeId(fl));
                    int flowCol = (fl == SpellFlowModifier.VOID_WARP && !canSelectVoidWarp())
                            ? (symHover ? 0xFFC084FC : 0xFF6B21A8)
                            : symCol;
                    ArcaneScribingGlyphRenderer.drawFlowSymbol(g, sx, sy, drawSize, fl, cryp, flowCol);
                    if (symHover) {
                        this.pendingTooltip = buildFlowDecisionTooltip(fl, this.flowMateria, this.flowMutation, i == selIdx);
                    }
                }
                case 3 -> {
                    SpellDeliveryForm fm = SpellDeliveryForm.values()[i];
                    boolean cryp = isCryptic(SpellResearchManager.nodeId(fm));
                    ArcaneScribingGlyphRenderer.drawFormSymbol(g, sx, sy, drawSize, fm, cryp, symCol);
                    if (symHover) {
                        this.pendingTooltip = buildFormDecisionTooltip(fm, currentSub().formMateria, currentSub().formMutation, i == selIdx);
                    }
                }
                case 4 -> {
                    List<SpellCoreEffect> allowed = allowedEffects();
                    SpellCoreEffect ef = allowed.get(i);
                    boolean cryp = isCryptic(SpellResearchManager.nodeId(ef));
                    ArcaneScribingGlyphRenderer.drawEffectSymbol(g, sx, sy, drawSize, ef, cryp, symCol);
                    if (symHover) {
                        this.pendingTooltip = buildEffectDecisionTooltip(ef, currentSub().primaryMateria, currentSub().primaryMutation, false, i == selIdx);
                    }
                }
            }
        }
    }

    private void checkGoldenAlignmentNeedleTooltip(int cx, int topY, int mouseX, int mouseY) {
        boolean needleHover = !this.editingTitlePopup && isInside(mouseX, mouseY, cx - 7, topY - 3, 15, 13);
        if (needleHover && this.pendingTooltip == null) {
            SpellAssembly built = buildDraftAssembly();
            SpellCostBreakdown cost = SpellFuelCalculator.calculateCastCost(built, null, 0.0F, EssenceType.REGULAR);
            String trigSummary = formatResearchAwareName(SpellResearchManager.nodeId(this.trigger), this.trigger.getDisplayName());
            if (this.trigger == SpellTriggerType.ON_TIMER) {
                trigSummary += String.format(" (%.1fs)", resolveEffectiveFlowDelaySeconds());
            } else if (this.trigger == SpellTriggerType.SELF_TIMED) {
                trigSummary += String.format(" (%.0fs)", resolveEffectiveFlowDelaySeconds());
            }
            this.pendingTooltip = List.of(
                    Component.literal("§6✦ Aligned Incantation Summary ✦"),
                    Component.literal("§f" + built.customName() + " §8• §eTier " + ROMAN[this.potencyTier - 1] + " " + this.prefix.getDisplayName()),
                    Component.literal("§7Travel: §b" + this.travelDistance.shortLabel()
                            + String.format(" §8(%.2fx)", this.travelDistance.getCostMultiplier())
                            + " §8• §7Trigger: §e" + trigSummary),
                    Component.literal("§7Flow: §b" + formatResearchAwareName(SpellResearchManager.nodeId(this.flow), this.flow.getDisplayName())
                            + " §8• §7Cast: §d" + SpellFlowModifier.getCastCountTitle(getActiveSlotCount())),
                    Component.literal("§7Cast Fuel: §e" + cost.formatTypeCostSummary()),
                    Component.literal(String.format("§7Cooldown: §b%.1fs §8• Hover any ring stop below for full stats!", built.getBaseCooldownTicks() / 20.0F))
            );
        }
    }

    // =========================================================================
    // UNDER-DESK INVENTORY TRAY & QUICK PLAQUES (y = 155..235)
    // =========================================================================

    private void renderUnderDeskTray(GuiGraphics g, int x, int y, int mouseX, int mouseY) {
        // Center Player Inventory Satchel Frame (x + 73 .. x + 243, y + 155 .. y + 235)
        ArcaneScribingGlyphRenderer.drawBeveledBox(g, x + 73.0F, y + 155.0F, 170.0F, 80.0F, 0xF40B132B, 0xFFD4AF37, 0x44FFFBEB);
        for (int i = 4; i < this.menu.slots.size(); i++) {
            Slot s = this.menu.slots.get(i);
            float sx = x + s.x - 1.0F;
            float sy = y + s.y - 1.0F;
            ArcaneScribingGlyphRenderer.drawBeveledBox(g, sx, sy, 18.0F, 18.0F, 0xFF111A2E, 0xFF3E5278, 0x22FFFFFF);
        }

        // Left Plaque: Tier Stepper (I..X), Spell Name & Quick Drawer Toggle (x + 4 .. x + 71, y + 155 .. y + 235)
        ArcaneScribingGlyphRenderer.drawBeveledBox(g, x + 4.0F, y + 155.0F, 67.0F, 80.0F, 0xF40B132B, 0xFFD4AF37, 0x44FFFBEB);

        // Tier [-] T-X [+]
        boolean minusHov = isInside(mouseX, mouseY, x + 8, y + 160, 13, 13);
        boolean plusHov = isInside(mouseX, mouseY, x + 54, y + 160, 13, 13);
        boolean tierHov = isInside(mouseX, mouseY, x + 21, y + 160, 33, 13);
        ArcaneScribingGlyphRenderer.drawBeveledBox(g, x + 8.0F, y + 160.0F, 13.0F, 13.0F,
                minusHov ? 0xFFFDE047 : 0xFF26354D, minusHov ? 0xFFFFFFFF : 0xFF64748B, 0x33FFFFFF);
        ArcaneScribingGlyphRenderer.drawScaledCenteredString(g, this.font, "−", x + 14.5F, y + 163.0F, 0.82F, minusHov ? 0xFF0F172A : 0xFFFFFFFF, false);
        ArcaneScribingGlyphRenderer.drawScaledCenteredString(g, this.font, "T-" + ROMAN[this.potencyTier - 1], x + 37.5F, y + 163.0F, 0.82F, 0xFF67E8F9, true);
        ArcaneScribingGlyphRenderer.drawBeveledBox(g, x + 54.0F, y + 160.0F, 13.0F, 13.0F,
                plusHov ? 0xFFFDE047 : 0xFF26354D, plusHov ? 0xFFFFFFFF : 0xFF64748B, 0x33FFFFFF);
        ArcaneScribingGlyphRenderer.drawScaledCenteredString(g, this.font, "+", x + 60.5F, y + 163.0F, 0.82F, plusHov ? 0xFF0F172A : 0xFFFFFFFF, false);
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

        // Quick Rename Ribbon Button — crisp scaled text fits full "Arcane Bolt"!
        boolean nameHov = isInside(mouseX, mouseY, x + 8, y + 177, 59, 14);
        ArcaneScribingGlyphRenderer.drawBeveledBox(g, x + 8.0F, y + 177.0F, 59.0F, 14.0F,
                nameHov ? 0xFFD4AF37 : 0xFF1A2538, nameHov ? 0xFFFDE68A : 0xFF475569, 0x33FFFFFF);
        String fittedName = ArcaneScribingGlyphRenderer.fitTextToWidth(this.font, this.customName, 55.0F, 0.72F);
        ArcaneScribingGlyphRenderer.drawScaledCenteredString(
                g, this.font, fittedName, x + 37.5F, y + 181.0F, 0.72F, nameHov ? 0xFF0F172A : 0xFFFDE68A, !nameHov
        );
        if (nameHov) {
            this.pendingTooltip = List.of(
                    Component.literal("§6Spell Name: §f" + this.customName),
                    Component.literal("§8• Click to rename or add custom incantation lore")
            );
        }

        // Quick Left Drawer Toggle Button
        boolean lDrawBtnHov = isInside(mouseX, mouseY, x + 8, y + 194, 59, 14);
        ArcaneScribingGlyphRenderer.drawBeveledBox(g, x + 8.0F, y + 194.0F, 59.0F, 14.0F,
                lDrawBtnHov ? 0xFF6B21A8 : 0xFF281047, lDrawBtnHov ? 0xFFE9D5FF : 0xFF7E22CE, 0x33FFFFFF);
        ArcaneScribingGlyphRenderer.drawScaledCenteredString(
                g, this.font, this.leftDrawerTarget > 0.5F ? "◀ Tuck" : "▶ Drawer", x + 37.5F, y + 198.0F, 0.75F, 0xFFF3E8FF, true
        );

        // Cooldown / Channel readout
        SpellAssembly preview = buildDraftAssembly();
        boolean channeledPreview = preview.isChanneledHoldBeam();
        ArcaneScribingGlyphRenderer.drawScaledCenteredString(
                g, this.font,
                channeledPreview ? "Hold Beam" : String.format("CD: %.1fs", preview.getBaseCooldownTicks() / 20.0F),
                x + 37.5F, y + 216.0F, 0.76F, channeledPreview ? 0xFF67E8F9 : 0xFF94A3B8, true
        );

        // Right Plaque: Live Fuel Cost & Quick Supply Drawer Toggle (x + 245 .. x + 312, y + 155 .. y + 235)
        ArcaneScribingGlyphRenderer.drawBeveledBox(g, x + 245.0F, y + 155.0F, 67.0F, 80.0F, 0xF40B132B, 0xFFD4AF37, 0x44FFFBEB);
        ArcaneScribingGlyphRenderer.drawScaledCenteredString(
                g, this.font, channeledPreview ? "Materia/s" : "Fuel Cost", x + 278.5F, y + 161.0F, 0.80F, 0xFFFDE047, true
        );

        SpellCostBreakdown cost = SpellFuelCalculator.calculateCastCost(preview, null, 0.0F, EssenceType.REGULAR);
        String costStr = cost.formatTypeCostSummary();
        String fittedCost = ArcaneScribingGlyphRenderer.fitTextToWidth(this.font, costStr, 60.0F, 0.70F);
        ArcaneScribingGlyphRenderer.drawScaledCenteredString(g, this.font, fittedCost, x + 278.5F, y + 173.5F, 0.70F, 0xFF67E8F9, true);
        ArcaneScribingGlyphRenderer.drawScaledCenteredString(
                g, this.font, this.materiaSpecific ? "-15% Lock" : "Dynamic", x + 278.5F, y + 185.5F, 0.74F,
                this.materiaSpecific ? 0xFF4ADE80 : 0xFFCBD5E1, true
        );

        boolean rDrawBtnHov = isInside(mouseX, mouseY, x + 249, y + 198, 59, 14);
        ArcaneScribingGlyphRenderer.drawBeveledBox(g, x + 249.0F, y + 198.0F, 59.0F, 14.0F,
                rDrawBtnHov ? 0xFFB45309 : 0xFF382010, rDrawBtnHov ? 0xFFFDE68A : 0xFFB8922A, 0x33FFFFFF);
        ArcaneScribingGlyphRenderer.drawScaledCenteredString(
                g, this.font, this.rightDrawerTarget > 0.5F ? "Cover ▶" : "◀ Scribe", x + 278.5F, y + 202.0F, 0.75F, 0xFFFDE68A, true
        );

        boolean ready = this.menu.getSlot(0).hasItem() && this.menu.getSlot(1).hasItem();
        ArcaneScribingGlyphRenderer.drawScaledCenteredString(
                g, this.font, ready ? "Ink Ready" : "Need Ink", x + 278.5F, y + 218.0F, 0.76F, ready ? 0xFF4ADE80 : 0xFFF87171, true
        );

        if (isInside(mouseX, mouseY, x + 246, y + 158, 65, 36)) {
            this.pendingTooltip = List.of(
                    Component.literal(channeledPreview ? "§6Live Channeled Beam Drain" : "§6Live Cast Fuel Breakdown"),
                    Component.literal((channeledPreview ? "§7Drain per Second: §e" : "§7Total per Cast: §e") + costStr),
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
        ArcaneScribingGlyphRenderer.fillHiRes(g, px - 2.0F, py - 2.0F, px + pw + 2.0F, py + ph + 2.0F, 0xAA000000);
        ArcaneScribingGlyphRenderer.drawBeveledBox(g, px, py, pw, ph, 0xFA0B132B, 0xFFD4AF37, 0x55FFFBEB);
        ArcaneScribingGlyphRenderer.drawScaledString(g, this.font, "§6Spell Name:", px + 8.0F, py + 6.0F, 0.80F, 0xFFFFFFFF, true);
        ArcaneScribingGlyphRenderer.drawScaledString(g, this.font, "§7Custom Lore:", px + 8.0F, py + 33.0F, 0.80F, 0xFFFFFFFF, true);
        ArcaneScribingGlyphRenderer.drawBeveledBox(g, px + 42.0F, py + 59.0F, 48.0F, 13.0F, 0xFF1E293B, 0xFFD4AF37, 0x44FFFFFF);
        ArcaneScribingGlyphRenderer.drawScaledCenteredString(g, this.font, "Done", px + 66.0F, py + 62.5F, 0.80F, 0xFFFDE047, true);
    }

    // =========================================================================
    // RICH SPELL DECISION TOOLTIP BUILDERS (STATE 3)
    // =========================================================================

    private List<Component> buildDistanceDecisionTooltip(SpellTravelDistance step, boolean isCurrent) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal("§b✦ " + step.getDisplayName()));
        lines.add(Component.literal(String.format("§6Outer Rim: §bTravel Distance Circle §8• §6Fuel Mult: §f%.2fx", step.getCostMultiplier())));
        lines.add(Component.literal("§7• " + step.getSummaryDescription()));
        if (step.isPointBlankZero()) {
            lines.add(Component.literal("§e• Point-Blank (0m): Discharges immediately right in front of you — zero flight time!"));
        } else {
            lines.add(Component.literal(String.format(
                    "§7• Scales Projectile flight, Beam reach, Blink step, Void-Warp & Placement (%.1fx standard).",
                    step.getDistanceScaleFromStandard()
            )));
        }
        SpellTravelDistance saved = this.travelDistance;
        this.travelDistance = step;
        SpellAssembly preview = buildDraftAssembly();
        this.travelDistance = saved;
        SpellCostBreakdown cost = SpellFuelCalculator.calculateCastCost(preview, null, 0.0F, EssenceType.REGULAR);
        lines.add(Component.literal((cost.channeledPerSecond() ? "§7• Materia Drain at " + step.shortLabel() + ": §b" : "§7• Cast Fuel at " + step.shortLabel() + ": §b")
                + cost.formatTypeCostSummary()));
        lines.add(Component.literal("§8• " + (isCurrent
                ? "Aligned! Scroll, drag, or Shift+Right-Click to reset to 16m."
                : "Click, scroll, or drag Outer Rim Distance Circle to align")));
        return lines;
    }

    private List<Component> buildTriggerDecisionTooltip(SpellTriggerType tr, boolean isCurrent) {
        List<Component> lines = new ArrayList<>();
        String nodeId = SpellResearchManager.nodeId(tr);
        if (isCryptic(nodeId)) {
            lines.add(Component.literal("§dCryptic Unknown Trigger Rune [?]"));
            lines.add(Component.literal("§7Outermost Ring: §dUnresearched Trigger Logic"));
            lines.add(Component.literal("§7• Align & Scribe to test this activation condition in the wild!"));
            lines.add(Component.literal("§8• Observing or casting it unlocks its true name."));
            return lines;
        }
        lines.add(Component.literal("§e" + tr.getDisplayName() + (isObservedOnly(nodeId) ? " §6(Observed)" : "")));
        lines.add(Component.literal("§6Outer Ring: §eTrigger Logic §8• §6Cost Mult: §f" + String.format("%.2fx", tr.getCostMultiplier())));

        String trigDesc = switch (tr) {
            case ON_ENTITY_HIT -> "Detonates full spell power on living targets; accidental block misses splash at 75% potency.";
            case ON_BLOCK_COLLISION -> "Detonates at 100% full potency on walls, floors & world blocks (passes cleanly past mobs).";
            case ON_TIMER -> String.format("Detonates or splits mid-air after %.1fs (Right-click aligned rune or Flow tab to tune 0.5s–3.0s • 25%%/75%% Fuel).",
                    this.trigger == SpellTriggerType.ON_TIMER ? resolveEffectiveFlowDelaySeconds() : 1.5F);
            case ON_PROXIMITY -> "Drifts as a sensor orb and detonates when a target enters within 3.0m (25%/75% Dynamic Fuel).";
            case ON_RELEASE -> "Hold right-click to compress your spell (up to +15% power), firing the instant you release!";
            case ON_DEATH -> "Marks a struck foe for 3.0s: if slain, its corpse blooms with all slotted spells (25%/75% Fuel)!";
            case SELF -> "Casts all slotted spells (Single–Octuple) immediately centered on yourself.";
            case SELF_TIMED -> String.format("Contingency aura: fires centered on yourself after %.0fs (Right-click to cycle 30s / 60s / 120s).",
                    this.trigger == SpellTriggerType.SELF_TIMED ? resolveEffectiveFlowDelaySeconds() : 30.0F);
        };
        lines.add(Component.literal("§7• " + trigDesc));
        lines.add(Component.literal("§b• Pairs freely with any Flow Modifier & Single–Octuple Cast!"));
        lines.add(Component.literal("§8• " + (isCurrent
                ? (tr.hasTimerDial() ? "Aligned! Right-click rune to tune timer interval." : "Aligned under the 12 o'clock Needle!")
                : "Click or drag Outermost Ring to align")));
        return lines;
    }

    private List<Component> buildFlowDecisionTooltip(SpellFlowModifier fl, EssenceType mat, boolean mutEnabled, boolean isCurrent) {
        List<Component> lines = new ArrayList<>();
        String nodeId = SpellResearchManager.nodeId(fl);
        if (isCryptic(nodeId)) {
            lines.add(Component.literal("§dCryptic Unknown Flow Rune [?]"));
            lines.add(Component.literal("§7Flow Ring: §dUnresearched Flow Modifier"));
            lines.add(Component.literal("§7• Align & Scribe to test this mysterious rune in the wild!"));
            lines.add(Component.literal("§8• Observing or casting it unlocks its true name & Mutations."));
            return lines;
        }
        Player p = Minecraft.getInstance().player;
        boolean canMut = mat != EssenceType.REGULAR && (p == null || (SpellResearchManager.isNodeUnlocked(p, nodeId)
                && SpellResearchManager.isMutationUnlocked(p, nodeId, mat, true)));
        boolean effectiveMut = (fl == SpellFlowModifier.VOID_WARP && canSelectVoidWarp()) || (mutEnabled && canMut);
        SpellMutationProfile prof = SpellMutationRegistry.resolveFlow(fl, mat, effectiveMut);

        String titleCol = (fl == SpellFlowModifier.VOID_WARP && !canSelectVoidWarp())
                ? "§5"
                : (mat == EssenceType.REGULAR ? "§b" : mat.getColorCode());
        lines.add(Component.literal(titleCol + (prof.mutated() ? prof.signatureTitle() : fl.getDisplayName())
                + (isObservedOnly(nodeId) ? " §e(Observed)" : "")));
        lines.add(Component.literal("§6Flow: §b" + fl.getDisplayName() + " §8• §6Materia: " + formatTooltipMateriaState(mat, prof.mutated())));

        String flowTrait = switch (fl) {
            case SINGLE -> "Direct delivery: carries your cast straight to the target (1x Flow Cost)";
            case DOUBLE_BIFURCATE -> "Splits into 2 smart-seeking payloads (converges on lone bosses! • 1.85x Cost)";
            case TRIPLE_TRIFURCATE -> "Splits into 3 forward-arc payloads seeking up to 3 foes (2.65x Cost)";
            case OCTUPLE_DOME -> "Bursts 8 payloads upward in a hemisphere dome — zero ground waste (5.50x Cost)";
            case CHAIN_LEAP -> "Leaps across up to 3 foes within 12m, casting your spells at each hit (25%/75% Fuel)";
            case ORBIT_SATELLITE -> "Orbits 4 guardian payloads around you that launch at nearby foes (2.80x Cost)";
            case VOID_WARP -> "Zero-travel spatial rift: materializes your spells instantly at crosshair (25%/75% Fuel)";
            case FAST_DART -> "Supersonic zero-gravity bolt (+135% speed, +35% knockback, 64m snipe)";
            case SLOW_HEAVY -> "Dense creeping orb (-55% speed, grants +0.40 Materia Advantage!)";
        };
        lines.add(Component.literal("§7• " + flowTrait));
        if (fl == SpellFlowModifier.VOID_WARP) {
            if (canSelectVoidWarp()) {
                lines.add(Component.literal("§d✦ Void Travel Mutation Ready: " + mat.getFormattedName() + " §dauthorizes spatial wormhole travel!"));
            } else {
                lines.add(Component.literal("§c✦ Locked Travel Mutation: Requires Void or Void-Created Materia + Mutation: ACTIVE!"));
                lines.add(Component.literal("§8  (Void, Astral, Null-R, Null-U, Wraith, Penumbra, Abyss, Oblivion, Entropic, Cataclysm, Eschaton, Entropica)"));
            }
        }
        lines.add(Component.literal("§d• Active Cast: §f" + SpellFlowModifier.getCastCountTitle(getActiveSlotCount())
                + (getActiveSlotCount() > 1 ? " §7(all " + getActiveSlotCount() + " spells cast at hit spot!)" : "")));
        appendMutationTraitBullets(lines, prof);
        if (mat != EssenceType.REGULAR) {
            lines.add(Component.literal("§8• Matchup: §7" + ddraig.net.entropica.combat.VisInteractionManager.getInstance().formatMatchupSummary(mat)));
        }

        SpellAssembly preview = buildDraftAssembly();
        SpellCostBreakdown cost = SpellFuelCalculator.calculateCastCost(preview, null, 0.0F, EssenceType.REGULAR);
        lines.add(Component.literal((cost.channeledPerSecond() ? "§7• Materia Drain: §b" : "§7• Spell Cast Fuel: §b") + cost.formatTypeCostSummary()));
        String cdOrHold = preview.isChanneledHoldBeam()
                ? "§7• Mode: §bHold Right-Click (Channeled)"
                : String.format("§7• Cooldown: §f%.1fs", preview.getBaseCooldownTicks() / 20.0F);
        lines.add(Component.literal(cdOrHold + " §8• " + (isCurrent ? "Aligned under Needle!" : (fl == SpellFlowModifier.VOID_WARP && !canSelectVoidWarp()
                ? "Infuse Void-Lineage Materia in Flow Drawer to Unlock"
                : "Click or Drag Ring to Align"))));
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

        boolean earthBonus = prof.mutated() && ddraig.net.entropica.spell.mutation.MateriaFamilyTier.isEarthLineage(mat);
        double beamReach = this.travelDistance.resolveBaseBeamReachBlocks(this.potencyTier, this.prefix == SpellPrefix.COMBAT, earthBonus) * prof.rangeMultiplier();
        String shapeDesc = switch (fm) {
            case PROJECTILE -> this.travelDistance.isPointBlankZero()
                    ? "Discharges immediately at point-blank muzzle origin (0m travel)"
                    : ("Fires an aimed arcane bolt up to " + this.travelDistance.shortLabel() + " toward your target");
            case BEAM -> String.format("Hold Right-Click to channel a %.1fm beam from your hand (5x/s + 3s Burst Finisher)", beamReach);
            case RADIAL_BURST -> String.format("Detonates a %.1fm expanding nova ring around impact/caster",
                    SpellDeliveryForm.getCombatRadiusBlocks(SpellDeliveryForm.RADIAL_BURST, this.potencyTier) * prof.radiusMultiplier());
            case TOUCH_STRIKE -> "Primes your weapon/hand (30s) — discharges on your next melee hit (+50% cleave splash)";
            case AURA -> "Wraps you in a sustained shroud (Sneak-Cast to anchor field)";
            case GLYPH -> "Inscribes a persistent 1s-arming trap (Sneak + Right-Click for 50% refund)";
            case CONE -> String.format("Sweeps a wide 60° elemental fan %.1fm forward",
                    SpellDeliveryForm.getCombatRadiusBlocks(SpellDeliveryForm.CONE, this.potencyTier) * prof.radiusMultiplier());
            case SKYFALL -> "Calls down an overhead elemental tempest or skyfall pillar (cave-safe)";
            case SUMMON_AGENT -> "Conjures a mobile Eidolon companion that hunts foes";
            case SUMMON_SENTRY -> "Conjures a stationary crystalline Eidolon turret pylon";
            case SUMMON_DECOY -> "Conjures an aggro-drawing Phantom Eidolon effigy";
        };
        lines.add(Component.literal("§7• " + shapeDesc));
        appendMutationTraitBullets(lines, prof);
        if (mat != EssenceType.REGULAR) {
            lines.add(Component.literal("§8• Matchup: §7" + ddraig.net.entropica.combat.VisInteractionManager.getInstance().formatMatchupSummary(mat)));
        }

        lines.add(Component.literal(String.format("§7• Base Power: §f%.0f HP §8(Tier %s)",
                MateriaStageTable.getBaseCombatDamageHp(this.potencyTier), ROMAN[this.potencyTier - 1])));
        SpellAssembly preview = buildDraftAssembly();
        SpellCostBreakdown cost = SpellFuelCalculator.calculateCastCost(preview, null, 0.0F, EssenceType.REGULAR);
        lines.add(Component.literal((cost.channeledPerSecond() ? "§7• Materia Drain: §b" : "§7• Fuel Cost: §b") + cost.formatTypeCostSummary()));
        lines.add(Component.literal(preview.isChanneledHoldBeam()
                ? "§7• Mode: §bHold Right-Click (Channeled)"
                : String.format("§7• Cooldown: §f%.1fs", preview.getBaseCooldownTicks() / 20.0F)));
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
            case HARM -> String.format("Deals %.1f HP pure Materia damage (Tier %s)",
                    MateriaStageTable.getBaseCombatDamageHp(this.potencyTier) * prof.potencyMultiplier(), ROMAN[this.potencyTier - 1]);
            case WARD -> String.format("Grants %.0f HP arcane ward barrier (Tier %s)",
                    MateriaStageTable.getBaseWardShieldHp(this.potencyTier) * prof.potencyMultiplier(), ROMAN[this.potencyTier - 1]);
            case RESTORATION -> String.format("Restores %.1f HP vitality & cleanses hexes (Tier %s)",
                    MateriaStageTable.getBaseRestorationHealHp(this.potencyTier) * prof.potencyMultiplier(), ROMAN[this.potencyTier - 1]);
            case KINETIC -> "Applies forceful knockback, pull, flight, or grapple impulse";
            case BLINK -> {
                if (this.travelDistance.isPointBlankZero()) {
                    yield "In-place 0m phase step (resets fall momentum & triggers arrival effects on the spot)";
                }
                float blinkReach = (this.potencyTier <= 3 ? 16.0F : 16.0F + (this.potencyTier - 3) * 2.5F)
                        * this.travelDistance.getDistanceScaleFromStandard();
                yield String.format("Teleports up to %.1fm (%s) • 100%% fuel refund if blocked/unsafe",
                        blinkReach, this.potencyTier >= 4 ? "Phases Walls" : "Line-of-Sight");
            }
            case TRANSMUTE -> "Breaks, harvests, or transmutes world blocks & dropped items";
            case AFFLICTION -> "Inflicts persistent anti-milk elemental hexes (30s/15s boss cap)";
            case CONJURE -> "Conjures arcane light, constructs, sonar, or weather shifts";
        };
        lines.add(Component.literal("§7• " + powerLine));
        if (ef == SpellCoreEffect.BLINK && isCurrent) {
            String activeMode = isDualVerb ? currentSub().secondarySubMode : currentSub().primarySubMode;
            boolean swap = "swap".equalsIgnoreCase(activeMode);
            lines.add(Component.literal("§e• Mode: §f" + (swap ? "Position Swap (Transposition)" : "Self-Blink (Instant Step)")
                    + " §8(Right-Click rune to toggle)"));
        }
        appendMutationTraitBullets(lines, prof);
        if (!prof.mutated() && canMut) {
            SpellMutationProfile previewMut = SpellMutationRegistry.resolveEffect(slotType, ef, mat, true);
            if (previewMut.mutated()) {
                lines.add(Component.literal("§8• Tip: Flip §eMUTATION: ON §8in Left Drawer for §d" + previewMut.signatureTitle() + "§8!"));
            }
        }
        if (mat != EssenceType.REGULAR) {
            lines.add(Component.literal("§8• Matchup: §7" + ddraig.net.entropica.combat.VisInteractionManager.getInstance().formatMatchupSummary(mat)));
        }

        SpellAssembly preview = buildDraftAssembly();
        SpellCostBreakdown cost = SpellFuelCalculator.calculateCastCost(preview, null, 0.0F, EssenceType.REGULAR);
        lines.add(Component.literal((cost.channeledPerSecond() ? "§7• Materia Drain: §b" : "§7• Fuel Cost: §b") + cost.formatTypeCostSummary()));
        lines.add(Component.literal(preview.isChanneledHoldBeam()
                ? "§7• Mode: §bHold Right-Click (Channeled)"
                : String.format("§7• Cooldown: §f%.1fs", preview.getBaseCooldownTicks() / 20.0F)));
        return lines;
    }

    private void appendMutationTraitBullets(List<Component> lines, SpellMutationProfile prof) {
        if (!prof.mutated()) return;
        if (prof.mutationDescription() != null && !prof.mutationDescription().isBlank()) {
            lines.add(Component.literal("§d✦ " + prof.mutationDescription()));
        }
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
                Component.literal("§7Epithet: §f" + SpellMutationRegistry.getEpithet(mat)),
                Component.literal("§7Matchup: §e" + ddraig.net.entropica.combat.VisInteractionManager.getInstance().formatMatchupSummary(mat)),
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
        if (this.leftDrawerOpen > 0.7F && handleOpenLeftDrawerClick(mx, my, btn, x, y)) {
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

        // 4. 6-Zone Concentric Symbol Rings (Click-and-Drag Rotation or Direct Stop/Symbol Click!)
        int cx = x + WHEEL_REL_CX;
        int cy = y + WHEEL_REL_CY;
        if (isInside(mx, my, x + 92, y + 28, 132, 117)) {
            float dist = (float) Math.hypot(mx - cx, my - cy);
            if (dist <= RING_DUAL_OUT) {
                this.focusedRing = 3;
                if (btn == 1 && event.hasShiftDown() && currentSub().secondaryEffect == SpellCoreEffect.BLINK) {
                    currentSub().secondarySubMode = "swap".equalsIgnoreCase(currentSub().secondarySubMode) ? "default" : "swap";
                    playRingTickSound(1.4F);
                    return true;
                }
                cycleWheelRing(5, btn == 1 ? -1 : 1);
                playStoneCircleDetentSound(5);
                return true;
            } else if (dist <= RING_DIST_OUT + 3.0F) {
                int wheelRing = dist <= RING_PRIM_OUT + 0.5F ? 4
                        : (dist <= RING_FORM_OUT + 0.5F ? 3
                        : (dist <= RING_FLOW_OUT + 0.5F ? 2
                        : (dist <= RING_TRIG_OUT + 0.5F ? 1 : 0)));
                focusMateriaRingForWheel(wheelRing);
                if (wheelRing == 0 && btn == 1 && event.hasShiftDown()) {
                    selectWheelRingIndexAnimated(0, SpellTravelDistance.DIST_16.ordinal());
                    return true;
                }
                if (wheelRing == 1 && btn == 1 && this.trigger.hasTimerDial()) {
                    cycleFlowTimerInterval();
                    return true;
                }
                if (wheelRing == 4 && btn == 1 && currentSub().primaryEffect == SpellCoreEffect.BLINK) {
                    currentSub().primarySubMode = "swap".equalsIgnoreCase(currentSub().primarySubMode) ? "default" : "swap";
                    playRingTickSound(1.4F);
                    return true;
                }
                this.draggingRing = wheelRing;
                this.lastRingDragMouseAngle = (float) Mth.atan2(my - cy, mx - cx);
                this.stoneGrindDragAccum = 0.0F;
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

    private boolean handleOpenLeftDrawerClick(double mx, double my, int btn, int x, int y) {
        int dx = x - 38 + Math.round((1.0F - this.leftDrawerOpen) * 124.0F);
        int dy = y + 25;
        if (!isInside(mx, my, dx, dy, 124, 124)) return false;

        // Wax Seals (Combat, Utility, Misc, Lock)
        int compAY = dy + 6;
        for (int s = 0; s < 4; s++) {
            int cardX = dx + 6 + s * 28;
            if (isInside(mx, my, cardX, compAY, 26, 28)) {
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
        // Sub-Spell Roman Tabs I..VIII (Single 1x through Octuple 8x Cast!)
        int tabY = compAY + 28;
        int slotCount = getActiveSlotCount();
        for (int i = 0; i < 8; i++) {
            if (isInside(mx, my, dx + 6 + i * 14, tabY, 13, 11)) {
                if (btn == 1) {
                    if (i == 0) {
                        this.subSpellCount = 1;
                    } else if (i == slotCount - 1) {
                        this.subSpellCount = Math.max(1, i);
                    } else {
                        this.subSpellCount = i + 1;
                    }
                    this.activeSubSlot = Math.clamp(this.activeSubSlot, 0, this.subSpellCount - 1);
                } else {
                    if (i >= slotCount) {
                        this.subSpellCount = i + 1;
                    }
                    this.activeSubSlot = i;
                }
                playPageTurnSound();
                return true;
            }
        }
        // Ring Focus Pills (Right-click Flow pill to cycle Timer / Self-Timed interval!)
        for (int r = 0; r < 4; r++) {
            if (isInside(mx, my, dx + 6 + r * 28, dy + 52, 26, 11)) {
                this.focusedRing = r;
                if (r == 0 && btn == 1 && this.trigger.hasTimerDial()) {
                    cycleFlowTimerInterval();
                } else if (r == 2 && btn == 1 && currentSub().primaryEffect == SpellCoreEffect.BLINK) {
                    currentSub().primarySubMode = "swap".equalsIgnoreCase(currentSub().primarySubMode) ? "default" : "swap";
                    playRingTickSound(1.4F);
                } else if (r == 3 && btn == 1 && currentSub().secondaryEffect == SpellCoreEffect.BLINK) {
                    currentSub().secondarySubMode = "swap".equalsIgnoreCase(currentSub().secondarySubMode) ? "default" : "swap";
                    playRingTickSound(1.4F);
                } else {
                    playClickSound(1.25F);
                }
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
        if (isInside(mx, my, x + 246, y + 53, 54, 38)) {
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
            int bx = (int) (x + 239.0F + b * 23.5F);
            int by = y + 95;
            if (isInside(mx, my, bx, by, 21, 13)) {
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

        if (this.draggingRing >= 0 && this.draggingRing <= 4) {
            if (Math.hypot(mx - this.ringClickStartX, my - this.ringClickStartY) > 3.5D) {
                this.dragMovedSignificantly = true;
            }
            int cx = this.leftPos + WHEEL_REL_CX;
            int cy = this.topPos + WHEEL_REL_CY;
            float curAng = (float) Mth.atan2(my - cy, mx - cx);
            float dAng = Mth.wrapDegrees((curAng - this.lastRingDragMouseAngle) * Mth.RAD_TO_DEG) * Mth.DEG_TO_RAD;
            this.lastRingDragMouseAngle = curAng;

            int r = this.draggingRing;
            int count = getWheelRingOptionCount(r);
            if (count > 0) {
                float stepRad = Mth.TWO_PI / count;
                this.ringAngleOffset[r] += dAng;
                this.stoneGrindDragAccum += Math.abs(dAng);

                boolean crossedDetent = false;
                while (this.ringAngleOffset[r] >= stepRad * 0.5F) {
                    this.ringAngleOffset[r] -= stepRad;
                    cycleWheelRing(r, -1);
                    crossedDetent = true;
                }
                while (this.ringAngleOffset[r] <= -stepRad * 0.5F) {
                    this.ringAngleOffset[r] += stepRad;
                    cycleWheelRing(r, 1);
                    crossedDetent = true;
                }

                long nowMs = System.currentTimeMillis();
                if (crossedDetent) {
                    this.stoneGrindDragAccum = 0.0F;
                    this.lastStoneGrindSoundMs = nowMs;
                    playStoneCircleDetentSound(r);
                } else if (this.stoneGrindDragAccum >= 0.052F && nowMs - this.lastStoneGrindSoundMs >= 72L) {
                    this.stoneGrindDragAccum = 0.0F;
                    this.lastStoneGrindSoundMs = nowMs;
                    float speedFactor = Mth.clamp(Math.abs(dAng) / 0.14F, 0.0F, 1.0F);
                    playStoneCircleGrindSound(r, speedFactor);
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
                // Direct click on a visible stop/symbol around the ring spins it smoothly under the needle!
                int cx = this.leftPos + WHEEL_REL_CX;
                int cy = this.topPos + WHEEL_REL_CY;
                int count = getWheelRingOptionCount(r);
                int selIdx = getWheelRingSelectedIndex(r);
                float midR = getWheelBandMidRadius(r);
                float stepRad = Mth.TWO_PI / count;
                double hitRad = r == 0 ? 6.5D : 5.2D;
                for (int i = 0; i < count; i++) {
                    float ang = -Mth.HALF_PI + (i - selIdx) * stepRad;
                    float sx = cx + Mth.cos(ang) * midR;
                    float sy = cy + Mth.sin(ang) * midR;
                    if (Math.hypot(event.x() - sx, event.y() - sy) <= hitRad) {
                        selectWheelRingIndexAnimated(r, i);
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

        // Scroll Roman Tabs (Single I .. Octuple VIII) or Materia Vials page when hovering open Left Drawer
        if (this.leftDrawerOpen > 0.7F) {
            int dx = x - 38 + Math.round((1.0F - this.leftDrawerOpen) * 124.0F);
            if (isInside(mouseX, mouseY, dx + 4, y + 57, 116, 15)) {
                this.subSpellCount = Mth.clamp(this.subSpellCount - delta, 1, 8);
                this.activeSubSlot = Math.clamp(this.activeSubSlot, 0, this.subSpellCount - 1);
                playPageTurnSound();
                return true;
            }
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

        // Scroll 6-Zone Concentric Symbol Rings directly!
        int cx = x + WHEEL_REL_CX;
        int cy = y + WHEEL_REL_CY;
        float dist = (float) Math.hypot(mouseX - cx, mouseY - cy);
        if (dist <= RING_DIST_OUT + 3.0F && isInside(mouseX, mouseY, x + 92, y + 28, 132, 117)) {
            int wheelRing = dist <= RING_DUAL_OUT ? 5
                    : (dist <= RING_PRIM_OUT + 0.5F ? 4
                    : (dist <= RING_FORM_OUT + 0.5F ? 3
                    : (dist <= RING_FLOW_OUT + 0.5F ? 2
                    : (dist <= RING_TRIG_OUT + 0.5F ? 1 : 0))));
            focusMateriaRingForWheel(wheelRing);
            int count = Math.max(1, getWheelRingOptionCount(wheelRing));
            cycleWheelRing(wheelRing, delta);
            if (wheelRing <= 4) {
                this.ringAngleOffset[wheelRing] = delta * (Mth.TWO_PI / count);
                this.stoneGrindGlideAccum[wheelRing] = 0.0F;
            }
            this.lastStoneGrindSoundMs = System.currentTimeMillis();
            playStoneCircleDetentSound(wheelRing);
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

    /**
     * Plays a soft, tactile carved-stone sliding/grinding grain while a concentric spell circle is being dragged or spun.
     * Larger outer rings sound slightly deeper and heavier than smaller inner rings.
     */
    private void playStoneCircleGrindSound(int wheelRing, float speedFactor) {
        if (this.minecraft == null || this.minecraft.player == null) {
            return;
        }
        int clampedRing = Mth.clamp(wheelRing, 0, 5);
        int variant = (this.stoneGrindGrainVariant++) & 3;
        float jitter = ((variant - 1.5F) * 0.018F);
        float basePitch = 0.63F + clampedRing * 0.052F + speedFactor * 0.06F + jitter;
        float stoneVol = 0.25F + speedFactor * 0.11F;
        float gritVol = 0.13F + speedFactor * 0.07F;

        net.minecraft.sounds.SoundEvent stoneScrape = switch (variant) {
            case 0 -> SoundEvents.POLISHED_DEEPSLATE_STEP;
            case 1 -> SoundEvents.DEEPSLATE_TILES_STEP;
            case 2 -> SoundEvents.POLISHED_TUFF_STEP;
            default -> SoundEvents.TUFF_BRICKS_STEP;
        };
        this.minecraft.player.playSound(stoneScrape, stoneVol, basePitch);
        this.minecraft.player.playSound(SoundEvents.BRUSH_SAND, gritVol, basePitch * 0.92F);
    }

    /**
     * Plays a light stone grind + carved-notch detent click when a rune or distance stop locks into place under the needle.
     */
    private void playStoneCircleDetentSound(int wheelRing) {
        if (this.minecraft == null || this.minecraft.player == null) {
            return;
        }
        int clampedRing = Mth.clamp(wheelRing, 0, 5);
        float basePitch = 0.72F + clampedRing * 0.055F;
        this.minecraft.player.playSound(SoundEvents.DEEPSLATE_TILES_STEP, 0.32F, basePitch);
        this.minecraft.player.playSound(SoundEvents.BRUSH_SAND, 0.15F, basePitch * 0.90F);
        this.minecraft.player.playSound(SoundEvents.LODESTONE_HIT, 0.17F, basePitch + 0.10F);
        this.minecraft.player.playSound(SoundEvents.AMETHYST_BLOCK_STEP, 0.12F, 1.02F + clampedRing * 0.05F);
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
