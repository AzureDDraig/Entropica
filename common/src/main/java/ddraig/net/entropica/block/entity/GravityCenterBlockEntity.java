package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.api.materia.IVaporHandler;
import ddraig.net.entropica.api.materia.MateriaFumusStack;
import ddraig.net.entropica.api.materia.MateriaStack;
import ddraig.net.entropica.block.GravityCenterBlock;
import ddraig.net.entropica.gravity.GravityApi;
import ddraig.net.entropica.registry.ModBlockEntities;
import ddraig.net.entropica.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

public class GravityCenterBlockEntity extends BlockEntity implements IVaporHandler {

    public static final Set<GravityCenterBlockEntity> ACTIVE_CENTERS = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public static GravityCenterBlockEntity getAffectingCenter(Level level, Vec3 pos) {
        if (level == null || pos == null) return null;
        for (GravityCenterBlockEntity center : ACTIVE_CENTERS) {
            if (center.isRemoved() || center.getLevel() != level) continue;
            if (!center.isActive()) continue;
            double distSqr = center.getBlockPos().getCenter().distanceToSqr(pos);
            double r = center.getRadius();
            if (distSqr <= r * r) {
                return center;
            }
        }
        return null;
    }

    protected int radius = 16;
    protected EssenceType storedType = null;
    protected int storedAmount = 0;
    protected int consumptionTicker = 0;

    // Animation & rendering states
    public float ringRotationX = 0.0F;
    public float ringRotationY = 0.0F;
    public float ringRotationZ = 0.0F;
    public float coreSpin = 0.0F;

    public float prevRingX = 0.0F;
    public float prevRingY = 0.0F;
    public float prevRingZ = 0.0F;
    public float prevCoreSpin = 0.0F;

    public GravityCenterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GRAVITY_CENTER_BE.get(), pos, state);
        if (state.hasProperty(GravityCenterBlock.RADIUS_LEVEL)) {
            this.radius = GravityCenterBlock.getRadiusForLevel(state.getValue(GravityCenterBlock.RADIUS_LEVEL));
        }
    }

    public GravityCenterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        if (state.hasProperty(GravityCenterBlock.RADIUS_LEVEL)) {
            this.radius = GravityCenterBlock.getRadiusForLevel(state.getValue(GravityCenterBlock.RADIUS_LEVEL));
        }
    }

    public boolean isCreative() {
        return false;
    }

    public boolean isActive() {
        if (level == null) return false;
        BlockState state = getBlockState();
        boolean powered = state.hasProperty(GravityCenterBlock.POWERED) && state.getValue(GravityCenterBlock.POWERED);
        if (powered) return false;
        return isCreative() || storedAmount > 0;
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        boolean active = isActive();
        if (active) {
            ACTIVE_CENTERS.add(this);
        } else {
            ACTIVE_CENTERS.remove(this);
        }

        if (level.isClientSide()) {
            prevRingX = ringRotationX;
            prevRingY = ringRotationY;
            prevRingZ = ringRotationZ;
            prevCoreSpin = coreSpin;

            if (active) {
                ringRotationX += 2.5F;
                ringRotationY += 3.8F;
                ringRotationZ += 1.6F;
                coreSpin += 6.0F;

                if (level.random.nextInt(3) == 0) {
                    double angle = level.random.nextDouble() * Math.PI * 2.0;
                    double r = 0.8 + level.random.nextDouble() * 0.6;
                    double px = pos.getX() + 0.5 + Math.cos(angle) * r;
                    double py = pos.getY() + 0.5 + (level.random.nextDouble() - 0.5) * 0.8;
                    double pz = pos.getZ() + 0.5 + Math.sin(angle) * r;
                    double vx = (pos.getX() + 0.5 - px) * 0.08;
                    double vy = (pos.getY() + 0.5 - py) * 0.08;
                    double vz = (pos.getZ() + 0.5 - pz) * 0.08;
                    level.addParticle(ModParticles.SPECTRUM_SPARKLE.get(), px, py, pz, vx, vy, vz);
                }
            }
            return;
        }

        // Server-side logic
        if (!isCreative() && storedAmount < getSafeCapacity()) {
            // Draw Materia Fumus from neighboring pipes and tanks
            for (Direction dir : Direction.values()) {
                if (storedAmount >= getSafeCapacity()) break;
                BlockEntity neighbor = level.getBlockEntity(pos.relative(dir));
                if (neighbor instanceof IVaporHandler handler && !(neighbor instanceof GravityCenterBlockEntity)) {
                    int needed = getSafeCapacity() - storedAmount;
                    MateriaStack drained = handler.drain(needed, false);
                    if (!drained.isEmpty() && drained instanceof MateriaFumusStack) {
                        this.storedType = drained.getType();
                        this.storedAmount += drained.getAmount();
                        this.setChanged();
                        level.sendBlockUpdated(pos, state, state, 3);
                    }
                }
            }
        }

        if (active) {
            // Fuel consumption for fueled variant: 1 mB per 40 ticks
            if (!isCreative()) {
                consumptionTicker++;
                if (consumptionTicker >= 40) {
                    consumptionTicker = 0;
                    if (storedAmount > 0) {
                        storedAmount--;
                        if (storedAmount == 0) {
                            storedType = null;
                        }
                        this.setChanged();
                        level.sendBlockUpdated(pos, state, state, 3);
                    }
                }
            }

            // Omnidirectional Point-Gravity Core Physics
            Vec3 center = pos.getCenter();
            AABB fieldArea = new AABB(pos).inflate(radius);
            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, fieldArea);

            for (LivingEntity entity : entities) {
                if (GravityApi.isAnchored(entity)) continue;

                Vec3 entityCenter = entity.position().add(0, entity.getBbHeight() * 0.5, 0);
                Vec3 toCenter = center.subtract(entityCenter);
                double dist = toCenter.length();

                if (dist > 0.35 && dist <= radius) {
                    Vec3 pullDir = toCenter.normalize();
                    double accel = 0.065;
                    Vec3 pull = pullDir.scale(accel);

                    entity.setDeltaMovement(entity.getDeltaMovement().add(pull));
                    entity.resetFallDistance();
                    entity.hurtMarked = true;

                    // Southern hemisphere / underside 180° inversion for players walking around the core
                    if (entity instanceof Player player) {
                        if (player.getY() < center.y - 0.45) {
                            if (!GravityApi.SOLES_INVERTED_ENTITIES.contains(player.getUUID())) {
                                GravityApi.SOLES_INVERTED_ENTITIES.add(player.getUUID());
                                GravityApi.setGravity(player, GravityApi.INVERTED_GRAVITY);
                            }
                        } else if (player.getY() > center.y + 0.45) {
                            if (GravityApi.SOLES_INVERTED_ENTITIES.contains(player.getUUID()) && !GravityApi.hasGravitonSoles(player)) {
                                GravityApi.SOLES_INVERTED_ENTITIES.remove(player.getUUID());
                                GravityApi.resetGravity(player);
                            }
                        }
                    }
                }
            }

            // Field exit cleanup: restore normal gravity if an inverted player without soles leaves the gravity center radius
            for (java.util.UUID uuid : GravityApi.SOLES_INVERTED_ENTITIES) {
                Player player = level.getPlayerByUUID(uuid);
                if (player != null && !GravityApi.hasGravitonSoles(player)) {
                    if (getAffectingCenter(level, player.position()) == null) {
                        GravityApi.SOLES_INVERTED_ENTITIES.remove(uuid);
                        GravityApi.resetGravity(player);
                    }
                }
            }
        }
    }

    public float getInterpolatedRingX(float partialTick) {
        return Mth.rotLerp(partialTick, prevRingX, ringRotationX);
    }

    public float getInterpolatedRingY(float partialTick) {
        return Mth.rotLerp(partialTick, prevRingY, ringRotationY);
    }

    public float getInterpolatedRingZ(float partialTick) {
        return Mth.rotLerp(partialTick, prevRingZ, ringRotationZ);
    }

    public float getInterpolatedCoreSpin(float partialTick) {
        return Mth.rotLerp(partialTick, prevCoreSpin, coreSpin);
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
        this.setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public int getStoredAmount() {
        return storedAmount;
    }

    public EssenceType getStoredType() {
        return storedType;
    }

    // --- IVaporHandler ---
    @Override
    public int fill(MateriaStack resource, boolean simulate) {
        if (isCreative()) return resource.getAmount();
        if (resource.isEmpty() || !(resource instanceof MateriaFumusStack)) return 0;
        if (this.storedType != null && this.storedType != resource.getType()) return 0;

        int space = getSafeCapacity() - this.storedAmount;
        if (space <= 0) return 0;

        int toFill = Math.min(resource.getAmount(), space);
        if (!simulate && toFill > 0) {
            this.storedType = resource.getType();
            this.storedAmount += toFill;
            this.setChanged();
            if (this.level != null && !this.level.isClientSide()) {
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            }
        }
        return toFill;
    }

    @NotNull
    @Override
    public MateriaStack drain(int maxDrain, boolean simulate) {
        if (this.storedAmount <= 0 || this.storedType == null) return MateriaFumusStack.EMPTY;
        int toDrain = Math.min(this.storedAmount, maxDrain);
        MateriaStack result = new MateriaFumusStack(this.storedType, toDrain);
        if (!simulate && toDrain > 0 && !isCreative()) {
            this.storedAmount -= toDrain;
            if (this.storedAmount <= 0) {
                this.storedType = null;
            }
            this.setChanged();
            if (this.level != null && !this.level.isClientSide()) {
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            }
        }
        return result;
    }

    @NotNull
    @Override
    public MateriaStack getMateriaInTank() {
        if (isCreative()) return new MateriaFumusStack(EssenceType.VOID, 2000);
        if (this.storedType == null || this.storedAmount <= 0) return MateriaFumusStack.EMPTY;
        return new MateriaFumusStack(this.storedType, this.storedAmount);
    }

    @Override
    public int getSafeCapacity() {
        return 2000;
    }

    @Override
    public int getAbsoluteCapacity() {
        return 3000;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Radius", this.radius);
        if (this.storedType != null) {
            output.putInt("StoredType", this.storedType.ordinal());
        }
        output.putInt("StoredAmount", this.storedAmount);
        output.putInt("ConsumptionTicker", this.consumptionTicker);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.radius = input.getIntOr("Radius", 16);
        if (input.getInt("StoredType").isPresent()) {
            int ord = input.getInt("StoredType").get();
            this.storedType = EssenceType.values()[ord % EssenceType.values().length];
        } else {
            this.storedType = null;
        }
        this.storedAmount = input.getIntOr("StoredAmount", 0);
        this.consumptionTicker = input.getIntOr("ConsumptionTicker", 0);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider p) {
        return this.saveWithoutMetadata(p);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void setRemoved() {
        ACTIVE_CENTERS.remove(this);
        if (level != null && !level.isClientSide()) {
            for (java.util.UUID uuid : GravityApi.SOLES_INVERTED_ENTITIES) {
                Player player = level.getPlayerByUUID(uuid);
                if (player != null && !GravityApi.hasGravitonSoles(player)) {
                    if (getAffectingCenter(level, player.position()) == null) {
                        GravityApi.SOLES_INVERTED_ENTITIES.remove(uuid);
                        GravityApi.resetGravity(player);
                    }
                }
            }
        }
        super.setRemoved();
    }
}
