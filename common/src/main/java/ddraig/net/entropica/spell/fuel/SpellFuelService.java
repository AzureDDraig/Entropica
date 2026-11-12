package ddraig.net.entropica.spell.fuel;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.api.materia.IVaporHandler;
import ddraig.net.entropica.api.materia.MateriaStack;
import ddraig.net.entropica.block.entity.*;
import ddraig.net.entropica.component.VisWeaponState;
import ddraig.net.entropica.item.EssenceAmpouleItem;
import ddraig.net.entropica.item.EssenceItem;
import ddraig.net.entropica.item.OrbisCellItem;
import ddraig.net.entropica.item.VisFumeAmpouleItem;
import ddraig.net.entropica.registry.ModDataComponents;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Atomic multi-Materia fuel extraction service supporting:
 * <ol>
 *     <li>Player inventory fueling across multiple Orbis Cells (T2-T10), Essence Ampoules (T1),
 *         Raw Essence items (T1), and weapon-embedded Orbis Cells.</li>
 *     <li>Cross-tier conversion (0% down-conversion penalty with lossless Mfum credit buffer;
 *         +25% per-tier upward conversion markup when lower-tier cells power higher-tier spells).</li>
 *     <li>Magic Circle 8-block placed Orbis Cell scanning for Spellgem infusion rituals.</li>
 * </ol>
 */
public final class SpellFuelService {

    private static final String TAG_CREDIT_MFUM = "SpellCreditMfum";

    public record FuelCheckResult(
            boolean success,
            List<EssenceType> missingTypes,
            Map<EssenceType, Long> drainedBaseMfumByType
    ) {
    }

    public record RitualFuelResult(
            double suppliedFraction,
            boolean fullySupplied,
            Map<EssenceType, Long> suppliedMfumByType
    ) {
    }

    private SpellFuelService() {
    }

    /**
     * Finds the first available Materia type stored in any Orbis Cell / Ampoule / Essence in the player's
     * inventory, used as the dynamic fallback when {@code materiaSpecific == false}.
     */
    @Nullable
    public static EssenceType detectPrimaryAvailableMateria(Player player) {
        if (player == null) return null;
        for (ItemStack stack : collectCandidateStacks(player)) {
            EssenceType cellType = readOrbisCellType(stack);
            if (cellType != null && getAvailableCellMfum(stack) > 0L) {
                return cellType;
            }
            if (stack.getItem() instanceof EssenceAmpouleItem) {
                EssenceType ampType = EssenceAmpouleItem.getEssenceType(stack);
                if (ampType != null) return ampType;
            }
            if (stack.getItem() instanceof EssenceItem essenceItem && essenceItem.getTier() >= 1) {
                EssenceType essType = EssenceItem.getEssenceType(stack);
                if (essType != null) return essType;
            }
        }
        return null;
    }

    /**
     * Atomically checks and drains ALL required Materia types for a spell cast.
     * Creative mode players bypass fuel consumption automatically.
     */
    public static FuelCheckResult consumePlayerSpellFuel(
            Player player,
            int spellTier,
            Map<EssenceType, Long> requiredBaseMfumByType,
            boolean simulate
    ) {
        if (player.isCreative()) {
            return new FuelCheckResult(true, List.of(), requiredBaseMfumByType);
        }

        // Phase 1: Simulate every required Materia type first so partial failures never drain half a spell!
        List<EssenceType> missing = new ArrayList<>();
        for (Map.Entry<EssenceType, Long> req : requiredBaseMfumByType.entrySet()) {
            long need = req.getValue();
            if (need <= 0L) continue;
            long satisfied = drainSingleTypeFromPlayer(player, spellTier, req.getKey(), need, true);
            if (satisfied < need) {
                missing.add(req.getKey());
            }
        }

        if (!missing.isEmpty()) {
            return new FuelCheckResult(false, List.copyOf(missing), Map.of());
        }

        if (simulate) {
            return new FuelCheckResult(true, List.of(), requiredBaseMfumByType);
        }

        // Phase 2: Commit atomic multi-type drain
        Map<EssenceType, Long> drained = new LinkedHashMap<>();
        for (Map.Entry<EssenceType, Long> req : requiredBaseMfumByType.entrySet()) {
            long need = req.getValue();
            if (need <= 0L) continue;
            long got = drainSingleTypeFromPlayer(player, spellTier, req.getKey(), need, false);
            drained.put(req.getKey(), got);
        }
        return new FuelCheckResult(true, List.of(), drained);
    }

    private static long drainSingleTypeFromPlayer(
            Player player,
            int spellTier,
            EssenceType requiredType,
            long neededSpellMfum,
            boolean simulate
    ) {
        long remainingSpellMfum = neededSpellMfum;
        Inventory inv = player.getInventory();

        // 1. Prefer Orbis Cells (T2-T10) matching requiredType (highest tier first to avoid up-conversion markup)
        List<Integer> cellSlots = new ArrayList<>();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.getItem() instanceof OrbisCellItem && readOrbisCellType(s) == requiredType) {
                cellSlots.add(i);
            }
        }
        cellSlots.sort((a, b) -> Integer.compare(OrbisCellItem.getTier(inv.getItem(b)), OrbisCellItem.getTier(inv.getItem(a))));

        for (int slotIdx : cellSlots) {
            if (remainingSpellMfum <= 0L) break;
            ItemStack cellStack = inv.getItem(slotIdx);
            remainingSpellMfum -= drainFromOrbisCellStack(cellStack, spellTier, remainingSpellMfum, simulate);
        }

        // 2. Also check weapon-embedded Orbis Cell in mainhand/offhand
        if (remainingSpellMfum > 0L) {
            for (ItemStack handStack : List.of(player.getMainHandItem(), player.getOffhandItem())) {
                if (remainingSpellMfum <= 0L) break;
                VisWeaponState weaponState = handStack.get(ModDataComponents.VIS_WEAPON_STATE.get());
                if (weaponState != null && weaponState.hasOrbisSlot() && !weaponState.orbisCell().isEmpty()) {
                    ItemStack embedded = simulate ? weaponState.orbisCell().copy() : weaponState.orbisCell().copy();
                    if (readOrbisCellType(embedded) == requiredType) {
                        long satisfied = drainFromOrbisCellStack(embedded, spellTier, remainingSpellMfum, simulate);
                        if (satisfied > 0L && !simulate) {
                            handStack.set(ModDataComponents.VIS_WEAPON_STATE.get(), weaponState.withOrbisCell(embedded));
                        }
                        remainingSpellMfum -= satisfied;
                    }
                }
            }
        }

        // 3. Check Tier 1 Essence Ampoules (Small=4, Med=16, Large=64 Mraw -> *8 Mfum)
        if (remainingSpellMfum > 0L) {
            for (int i = 0; i < inv.getContainerSize() && remainingSpellMfum > 0L; i++) {
                ItemStack s = inv.getItem(i);
                if (s.getItem() instanceof EssenceAmpouleItem ampoule && EssenceAmpouleItem.getEssenceType(s) == requiredType) {
                    long mrawPerAmpoule = switch (ampoule.getTier()) {
                        case 1 -> 4L;
                        case 2 -> 16L;
                        default -> 64L;
                    };
                    long mfumPerAmpoule = mrawPerAmpoule * MateriaStageTable.getMfumPerNativeUnit(1);
                    long effectiveSpellMfumPerAmpoule = unadjustSourceMfumToSpellMfum(mfumPerAmpoule, spellTier, 1);
                    if (effectiveSpellMfumPerAmpoule <= 0L) continue;

                    int availableCount = s.getCount();
                    long neededAmpoules = Math.min(availableCount,
                            (remainingSpellMfum + effectiveSpellMfumPerAmpoule - 1L) / effectiveSpellMfumPerAmpoule);
                    long satisfied = Math.min(remainingSpellMfum, neededAmpoules * effectiveSpellMfumPerAmpoule);

                    if (!simulate && neededAmpoules > 0L) {
                        s.shrink((int) neededAmpoules);
                        ItemStack emptyAmp = switch (ampoule.getTier()) {
                            case 1 -> new ItemStack(ModItems.SMALL_AMPOULE.get(), (int) neededAmpoules);
                            case 2 -> new ItemStack(ModItems.MEDIUM_AMPOULE.get(), (int) neededAmpoules);
                            default -> new ItemStack(ModItems.LARGE_AMPOULE.get(), (int) neededAmpoules);
                        };
                        if (!inv.add(emptyAmp)) {
                            player.drop(emptyAmp, false);
                        }
                    }
                    remainingSpellMfum -= satisfied;
                }
            }
        }

        // 4. Check Tier 1 Raw Essence Items (Weak=1, Average=4, Strong=16 Mraw)
        if (remainingSpellMfum > 0L) {
            for (int i = 0; i < inv.getContainerSize() && remainingSpellMfum > 0L; i++) {
                ItemStack s = inv.getItem(i);
                if (s.getItem() instanceof EssenceItem ess && ess.getTier() >= 1 && EssenceItem.getEssenceType(s) == requiredType) {
                    long mrawPerItem = switch (ess.getTier()) {
                        case 1 -> 1L;
                        case 2 -> 4L;
                        default -> 16L;
                    };
                    long mfumPerItem = mrawPerItem * MateriaStageTable.getMfumPerNativeUnit(1);
                    long effectiveSpellMfumPerItem = unadjustSourceMfumToSpellMfum(mfumPerItem, spellTier, 1);
                    if (effectiveSpellMfumPerItem <= 0L) continue;

                    int availableCount = s.getCount();
                    long neededItems = Math.min(availableCount,
                            (remainingSpellMfum + effectiveSpellMfumPerItem - 1L) / effectiveSpellMfumPerItem);
                    long satisfied = Math.min(remainingSpellMfum, neededItems * effectiveSpellMfumPerItem);

                    if (!simulate && neededItems > 0L) {
                        s.shrink((int) neededItems);
                    }
                    remainingSpellMfum -= satisfied;
                }
            }
        }

        return neededSpellMfum - Math.max(0L, remainingSpellMfum);
    }

    /**
     * Drains from a single OrbisCellItem stack using exact Mfum remainder credit so higher-tier
     * cells never lose fractional compression when powering multi-part or lower-tier spells.
     *
     * @return how much of {@code neededSpellMfum} was satisfied (in base spell Mfum).
     */
    public static long drainFromOrbisCellStack(ItemStack cellStack, int spellTier, long neededSpellMfum, boolean simulate) {
        if (!(cellStack.getItem() instanceof OrbisCellItem) || neededSpellMfum <= 0L) return 0L;

        int cellTier = OrbisCellItem.getTier(cellStack);
        long mfumPerNative = MateriaStageTable.getMfumPerNativeUnit(cellTier);
        int storedNative = OrbisCellItem.getStoredMateriaAmount(cellStack);
        long creditMfum = getCellCreditMfum(cellStack);

        long totalCellAvailableMfum = (storedNative * mfumPerNative) + creditMfum;
        if (totalCellAvailableMfum <= 0L) return 0L;

        long requiredSourceMfum = MateriaStageTable.adjustMfumForFuelTier(neededSpellMfum, spellTier, cellTier);
        if (totalCellAvailableMfum >= requiredSourceMfum) {
            if (!simulate) {
                deductCellRawMfum(cellStack, cellTier, mfumPerNative, storedNative, creditMfum, requiredSourceMfum);
            }
            return neededSpellMfum;
        } else {
            long satisfiedSpellMfum = unadjustSourceMfumToSpellMfum(totalCellAvailableMfum, spellTier, cellTier);
            if (!simulate && satisfiedSpellMfum > 0L) {
                OrbisCellItem.setStoredMateriaAmount(cellStack, 0);
                setCellCreditMfum(cellStack, 0L);
            }
            return satisfiedSpellMfum;
        }
    }

    private static void deductCellRawMfum(ItemStack cellStack, int cellTier, long mfumPerNative,
                                          int storedNative, long creditMfum, long sourceMfumToDeduct) {
        long remaining = sourceMfumToDeduct;
        long newCredit = creditMfum;
        int newNative = storedNative;

        if (newCredit >= remaining) {
            newCredit -= remaining;
            remaining = 0L;
        } else {
            remaining -= newCredit;
            newCredit = 0L;
        }

        if (remaining > 0L) {
            long nativeUnitsToBreak = (remaining + mfumPerNative - 1L) / mfumPerNative;
            nativeUnitsToBreak = Math.min(newNative, nativeUnitsToBreak);
            newNative -= (int) nativeUnitsToBreak;
            long unlockedMfum = nativeUnitsToBreak * mfumPerNative;
            newCredit = Math.max(0L, unlockedMfum - remaining);
        }

        OrbisCellItem.setStoredMateriaAmount(cellStack, newNative);
        setCellCreditMfum(cellStack, newCredit);
    }

    private static long unadjustSourceMfumToSpellMfum(long availableSourceMfum, int spellTier, int sourceFuelTier) {
        int clampedSpell = Math.clamp(spellTier, 1, 10);
        int clampedSource = Math.clamp(sourceFuelTier, 1, 10);
        if (clampedSource >= clampedSpell) {
            return availableSourceMfum;
        }
        double markup = 1.0D + (0.25D * (clampedSpell - clampedSource));
        return (long) Math.floor(availableSourceMfum / markup);
    }

    /**
     * Scans all placed Orbis Cell / Calix / Core BlockEntities, Ritual Bowls, and Materia Vessels
     * within an 8-block radius of a Magic Circle (Section 8.3) and consumes up to the required ritual Materia per type.
     * If supplied < 100%, returns {@code fullySupplied == false} so the caller triggers the Flawed Infusion Roll!
     */
    public static RitualFuelResult consumeMagicCircleRitualFuel(
            Level level,
            BlockPos circleCenter,
            int targetSpellTier,
            Map<EssenceType, Long> requiredRitualMfumByType,
            boolean simulate
    ) {
        long totalRequired = 0L;
        for (long v : requiredRitualMfumByType.values()) totalRequired += v;
        if (totalRequired <= 0L) {
            return new RitualFuelResult(1.0D, true, Map.of());
        }

        List<BlockEntity> nearbyCells = new ArrayList<>();
        List<RitualBowlBlockEntity> nearbyBowls = new ArrayList<>();
        List<IVaporHandler> nearbyVaporVessels = new ArrayList<>();
        int radius = 8;
        for (BlockPos p : BlockPos.betweenClosed(
                circleCenter.offset(-radius, -radius, -radius),
                circleCenter.offset(radius, radius, radius))) {
            BlockEntity be = level.getBlockEntity(p);
            if (be == null) continue;
            if (DecompressionCouplerBlockEntity.getMateriaTier(be) >= 2) {
                nearbyCells.add(be);
            } else if (be instanceof RitualBowlBlockEntity bowl) {
                nearbyBowls.add(bowl);
            } else if (be instanceof IVaporHandler vapor) {
                nearbyVaporVessels.add(vapor);
            }
        }

        Map<EssenceType, Long> suppliedMap = new LinkedHashMap<>();
        long totalSupplied = 0L;

        for (Map.Entry<EssenceType, Long> req : requiredRitualMfumByType.entrySet()) {
            EssenceType neededType = req.getKey();
            long remaining = req.getValue();

            // 1. Placed Orbis Cells / Calixes / Cores (T2..T10)
            for (BlockEntity be : nearbyCells) {
                if (remaining <= 0L) break;
                long got = drainFromPlacedCellBe(be, targetSpellTier, neededType, remaining, simulate);
                if (got > 0L) {
                    suppliedMap.merge(neededType, got, Long::sum);
                    totalSupplied += got;
                    remaining -= got;
                }
            }

            // 2. Materia Fumus Vessels / Vapor Handlers (Tier 2 Mfum)
            for (IVaporHandler vapor : nearbyVaporVessels) {
                if (remaining <= 0L) break;
                MateriaStack tank = vapor.getMateriaInTank();
                if (tank.isEmpty() || tank.getType() != neededType || tank.getAmount() <= 0) continue;
                long requiredSourceMfum = MateriaStageTable.adjustMfumForFuelTier(remaining, targetSpellTier, 2);
                int toDrain = (int) Math.min(Integer.MAX_VALUE, Math.min(tank.getAmount(), requiredSourceMfum));
                if (toDrain <= 0) continue;
                MateriaStack drained = vapor.drain(toDrain, simulate);
                if (!drained.isEmpty() && drained.getAmount() > 0) {
                    long satisfied = (drained.getAmount() >= requiredSourceMfum)
                            ? remaining
                            : unadjustSourceMfumToSpellMfum(drained.getAmount(), targetSpellTier, 2);
                    suppliedMap.merge(neededType, satisfied, Long::sum);
                    totalSupplied += satisfied;
                    remaining = Math.max(0L, remaining - satisfied);
                }
            }

            // 3. Ritual Bowls holding Orbis Cells, Ampoules, or Raw Essences
            for (RitualBowlBlockEntity bowl : nearbyBowls) {
                if (remaining <= 0L) break;
                for (int slot = 0; slot < bowl.inventory.getContainerSize() && remaining > 0L; slot++) {
                    long got = drainFromContainerSlot(bowl.inventory, slot, targetSpellTier, neededType, remaining, simulate);
                    if (got > 0L) {
                        suppliedMap.merge(neededType, got, Long::sum);
                        totalSupplied += got;
                        remaining -= got;
                    }
                }
            }
        }

        double fraction = Math.clamp((double) totalSupplied / (double) totalRequired, 0.0D, 1.0D);
        boolean full = totalSupplied >= totalRequired;
        return new RitualFuelResult(fraction, full, suppliedMap);
    }

    /**
     * Detects the primary available Materia type in an Autocast Pedestal's fuel slot or directly adjacent placed cells.
     */
    @Nullable
    public static EssenceType detectPedestalAvailableMateria(Level level, BlockPos pedestalPos, Container container, int fuelSlot) {
        ItemStack stack = container.getItem(fuelSlot);
        if (!stack.isEmpty()) {
            EssenceType cellType = readOrbisCellType(stack);
            if (cellType != null && getAvailableCellMfum(stack) > 0L) return cellType;
            if (stack.getItem() instanceof EssenceAmpouleItem) {
                EssenceType t = EssenceAmpouleItem.getEssenceType(stack);
                if (t != null) return t;
            }
            if (stack.getItem() instanceof VisFumeAmpouleItem) {
                EssenceType t = VisFumeAmpouleItem.getEssenceType(stack);
                if (t != null) return t;
            }
            if (stack.getItem() instanceof EssenceItem ess && ess.getTier() >= 1) {
                EssenceType t = EssenceItem.getEssenceType(stack);
                if (t != null) return t;
            }
        }
        for (Direction dir : Direction.values()) {
            BlockEntity adj = level.getBlockEntity(pedestalPos.relative(dir));
            if (adj != null && DecompressionCouplerBlockEntity.getMateriaTier(adj) >= 2 && getPlacedCellAmount(adj) > 0) {
                EssenceType t = getPlacedCellType(adj);
                if (t != null) return t;
            }
        }
        return null;
    }

    /**
     * Atomically checks and drains spell fuel from an Autocast Pedestal's fuel slot and/or adjacent placed Orbis Cells.
     */
    public static FuelCheckResult consumePedestalSpellFuel(
            Level level,
            BlockPos pedestalPos,
            Container container,
            int fuelSlot,
            int spellTier,
            Map<EssenceType, Long> requiredBaseMfumByType,
            boolean simulate
    ) {
        List<EssenceType> missing = new ArrayList<>();
        for (Map.Entry<EssenceType, Long> req : requiredBaseMfumByType.entrySet()) {
            long need = req.getValue();
            if (need <= 0L) continue;
            long satisfied = drainSingleTypeFromPedestal(level, pedestalPos, container, fuelSlot, spellTier, req.getKey(), need, true);
            if (satisfied < need) {
                missing.add(req.getKey());
            }
        }

        if (!missing.isEmpty()) {
            return new FuelCheckResult(false, List.copyOf(missing), Map.of());
        }
        if (simulate) {
            return new FuelCheckResult(true, List.of(), requiredBaseMfumByType);
        }

        Map<EssenceType, Long> drained = new LinkedHashMap<>();
        for (Map.Entry<EssenceType, Long> req : requiredBaseMfumByType.entrySet()) {
            long need = req.getValue();
            if (need <= 0L) continue;
            long got = drainSingleTypeFromPedestal(level, pedestalPos, container, fuelSlot, spellTier, req.getKey(), need, false);
            drained.put(req.getKey(), got);
        }
        return new FuelCheckResult(true, List.of(), drained);
    }

    private static long drainSingleTypeFromPedestal(
            Level level,
            BlockPos pedestalPos,
            Container container,
            int fuelSlot,
            int spellTier,
            EssenceType neededType,
            long neededSpellMfum,
            boolean simulate
    ) {
        long remaining = neededSpellMfum;
        remaining -= drainFromContainerSlot(container, fuelSlot, spellTier, neededType, remaining, simulate);

        for (Direction dir : Direction.values()) {
            if (remaining <= 0L) break;
            BlockEntity adj = level.getBlockEntity(pedestalPos.relative(dir));
            if (adj != null && DecompressionCouplerBlockEntity.getMateriaTier(adj) >= 2) {
                remaining -= drainFromPlacedCellBe(adj, spellTier, neededType, remaining, simulate);
            }
        }
        return neededSpellMfum - Math.max(0L, remaining);
    }

    private static long drainFromPlacedCellBe(
            BlockEntity be,
            int targetSpellTier,
            EssenceType neededType,
            long remainingSpellMfum,
            boolean simulate
    ) {
        if (remainingSpellMfum <= 0L) return 0L;
        EssenceType beType = getPlacedCellType(be);
        if (beType != neededType) return 0L;

        int beTier = DecompressionCouplerBlockEntity.getMateriaTier(be);
        int storedNative = getPlacedCellAmount(be);
        if (storedNative <= 0) return 0L;

        long mfumPerNative = MateriaStageTable.getMfumPerNativeUnit(beTier);
        long availableSourceMfum = storedNative * mfumPerNative;
        long requiredSourceMfum = MateriaStageTable.adjustMfumForFuelTier(remainingSpellMfum, targetSpellTier, beTier);

        if (availableSourceMfum >= requiredSourceMfum) {
            long nativeToDrain = Math.max(1L, (requiredSourceMfum + mfumPerNative - 1L) / mfumPerNative);
            nativeToDrain = Math.min(storedNative, nativeToDrain);
            if (!simulate) {
                setPlacedCellAmount(be, storedNative - (int) nativeToDrain);
            }
            return remainingSpellMfum;
        } else {
            long satisfied = unadjustSourceMfumToSpellMfum(availableSourceMfum, targetSpellTier, beTier);
            if (!simulate && satisfied > 0L) {
                setPlacedCellAmount(be, 0);
            }
            return satisfied;
        }
    }

    private static long drainFromContainerSlot(
            Container container,
            int slot,
            int spellTier,
            EssenceType requiredType,
            long neededSpellMfum,
            boolean simulate
    ) {
        if (neededSpellMfum <= 0L) return 0L;
        ItemStack stack = container.getItem(slot);
        if (stack.isEmpty()) return 0L;

        if (stack.getItem() instanceof OrbisCellItem && readOrbisCellType(stack) == requiredType) {
            ItemStack working = simulate ? stack.copy() : stack;
            long got = drainFromOrbisCellStack(working, spellTier, neededSpellMfum, simulate);
            if (!simulate && got > 0L) {
                container.setItem(slot, working);
            }
            return got;
        }

        if (stack.getItem() instanceof VisFumeAmpouleItem fumeAmp && VisFumeAmpouleItem.getEssenceType(stack) == requiredType) {
            long mfumPerAmp = fumeAmp.getCapacity();
            long effectivePerAmp = unadjustSourceMfumToSpellMfum(mfumPerAmp, spellTier, 2);
            if (effectivePerAmp <= 0L) return 0L;
            long countNeeded = Math.min(stack.getCount(), (neededSpellMfum + effectivePerAmp - 1L) / effectivePerAmp);
            long satisfied = Math.min(neededSpellMfum, countNeeded * effectivePerAmp);
            if (!simulate && countNeeded > 0L) {
                if (stack.getCount() == countNeeded) {
                    container.setItem(slot, new ItemStack(fumeAmp.getBaseItem(), (int) countNeeded));
                } else {
                    stack.shrink((int) countNeeded);
                    container.setItem(slot, stack);
                }
            }
            return satisfied;
        }

        if (stack.getItem() instanceof EssenceAmpouleItem ampoule && EssenceAmpouleItem.getEssenceType(stack) == requiredType) {
            long mrawPerAmpoule = switch (ampoule.getTier()) {
                case 1 -> 4L;
                case 2 -> 16L;
                default -> 64L;
            };
            long mfumPerAmpoule = mrawPerAmpoule * MateriaStageTable.getMfumPerNativeUnit(1);
            long effectivePerAmp = unadjustSourceMfumToSpellMfum(mfumPerAmpoule, spellTier, 1);
            if (effectivePerAmp <= 0L) return 0L;
            long countNeeded = Math.min(stack.getCount(), (neededSpellMfum + effectivePerAmp - 1L) / effectivePerAmp);
            long satisfied = Math.min(neededSpellMfum, countNeeded * effectivePerAmp);
            if (!simulate && countNeeded > 0L) {
                ItemStack emptyAmp = switch (ampoule.getTier()) {
                    case 1 -> new ItemStack(ModItems.SMALL_AMPOULE.get(), (int) countNeeded);
                    case 2 -> new ItemStack(ModItems.MEDIUM_AMPOULE.get(), (int) countNeeded);
                    default -> new ItemStack(ModItems.LARGE_AMPOULE.get(), (int) countNeeded);
                };
                if (stack.getCount() == countNeeded) {
                    container.setItem(slot, emptyAmp);
                } else {
                    stack.shrink((int) countNeeded);
                    container.setItem(slot, stack);
                }
            }
            return satisfied;
        }

        if (stack.getItem() instanceof EssenceItem ess && ess.getTier() >= 1 && EssenceItem.getEssenceType(stack) == requiredType) {
            long mrawPerItem = switch (ess.getTier()) {
                case 1 -> 1L;
                case 2 -> 4L;
                default -> 16L;
            };
            long mfumPerItem = mrawPerItem * MateriaStageTable.getMfumPerNativeUnit(1);
            long effectivePerItem = unadjustSourceMfumToSpellMfum(mfumPerItem, spellTier, 1);
            if (effectivePerItem <= 0L) return 0L;
            long countNeeded = Math.min(stack.getCount(), (neededSpellMfum + effectivePerItem - 1L) / effectivePerItem);
            long satisfied = Math.min(neededSpellMfum, countNeeded * effectivePerItem);
            if (!simulate && countNeeded > 0L) {
                stack.shrink((int) countNeeded);
                container.setItem(slot, stack.isEmpty() ? ItemStack.EMPTY : stack);
            }
            return satisfied;
        }

        return 0L;
    }

    @Nullable
    public static EssenceType readOrbisCellType(ItemStack stack) {
        if (!(stack.getItem() instanceof OrbisCellItem)) return null;
        EssenceType t = OrbisCellItem.getStoredMateriaType(stack);
        if (t != null) return t;
        CustomData cd = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        String alt = cd.copyTag().getString("EssenceType").orElse("");
        if (!alt.isEmpty()) {
            try {
                return EssenceType.valueOf(alt);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return null;
    }

    public static void fillOrbisCellStack(ItemStack cellStack, EssenceType type, int nativeAmount) {
        if (!(cellStack.getItem() instanceof OrbisCellItem)) return;
        int tier = OrbisCellItem.getTier(cellStack);
        CompoundTag tag = cellStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putString("MateriaType", type.name());
        tag.putInt("StoredMateria" + tier, Math.max(0, nativeAmount));
        cellStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static long getAvailableCellMfum(ItemStack cellStack) {
        if (!(cellStack.getItem() instanceof OrbisCellItem)) return 0L;
        int tier = OrbisCellItem.getTier(cellStack);
        int nativeAmt = OrbisCellItem.getStoredMateriaAmount(cellStack);
        return (nativeAmt * MateriaStageTable.getMfumPerNativeUnit(tier)) + getCellCreditMfum(cellStack);
    }

    /**
     * Recharges an equipped or inventory {@link OrbisCellItem} with siphoned Materia fuel
     * (used by Nether <i>Tendril Tether</i>, Undead <i>Siphon Life</i>, and Lightning <i>Static Shroud</i>).
     * Uses the lossless {@code SpellCreditMfum} sub-unit buffer so even small siphons accumulate cleanly.
     *
     * @return actual Mfum stored into the player's Orbis Cell(s).
     */
    public static long restorePlayerMateriaFuel(Player player, EssenceType preferredType, long restoredMfum) {
        if (player == null || preferredType == null || restoredMfum <= 0L) return 0L;
        long remaining = restoredMfum;

        // Pass 1: cells already attuned to preferredType (including embedded weapon cells)
        remaining -= restoreIntoMatchingCells(player, preferredType, remaining, false);
        // Pass 2: empty cells that can accept preferredType
        if (remaining > 0L) {
            remaining -= restoreIntoMatchingCells(player, preferredType, remaining, true);
        }
        return restoredMfum - Math.max(0L, remaining);
    }

    private static long restoreIntoMatchingCells(Player player, EssenceType targetType, long mfumToStore, boolean allowEmptyCell) {
        if (mfumToStore <= 0L) return 0L;
        long storedTotal = 0L;
        Inventory inv = player.getInventory();

        for (int i = 0; i < inv.getContainerSize() && storedTotal < mfumToStore; i++) {
            ItemStack s = inv.getItem(i);
            if (!(s.getItem() instanceof OrbisCellItem)) continue;
            EssenceType curType = readOrbisCellType(s);
            if (curType == targetType || (allowEmptyCell && (curType == null || OrbisCellItem.getStoredMateriaAmount(s) == 0))) {
                storedTotal += addMfumToCellStack(s, targetType, mfumToStore - storedTotal);
            }
        }

        for (ItemStack handStack : List.of(player.getMainHandItem(), player.getOffhandItem())) {
            if (storedTotal >= mfumToStore) break;
            VisWeaponState ws = handStack.get(ModDataComponents.VIS_WEAPON_STATE.get());
            if (ws != null && ws.hasOrbisSlot() && !ws.orbisCell().isEmpty()) {
                ItemStack embedded = ws.orbisCell().copy();
                EssenceType curType = readOrbisCellType(embedded);
                if (curType == targetType || (allowEmptyCell && (curType == null || OrbisCellItem.getStoredMateriaAmount(embedded) == 0))) {
                    long added = addMfumToCellStack(embedded, targetType, mfumToStore - storedTotal);
                    if (added > 0L) {
                        handStack.set(ModDataComponents.VIS_WEAPON_STATE.get(), ws.withOrbisCell(embedded));
                        storedTotal += added;
                    }
                }
            }
        }
        return storedTotal;
    }

    public static long addMfumToCellStack(ItemStack cellStack, EssenceType type, long mfumToAdd) {
        if (!(cellStack.getItem() instanceof OrbisCellItem) || mfumToAdd <= 0L) return 0L;
        int tier = OrbisCellItem.getTier(cellStack);
        int maxNative = OrbisCellItem.getMaxMateria(cellStack);
        int curNative = OrbisCellItem.getStoredMateriaAmount(cellStack);
        long mfumPerNative = MateriaStageTable.getMfumPerNativeUnit(tier);
        long curCredit = getCellCreditMfum(cellStack);

        long maxTotalMfum = (long) maxNative * mfumPerNative;
        long curTotalMfum = ((long) curNative * mfumPerNative) + curCredit;
        if (curTotalMfum >= maxTotalMfum) return 0L;

        long accepted = Math.min(mfumToAdd, maxTotalMfum - curTotalMfum);
        long newTotal = curTotalMfum + accepted;
        int newNative = (int) Math.min(maxNative, newTotal / mfumPerNative);
        long newCredit = (newNative >= maxNative) ? 0L : (newTotal % mfumPerNative);

        fillOrbisCellStack(cellStack, type, newNative);
        setCellCreditMfum(cellStack, newCredit);
        return accepted;
    }

    private static long getCellCreditMfum(ItemStack cellStack) {
        CustomData cd = cellStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return cd.copyTag().getLong(TAG_CREDIT_MFUM).orElse(0L);
    }

    private static void setCellCreditMfum(ItemStack cellStack, long credit) {
        CompoundTag tag = cellStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (credit <= 0L) {
            tag.remove(TAG_CREDIT_MFUM);
        } else {
            tag.putLong(TAG_CREDIT_MFUM, credit);
        }
        cellStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private static List<ItemStack> collectCandidateStacks(Player player) {
        List<ItemStack> list = new ArrayList<>();
        list.add(player.getOffhandItem());
        list.add(player.getMainHandItem());
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            list.add(inv.getItem(i));
        }
        return list;
    }

    @Nullable
    private static EssenceType getPlacedCellType(BlockEntity be) {
        if (be instanceof OrbisCellBlockEntity c) return c.getManaType();
        if (be instanceof SublimatedOrbisCellBlockEntity c) return c.getManaType();
        if (be instanceof PneumaticCalixBlockEntity c) return c.getManaType();
        if (be instanceof VoltaicCalixBlockEntity c) return c.getManaType();
        if (be instanceof MatrixCalixBlockEntity c) return c.getManaType();
        if (be instanceof ThecaCellBlockEntity c) return c.getManaType();
        if (be instanceof VasCellBlockEntity c) return c.getManaType();
        if (be instanceof MonadCoreBlockEntity c) return c.getManaType();
        if (be instanceof AthanorCoreBlockEntity c) return c.getManaType();
        return null;
    }

    private static int getPlacedCellAmount(BlockEntity be) {
        if (be instanceof OrbisCellBlockEntity c) return c.getMateria();
        if (be instanceof SublimatedOrbisCellBlockEntity c) return c.getMateria();
        if (be instanceof PneumaticCalixBlockEntity c) return c.getMateria();
        if (be instanceof VoltaicCalixBlockEntity c) return c.getMateria();
        if (be instanceof MatrixCalixBlockEntity c) return c.getMateria();
        if (be instanceof ThecaCellBlockEntity c) return c.getMateria();
        if (be instanceof VasCellBlockEntity c) return c.getMateria();
        if (be instanceof MonadCoreBlockEntity c) return c.getMateria();
        if (be instanceof AthanorCoreBlockEntity c) return c.getMateria();
        return 0;
    }

    private static void setPlacedCellAmount(BlockEntity be, int amount) {
        if (be instanceof OrbisCellBlockEntity c) c.setMateria(amount);
        else if (be instanceof SublimatedOrbisCellBlockEntity c) c.setMateria(amount);
        else if (be instanceof PneumaticCalixBlockEntity c) c.setMateria(amount);
        else if (be instanceof VoltaicCalixBlockEntity c) c.setMateria(amount);
        else if (be instanceof MatrixCalixBlockEntity c) c.setMateria(amount);
        else if (be instanceof ThecaCellBlockEntity c) c.setMateria(amount);
        else if (be instanceof VasCellBlockEntity c) c.setMateria(amount);
        else if (be instanceof MonadCoreBlockEntity c) c.setMateria(amount);
        else if (be instanceof AthanorCoreBlockEntity c) c.setMateria(amount);
    }
}
