package ddraig.net.entropica.block.entity;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.block.OpticTransmitterBlock;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.registry.ModAttachments;
import ddraig.net.entropica.registry.ModBlockEntities;
import ddraig.net.entropica.registry.ModEffects;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.List;

public class OpticTransmitterBlockEntity extends BlockEntity {
    private String activeStarName = "";
    private int transmittingTicksLeft = 0;
    private boolean isTransmitting = false;
    private float beamDistance = 0.0f;

    public OpticTransmitterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.OPTIC_TRANSMITTER_BE.get(), pos, state);
    }

    public AABB getRenderBoundingBox() {
        return new AABB(-30000000.0, -30000000.0, -30000000.0, 30000000.0, 30000000.0, 30000000.0);
    }

    public void receiveFiberSignal(String starName) {
        this.activeStarName = starName != null ? starName : "";
        this.transmittingTicksLeft = 10;
        this.isTransmitting = true;
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public boolean isTransmitting() {
        return isTransmitting;
    }

    public float getBeamDistance() {
        return beamDistance;
    }

    public String getActiveStarName() {
        return activeStarName;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, OpticTransmitterBlockEntity be) {
        if (level.isClientSide()) {
            if (be.transmittingTicksLeft > 0) {
                be.transmittingTicksLeft--;
                be.isTransmitting = true;
            } else {
                be.isTransmitting = false;
            }
            return;
        }

        if (be.transmittingTicksLeft > 0) {
            be.transmittingTicksLeft--;
            be.isTransmitting = true;
        } else {
            be.isTransmitting = false;
            be.beamDistance = 0.0f;
            return;
        }

        Direction facing = state.getValue(OpticTransmitterBlock.FACING);
        Vec3 nozzle = Vec3.atCenterOf(pos).add(facing.getStepX() * 0.45, facing.getStepY() * 0.45, facing.getStepZ() * 0.45);
        Vec3 dir = new Vec3(facing.getStepX(), facing.getStepY(), facing.getStepZ());
        double maxRange = 32.0;
        Vec3 rayEnd = nozzle.add(dir.scale(maxRange));

        double closestDist = maxRange;
        BlockEntity closestBE = null;
        Set<BlockPos> checkedPositions = new HashSet<>();

        for (double step = 0.5; step <= maxRange; step += 0.15) {
            Vec3 samplePoint = nozzle.add(dir.scale(step));
            BlockPos p = BlockPos.containing(samplePoint);
            if (!checkedPositions.add(p)) continue;
            if (p.equals(pos)) continue;

            BlockState blockState = level.getBlockState(p);
            BlockEntity targetBE = level.getBlockEntity(p);

            if (targetBE != null) {
                boolean isOptic = (targetBE instanceof SecondaryAstralLensBlockEntity ||
                                   targetBE instanceof RefractiveAstralLensBlockEntity ||
                                   targetBE instanceof BeamSplitterPrismBlockEntity ||
                                   targetBE instanceof AstralInfusionPedestalBlockEntity ||
                                   targetBE instanceof OpticalTransmitterPortBlockEntity ||
                                   targetBE instanceof OpticReceiverBlockEntity ||
                                   targetBE instanceof AstralCollectorBlockEntity);

                if (isOptic) {
                    double centerY = (targetBE instanceof AstralCollectorBlockEntity) ? 1.25 : 
                                     ((targetBE instanceof SecondaryAstralLensBlockEntity || targetBE instanceof RefractiveAstralLensBlockEntity) ? 0.5625 : 
                                     (targetBE instanceof AstralInfusionPedestalBlockEntity ? 1.1 : 0.5));
                    Vec3 targetCenter = new Vec3(p.getX() + 0.5, p.getY() + centerY, p.getZ() + 0.5);
                    closestDist = Math.min(step, nozzle.distanceTo(targetCenter));
                    closestBE = targetBE;
                    break;
                }
            }

            if (RefractiveAstralLensBlockEntity.isOpticalPassthrough(level, p, blockState)) {
                continue;
            }

            net.minecraft.world.phys.shapes.VoxelShape shape = blockState.getCollisionShape(level, p);
            if (shape.isEmpty()) shape = blockState.getShape(level, p);
            BlockHitResult bHit = shape.clip(nozzle, rayEnd, p);
            if (bHit != null && bHit.getType() == HitResult.Type.BLOCK) {
                closestDist = nozzle.distanceTo(bHit.getLocation());
            } else {
                closestDist = step;
            }
            closestBE = null;
            break;
        }

        // Check for intercepting living entities along the beam
        AABB beamBox = new AABB(nozzle, nozzle.add(dir.scale(closestDist))).inflate(0.35);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, beamBox, e -> e.isAlive() && !e.isSpectator());
        LivingEntity blockingEntity = null;

        for (LivingEntity entity : entities) {
            AABB entityBox = entity.getBoundingBox().inflate(0.2);
            var optClip = entityBox.clip(nozzle, rayEnd);
            if (optClip.isPresent()) {
                double d = nozzle.distanceTo(optClip.get());
                if (d < closestDist) {
                    closestDist = d;
                    blockingEntity = entity;
                    closestBE = null;
                }
            }
        }

        be.beamDistance = (float) closestDist;

        EssenceType starEssence = RefractiveAstralLensBlockEntity.resolveStarEssence(be.activeStarName);

        if (blockingEntity != null) {
            // Apply Materia Toxicity & magic damage
            blockingEntity.addEffect(new MobEffectInstance(ModEffects.MATERIA_TOXICITY, 100, 0, false, true, true));
            ModAttachments.setToxicitySource(blockingEntity, starEssence.name());
            if (level instanceof ServerLevel serverLevel && serverLevel.getGameTime() % 10 == 0) {
                serverLevel.playSound(null, blockingEntity.getX(), blockingEntity.getY() + 0.5, blockingEntity.getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.4f, 1.8f);
                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, blockingEntity.getX(), blockingEntity.getY() + 1.0, blockingEntity.getZ(), 5, 0.2, 0.2, 0.2, 0.05);
            }
        } else if (closestBE != null) {
            if (closestBE instanceof AstralInfusionPedestalBlockEntity pedestal) {
                pedestal.receiveIrradiation(be.activeStarName);
            } else if (closestBE instanceof AstralCollectorBlockEntity collector) {
                collector.receiveStarlightBeam(be.activeStarName, starEssence);
            } else if (closestBE instanceof OpticReceiverBlockEntity receiver) {
                receiver.receiveOpticalBeam(be.activeStarName);
            } else if (closestBE instanceof BeamSplitterPrismBlockEntity prism) {
                prism.receiveRelayedBeam(pos, be.activeStarName);
            } else if (closestBE instanceof RefractiveAstralLensBlockEntity lens) {
                lens.receiveRelayBeam(pos, be.activeStarName);
            } else if (closestBE instanceof SecondaryAstralLensBlockEntity secondaryLens) {
                secondaryLens.receiveRelayBeam(pos, be.activeStarName);
            }
        }

        if (level.getGameTime() % 4 == 0) {
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("ActiveStarName", Codec.STRING, this.activeStarName);
        output.store("TransmittingTicksLeft", Codec.INT, this.transmittingTicksLeft);
        output.store("IsTransmitting", Codec.BOOL, this.isTransmitting);
        output.store("BeamDistance", Codec.FLOAT, this.beamDistance);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.read("ActiveStarName", Codec.STRING).ifPresent(s -> this.activeStarName = s);
        input.read("TransmittingTicksLeft", Codec.INT).ifPresent(t -> this.transmittingTicksLeft = t);
        input.read("IsTransmitting", Codec.BOOL).ifPresent(b -> this.isTransmitting = b);
        input.read("BeamDistance", Codec.FLOAT).ifPresent(d -> this.beamDistance = d);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return this.saveWithoutMetadata(provider);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
