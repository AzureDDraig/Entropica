package ddraig.net.entropica.block.entity;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.item.EssenceAmpouleItem;
import ddraig.net.entropica.item.EssenceItem;
import ddraig.net.entropica.item.OrbisCellItem;
import ddraig.net.entropica.item.VisFumeAmpouleItem;
import ddraig.net.entropica.registry.ModBlockEntities;
import ddraig.net.entropica.registry.ModItems;
import ddraig.net.entropica.spell.fuel.SpellFuelService;
import ddraig.net.entropica.spell.research.SpellResearchManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Research Bench BlockEntity — finalizes {@code Observed -> Unlocked} spell nodes and
 * element-node mutations via a 5-second ({@code 100}-tick) study timer (Spellcrafting Guide Section 3.8).
 */
public class ResearchBenchBlockEntity extends BlockEntity {

    public static final int STUDY_DURATION_TICKS = 100;

    private int studyTicksRemaining = 0;
    @Nullable
    private UUID studyingPlayerUuid = null;
    private boolean activeIsMutation = false;
    private String activeTargetKey = "";
    private String activeDisplayLabel = "";
    @Nullable
    private EssenceType activeRequiredMateria = null;

    public ResearchBenchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RESEARCH_BENCH_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ResearchBenchBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel) || be.studyTicksRemaining <= 0) {
            return;
        }

        be.studyTicksRemaining--;
        if (be.studyTicksRemaining % 5 == 0) {
            serverLevel.sendParticles(
                    ParticleTypes.ENCHANT,
                    pos.getX() + 0.5D,
                    pos.getY() + 1.15D,
                    pos.getZ() + 0.5D,
                    6,
                    0.35D,
                    0.25D,
                    0.35D,
                    0.08D
            );
        }

        if (be.studyTicksRemaining == 0) {
            ServerPlayer player = be.studyingPlayerUuid != null
                    ? serverLevel.getServer().getPlayerList().getPlayer(be.studyingPlayerUuid)
                    : null;
            if (player != null && !be.activeTargetKey.isEmpty()) {
                SpellResearchManager.PendingStudyTarget target = new SpellResearchManager.PendingStudyTarget(
                        be.activeIsMutation,
                        be.activeTargetKey,
                        be.activeDisplayLabel,
                        be.activeRequiredMateria
                );
                SpellResearchManager.finalizeObservedTarget(player, target);
                serverLevel.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.25F);
                serverLevel.sendParticles(
                        ParticleTypes.END_ROD,
                        pos.getX() + 0.5D,
                        pos.getY() + 1.2D,
                        pos.getZ() + 0.5D,
                        18,
                        0.3D,
                        0.3D,
                        0.3D,
                        0.04D
                );
                player.displayClientMessage(
                        Component.literal("§a[Research Unlocked!] §fMastered §b" + be.activeDisplayLabel
                                + " §fin the Entropic Codex!"),
                        true
                );
            }
            be.studyingPlayerUuid = null;
            be.activeTargetKey = "";
            be.activeDisplayLabel = "";
            be.activeRequiredMateria = null;
            be.setChanged();
        }
    }

    public void startOrCheckStudy(ServerLevel level, Player player) {
        if (this.studyTicksRemaining > 0) {
            float secLeft = this.studyTicksRemaining / 20.0F;
            player.displayClientMessage(
                    Component.literal(String.format("§d[Research Bench] §7Studying §b%s §7(%.1fs remaining)...",
                            this.activeDisplayLabel, secLeft)),
                    true
            );
            return;
        }

        List<SpellResearchManager.PendingStudyTarget> pending = SpellResearchManager.getPendingObservedTargets(player);
        if (pending.isEmpty()) {
            player.displayClientMessage(
                    Component.literal("§7[Research Bench] No pending Observed spell parts or mutations. Cast a cryptic or mutated spell first!"),
                    true
            );
            return;
        }

        // Pick the first pending target for which the player holds valid materials (or first target overall to show requirements)
        SpellResearchManager.PendingStudyTarget chosen = null;
        for (SpellResearchManager.PendingStudyTarget candidate : pending) {
            if (player.isCreative() || tryConsumeStudyMaterials(player, candidate.requiredMateriaSample(), true)) {
                chosen = candidate;
                break;
            }
        }

        if (chosen == null) {
            SpellResearchManager.PendingStudyTarget first = pending.getFirst();
            String sampleHint = first.requiredMateriaSample() != null
                    ? first.requiredMateriaSample().getDisplayName() + " Materia sample"
                    : "any Materia Essence/Ampoule/Cell sample";
            player.displayClientMessage(
                    Component.literal("§e[Research Bench] §7To finalize §b" + first.displayLabel()
                            + "§7, carry: §f1x Scribing Ink + 1x Blank Parchment + " + sampleHint + "§7!"),
                    true
            );
            return;
        }

        if (!player.isCreative()) {
            tryConsumeStudyMaterials(player, chosen.requiredMateriaSample(), false);
        }

        this.studyingPlayerUuid = player.getUUID();
        this.activeIsMutation = chosen.isMutation();
        this.activeTargetKey = chosen.targetKey();
        this.activeDisplayLabel = chosen.displayLabel();
        this.activeRequiredMateria = chosen.requiredMateriaSample();
        this.studyTicksRemaining = STUDY_DURATION_TICKS;
        this.setChanged();

        level.playSound(null, this.worldPosition, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1.0F, 1.1F);
        player.displayClientMessage(
                Component.literal("§d[Research Bench] §fBegan 5.0s Codex study of §b" + chosen.displayLabel() + "§f..."),
                true
        );
    }

    public static boolean tryConsumeStudyMaterials(Player player, @Nullable EssenceType requiredSample, boolean simulate) {
        Inventory inv = player.getInventory();
        int inkSlot = -1;
        int parchmentSlot = -1;
        int sampleSlot = -1;
        int sampleKind = 0; // 1=EssenceItem, 2=EssenceAmpoule, 3=FumeAmpoule, 4=OrbisCell

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.isEmpty()) continue;

            if (inkSlot < 0 && s.is(ModItems.SCRIBING_INK.get())) {
                inkSlot = i;
                continue;
            }
            if (parchmentSlot < 0 && (s.is(ModItems.BLANK_PARCHMENT.get()) || s.is(Items.PAPER))) {
                parchmentSlot = i;
                continue;
            }
            if (sampleSlot < 0) {
                if (s.getItem() instanceof EssenceItem ess && ess.getTier() >= 1) {
                    EssenceType t = EssenceItem.getEssenceType(s);
                    if (requiredSample == null || t == requiredSample) {
                        sampleSlot = i;
                        sampleKind = 1;
                    }
                } else if (s.getItem() instanceof EssenceAmpouleItem) {
                    EssenceType t = EssenceAmpouleItem.getEssenceType(s);
                    if (requiredSample == null || t == requiredSample) {
                        sampleSlot = i;
                        sampleKind = 2;
                    }
                } else if (s.getItem() instanceof VisFumeAmpouleItem) {
                    EssenceType t = VisFumeAmpouleItem.getEssenceType(s);
                    if (requiredSample == null || t == requiredSample) {
                        sampleSlot = i;
                        sampleKind = 3;
                    }
                } else if (s.getItem() instanceof OrbisCellItem && SpellFuelService.getAvailableCellMfum(s) >= 8L) {
                    EssenceType t = SpellFuelService.readOrbisCellType(s);
                    if (t != null && (requiredSample == null || t == requiredSample)) {
                        sampleSlot = i;
                        sampleKind = 4;
                    }
                }
            }
        }

        if (inkSlot < 0 || parchmentSlot < 0 || sampleSlot < 0) {
            return false;
        }
        if (simulate) {
            return true;
        }

        inv.getItem(inkSlot).shrink(1);
        inv.getItem(parchmentSlot).shrink(1);

        ItemStack sampleStack = inv.getItem(sampleSlot);
        switch (sampleKind) {
            case 1 -> sampleStack.shrink(1);
            case 2 -> {
                EssenceAmpouleItem amp = (EssenceAmpouleItem) sampleStack.getItem();
                sampleStack.shrink(1);
                ItemStack empty = switch (amp.getTier()) {
                    case 1 -> new ItemStack(ModItems.SMALL_AMPOULE.get());
                    case 2 -> new ItemStack(ModItems.MEDIUM_AMPOULE.get());
                    default -> new ItemStack(ModItems.LARGE_AMPOULE.get());
                };
                if (!inv.add(empty)) player.drop(empty, false);
            }
            case 3 -> {
                VisFumeAmpouleItem amp = (VisFumeAmpouleItem) sampleStack.getItem();
                sampleStack.shrink(1);
                ItemStack empty = new ItemStack(amp.getBaseItem());
                if (!inv.add(empty)) player.drop(empty, false);
            }
            case 4 -> SpellFuelService.drainFromOrbisCellStack(sampleStack, 1, 8L, false);
        }
        return true;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("StudyTicksRemaining", this.studyTicksRemaining);
        output.putBoolean("ActiveIsMutation", this.activeIsMutation);
        if (!this.activeTargetKey.isEmpty()) {
            output.store("ActiveTargetKey", Codec.STRING, this.activeTargetKey);
            output.store("ActiveDisplayLabel", Codec.STRING, this.activeDisplayLabel);
        }
        if (this.studyingPlayerUuid != null) {
            output.store("StudyingPlayerUuid", UUIDUtil.CODEC, this.studyingPlayerUuid);
        }
        if (this.activeRequiredMateria != null) {
            output.store("ActiveRequiredMateria", Codec.STRING, this.activeRequiredMateria.name());
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.studyTicksRemaining = Math.max(0, input.getIntOr("StudyTicksRemaining", 0));
        this.activeIsMutation = input.getBooleanOr("ActiveIsMutation", false);
        this.activeTargetKey = input.read("ActiveTargetKey", Codec.STRING).orElse("");
        this.activeDisplayLabel = input.read("ActiveDisplayLabel", Codec.STRING).orElse("");
        this.studyingPlayerUuid = input.read("StudyingPlayerUuid", UUIDUtil.CODEC).orElse(null);
        String reqMat = input.read("ActiveRequiredMateria", Codec.STRING).orElse("");
        if (!reqMat.isEmpty()) {
            try {
                this.activeRequiredMateria = EssenceType.valueOf(reqMat);
            } catch (IllegalArgumentException ignored) {
                this.activeRequiredMateria = null;
            }
        } else {
            this.activeRequiredMateria = null;
        }
    }
}
