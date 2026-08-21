package ddraig.net.entropica.block.entity;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.block.OpticalReceiverPortBlock;
import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class OpticalReceiverPortBlockEntity extends BlockEntity {

    private boolean isEmitting = false;
    private String activeStarName = "Uncalibrated";
    private int emittingTicksLeft = 0;
    private float beamDistance = 0.0f;

    private float signalQuality = 1.0f;

    public OpticalReceiverPortBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.OPTICAL_RECEIVER_PORT_BE.get(), pos, state);
    }

    public boolean isEmitting() {
        return isEmitting;
    }

    public String getActiveStarName() {
        return activeStarName;
    }

    public float getBeamDistance() {
        return beamDistance;
    }

    public float getSignalQuality() {
        return signalQuality;
    }

    public void receiveTransmittedSignal(String starName) {
        receiveTransmittedSignal(starName, 1.0f);
    }

    public void receiveTransmittedSignal(String starName, float signalQuality) {
        boolean wasEmitting = this.isEmitting;
        this.isEmitting = true;
        this.activeStarName = starName != null ? starName : "Uncalibrated";
        this.signalQuality = signalQuality;
        this.emittingTicksLeft = 20;
        setChanged();
        if (!wasEmitting && level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, OpticalReceiverPortBlockEntity be) {
        if (level.isClientSide()) {
            return;
        }

        boolean wasEmitting = be.isEmitting;
        if (be.emittingTicksLeft > 0) {
            be.emittingTicksLeft--;
            be.isEmitting = true;
        } else {
            be.isEmitting = false;
        }

        if (wasEmitting != be.isEmitting) {
            be.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }

        if (be.isEmitting) {
            Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
            Vec3 start = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            double maxRange = 16.0 * be.signalQuality;
            Vec3 end = start.add(facing.getStepX() * maxRange, 0, facing.getStepZ() * maxRange);

            double closestDist = maxRange;

            BlockHitResult hit = level.clip(new ClipContext(
                    start, end,
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    net.minecraft.world.phys.shapes.CollisionContext.empty()
            ));

            BlockPos hitBlockPos = null;
            if (hit.getType() == HitResult.Type.BLOCK) {
                hitBlockPos = hit.getBlockPos();
                closestDist = start.distanceTo(hit.getLocation());
            }

            // Check for Living Entities blocking the beam
            net.minecraft.world.phys.AABB beamBounds = new net.minecraft.world.phys.AABB(start, start.add(facing.getStepX() * closestDist, 0, facing.getStepZ() * closestDist)).inflate(0.4);
            net.minecraft.world.entity.LivingEntity blockingEntity = null;
            for (net.minecraft.world.entity.LivingEntity living : level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, beamBounds, e -> !e.isSpectator() && e.isAlive())) {
                net.minecraft.world.phys.AABB entityBox = living.getBoundingBox().inflate(0.1);
                java.util.Optional<Vec3> optHit = entityBox.clip(start, end);
                if (optHit.isPresent()) {
                    double entityDist = start.distanceTo(optHit.get());
                    if (entityDist < closestDist) {
                        closestDist = entityDist;
                        blockingEntity = living;
                        hitBlockPos = null;
                    }
                }
            }

            be.beamDistance = (float) closestDist;
            ddraig.net.entropica.api.EssenceType starEssence = RefractiveAstralLensBlockEntity.resolveStarEssence(be.activeStarName);

            if (blockingEntity != null) {
                blockingEntity.addEffect(new net.minecraft.world.effect.MobEffectInstance(ddraig.net.entropica.registry.ModEffects.MATERIA_TOXICITY, 100, 0, false, true, true));
                ddraig.net.entropica.registry.ModAttachments.setToxicitySource(blockingEntity, starEssence.name());
                if (level instanceof net.minecraft.server.level.ServerLevel serverLevel && serverLevel.getGameTime() % 10 == 0) {
                    serverLevel.playSound(null, blockingEntity.getX(), blockingEntity.getY() + 0.5, blockingEntity.getZ(), net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH, net.minecraft.sounds.SoundSource.BLOCKS, 0.4f, 1.8f);
                    serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK, blockingEntity.getX(), blockingEntity.getY() + 1.0, blockingEntity.getZ(), 5, 0.2, 0.2, 0.2, 0.05);
                }
            } else if (hitBlockPos != null) {
                BlockEntity targetBE = level.getBlockEntity(hitBlockPos);
                if (targetBE instanceof RefractiveAstralLensBlockEntity lens) {
                    lens.receiveRelayBeam(pos, be.activeStarName);
                } else if (targetBE instanceof SecondaryAstralLensBlockEntity secondaryLens) {
                    secondaryLens.receiveRelayBeam(pos, be.activeStarName);
                } else if (targetBE instanceof AstralInfusionPedestalBlockEntity pedestal) {
                    pedestal.receiveIrradiation(be.activeStarName);
                } else if (targetBE instanceof BeamSplitterPrismBlockEntity prism) {
                    prism.receiveRelayedBeam(pos, be.activeStarName);
                } else if (targetBE instanceof AstralCollectorBlockEntity collector) {
                    collector.receiveStarlightBeam(be.activeStarName, starEssence);
                }
            }

            if (level.getGameTime() % 20 == 0) {
                level.sendBlockUpdated(pos, state, state, 3);
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("IsEmitting", Codec.BOOL, this.isEmitting);
        output.store("ActiveStarName", Codec.STRING, this.activeStarName);
        output.store("EmittingTicksLeft", Codec.INT, this.emittingTicksLeft);
        output.store("BeamDistance", Codec.FLOAT, this.beamDistance);
        output.store("SignalQuality", Codec.FLOAT, this.signalQuality);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.read("IsEmitting", Codec.BOOL).ifPresent(e -> this.isEmitting = e);
        input.read("ActiveStarName", Codec.STRING).ifPresent(s -> this.activeStarName = s);
        input.read("EmittingTicksLeft", Codec.INT).ifPresent(t -> this.emittingTicksLeft = t);
        input.read("BeamDistance", Codec.FLOAT).ifPresent(d -> this.beamDistance = d);
        input.read("SignalQuality", Codec.FLOAT).ifPresent(q -> this.signalQuality = q);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider p) {
        return this.saveWithoutMetadata(p);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
