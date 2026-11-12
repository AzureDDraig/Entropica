package ddraig.net.entropica.block.entity;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.api.materia.IVaporHandler;
import ddraig.net.entropica.api.materia.MateriaFumusStack;
import ddraig.net.entropica.api.materia.MateriaStack;
import ddraig.net.entropica.item.AstralCrystalItem;
import ddraig.net.entropica.registry.ModBlockEntities;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;

public class MateriaFluxDistributorBlockEntity extends BlockEntity implements IVaporHandler {

    private int tier = 1; // Tiers 1 to 3
    private final List<BlockPos> cachedPedestals = new ArrayList<>();
    private final List<BlockPos> cachedWirelessReceivers = new ArrayList<>();
    private int storedFlux = 0;
    private MateriaFumusStack storedFume = MateriaFumusStack.EMPTY;
    private int animationTicks = 0;

    public MateriaFluxDistributorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MATERIA_FLUX_DISTRIBUTOR_BE.get(), pos, state);
    }

    public int getDistributorTier() {
        return tier;
    }

    public int getStoredFlux() {
        return storedFlux;
    }

    public void receiveFluxBeam(int flux) {
        if (flux <= 0) return;
        this.storedFlux = Math.min(getMaxCapacity(), this.storedFlux + flux);
        setChanged();
        if (level != null && !level.isClientSide() && level.getGameTime() % 20 == 0) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public boolean upgradeTier() {
        if (this.tier < 3) {
            this.tier++;
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                level.playSound(null, worldPosition, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0f, 1.2f + (this.tier * 0.2f));
            }
            return true;
        }
        return false;
    }

    public int getDistributionRadius() {
        return switch (this.tier) {
            case 2 -> 64;
            case 3 -> 96;
            default -> 32;
        };
    }

    public int getMaxCapacity() {
        return switch (this.tier) {
            case 2 -> 8000;
            case 3 -> 16000;
            default -> 4000;
        };
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MateriaFluxDistributorBlockEntity be) {
        be.animationTicks++;

        if (level.isClientSide()) return;

        boolean changed = false;

        // 1. Siphon directly from an Astral Collector placed directly above
        BlockEntity aboveBe = level.getBlockEntity(pos.above());
        if (aboveBe instanceof AstralCollectorBlockEntity collector) {
            if (collector.getStoredMateria() > 0 && be.getStoredAmount() < be.getMaxCapacity()) {
                int space = be.getMaxCapacity() - be.getStoredAmount();
                int maxDrain = Math.min(space, 20);
                int drained = collector.drainMateria(maxDrain);
                if (drained > 0) {
                    if (be.storedFume.isEmpty()) {
                        be.storedFume = new MateriaFumusStack(collector.getStoredEssence(), drained);
                    } else {
                        be.storedFume.grow(drained);
                    }
                    changed = true;
                }
            }
        }

        // 2. Refresh cached pedestals and wireless flux receivers periodically
        if (level.getGameTime() % 100 == 0) {
            be.cachedPedestals.clear();
            be.cachedWirelessReceivers.clear();
            int scanR = Math.min(be.getDistributionRadius(), 48);
            BlockPos minP = pos.offset(-scanR, -16, -scanR);
            BlockPos maxP = pos.offset(scanR, 16, scanR);
            for (BlockPos p : BlockPos.betweenClosed(minP, maxP)) {
                BlockEntity targetBe = level.getBlockEntity(p);
                if (targetBe instanceof AttunementPedestalBlockEntity) {
                    be.cachedPedestals.add(p.immutable());
                } else if (targetBe instanceof ddraig.net.entropica.api.starlight.IWirelessFluxReceiver) {
                    be.cachedWirelessReceivers.add(p.immutable());
                }
            }
        }

        // 3. Wireless Materia-Flux Distribution to Inscribed Astral Crystals & Cosmetic Receivers
        if (be.getStoredAmount() > 0 && level.getGameTime() % 10 == 0) {
            int radius = be.getDistributionRadius();
            int transferRate = switch (be.tier) {
                case 2 -> 25;
                case 3 -> 60;
                default -> 10;
            };

            AABB searchBox = new AABB(pos).inflate(radius);

            // Target search 1: scan players in area with crystals
            for (Player player : level.getEntitiesOfClass(Player.class, searchBox)) {
                if (be.getStoredAmount() <= 0) break;
                for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                    ItemStack stack = player.getInventory().getItem(slot);
                    if (chargeCrystalStack(stack, be, transferRate, level, pos, player.blockPosition())) {
                        changed = true;
                        if (be.getStoredAmount() <= 0) break;
                    }
                }
            }

            // Target search 2: scan nearby cached Attunement Pedestals
            if (be.getStoredAmount() > 0) {
                for (BlockPos pPos : be.cachedPedestals) {
                    if (be.getStoredAmount() <= 0) break;
                    if (pos.distSqr(pPos) <= (radius * radius)) {
                        BlockEntity pBe = level.getBlockEntity(pPos);
                        if (pBe instanceof AttunementPedestalBlockEntity pedestal) {
                            ItemStack pStack = pedestal.getHeldItem();
                            if (chargeCrystalStack(pStack, be, transferRate, level, pos, pPos)) {
                                pedestal.setChanged();
                                level.sendBlockUpdated(pPos, pedestal.getBlockState(), pedestal.getBlockState(), 3);
                                changed = true;
                            }
                        }
                    }
                }
            }

            // Target search 3: broadcast wireless starlight flux to nearby cosmetic & functional receivers
            if (be.getStoredAmount() > 0 && !be.cachedWirelessReceivers.isEmpty()) {
                EssenceType distEssence = !be.storedFume.isEmpty() ? be.storedFume.getType() : EssenceType.ASTRAL;
                String starName = distEssence != null ? distEssence.name() : "Astral";
                for (BlockPos rPos : be.cachedWirelessReceivers) {
                    if (pos.distSqr(rPos) <= (radius * radius)) {
                        BlockEntity rBe = level.getBlockEntity(rPos);
                        if (rBe instanceof ddraig.net.entropica.api.starlight.IWirelessFluxReceiver receiver) {
                            receiver.receiveFluxPulse(starName, distEssence);
                        }
                    }
                }
            }
        }

        if (changed || level.getGameTime() % 40 == 0) {
            be.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    private static boolean chargeCrystalStack(ItemStack stack, MateriaFluxDistributorBlockEntity be, int maxChargeAmount, Level level, BlockPos sourcePos, BlockPos targetPos) {
        if (stack.isEmpty() || !stack.is(ModItems.ASTRAL_CRYSTAL.get())) return false;

        // Must be an inscribed crystal
        if (AstralCrystalItem.getRitual(stack) == null) return false;

        int maxCap = AstralCrystalItem.getMaxMateria(stack);
        int currentCharge = AstralCrystalItem.getStoredMateria(stack);

        if (currentCharge < maxCap && be.getStoredAmount() > 0) {
            int needed = maxCap - currentCharge;
            int toTransfer = Math.min(needed, Math.min(maxChargeAmount, be.getStoredAmount()));
            if (toTransfer > 0) {
                // Drain from stored celestial flux first, then fallback to fumus
                if (be.storedFlux >= toTransfer) {
                    be.storedFlux -= toTransfer;
                } else {
                    int fromFlux = be.storedFlux;
                    be.storedFlux = 0;
                    be.storedFume.shrink(toTransfer - fromFlux);
                }
                AstralCrystalItem.setStoredMateria(stack, currentCharge + toTransfer);
                be.setChanged();

                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.ENCHANT, sourcePos.getX() + 0.5, sourcePos.getY() + 0.8, sourcePos.getZ() + 0.5, 3, 0.2, 0.2, 0.2, 0.05);
                    serverLevel.sendParticles(ParticleTypes.END_ROD, targetPos.getX() + 0.5, targetPos.getY() + 1.1, targetPos.getZ() + 0.5, 2, 0.1, 0.1, 0.1, 0.02);
                }
                return true;
            }
        }
        return false;
    }

    public int getStoredAmount() {
        return this.storedFlux + (this.storedFume.isEmpty() ? 0 : this.storedFume.getAmount());
    }

    // --- IVaporHandler Implementation ---

    @Override
    public int getSafeCapacity() {
        return getMaxCapacity();
    }

    @Override
    public int getAbsoluteCapacity() {
        return getMaxCapacity() * 2;
    }

    @Override
    public @NotNull MateriaStack getMateriaInTank() {
        return this.storedFume;
    }

    @Override
    public int fill(MateriaStack resource, boolean simulate) {
        if (resource.isEmpty() || !(resource instanceof MateriaFumusStack fumus)) return 0;
        if (!this.storedFume.isEmpty() && this.storedFume.getType() != resource.getType()) return 0;

        int space = getSafeCapacity() - getStoredAmount();
        int toFill = Math.min(space, resource.getAmount());
        if (!simulate && toFill > 0) {
            if (this.storedFume.isEmpty()) {
                this.storedFume = new MateriaFumusStack(resource.getType(), toFill);
            } else {
                this.storedFume.grow(toFill);
            }
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
        return toFill;
    }

    @Override
    public @NotNull MateriaStack drain(int maxDrain, boolean simulate) {
        if (this.storedFume.isEmpty() || maxDrain <= 0) return MateriaFumusStack.EMPTY;
        int toDrain = Math.min(maxDrain, this.storedFume.getAmount());
        MateriaStack drained = new MateriaFumusStack(this.storedFume.getType(), toDrain);
        if (!simulate) {
            this.storedFume.shrink(toDrain);
            if (this.storedFume.getAmount() <= 0) {
                this.storedFume = MateriaFumusStack.EMPTY;
            }
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
        return drained;
    }

    // --- NBT Storage ---

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("Tier", Codec.INT, this.tier);
        output.store("StoredFlux", Codec.INT, this.storedFlux);
        if (!this.storedFume.isEmpty()) {
            output.store("FumeType", Codec.STRING, this.storedFume.getType().name());
            output.store("FumeAmount", Codec.INT, this.storedFume.getAmount());
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.read("Tier", Codec.INT).ifPresent(t -> this.tier = t);
        this.storedFlux = input.read("StoredFlux", Codec.INT).orElse(0);
        if (input.read("FumeType", Codec.STRING).isPresent() && input.read("FumeAmount", Codec.INT).isPresent()) {
            String typeStr = input.read("FumeType", Codec.STRING).get();
            int amount = input.read("FumeAmount", Codec.INT).get();
            try {
                this.storedFume = new MateriaFumusStack(EssenceType.valueOf(typeStr), amount);
            } catch (Exception e) {
                this.storedFume = MateriaFumusStack.EMPTY;
            }
        } else {
            this.storedFume = MateriaFumusStack.EMPTY;
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider p) {
        CompoundTag tag = super.getUpdateTag(p);
        tag.putInt("Tier", this.tier);
        tag.putInt("StoredFlux", this.storedFlux);
        tag.putInt("FumeAmount", getStoredAmount());
        if (!this.storedFume.isEmpty()) {
            tag.putString("FumeType", this.storedFume.getType().name());
        }
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
